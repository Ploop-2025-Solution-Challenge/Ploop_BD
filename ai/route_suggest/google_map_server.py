#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
FastAPI 서버
- 요청으로 받은 temp_trash_data.json 형식(payload)을 그대로 보존하고,
  success, message, waypoints, (성공 시) route 를 "추가"해서 응답.
- destination 미제공 시:
    * bins가 1개 이상이면 현재 위치에서 가장 가까운 bin을 destination으로 설정
    * bins가 존재하지 않으면 현재 위치를 destination으로 설정
- 파일 저장 없음. 콘솔에 응답 payload를 print.
"""

import os, math, json
from typing import List, Dict, Any, Tuple
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import requests
import numpy as np

# ===== 설정 =====
GOOGLE_MAPS_API_KEY = os.environ.get("GOOGLE_MAPS_API_KEY", "KEY...")
ROUTES_URL = "https://routes.googleapis.com/directions/v2:computeRoutes"
FIELD_MASK = "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline"

app = FastAPI(title="Trash Routing Server", version="1.4.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 개발 편의. 배포 시 도메인 제한 권장
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ===== 유틸리티 =====
def haversine(lat1, lng1, lat2, lng2) -> float:
    """미터 단위 거리"""
    R = 6371000.0
    dlat = math.radians(lat2 - lat1)
    dlng = math.radians(lng2 - lng1)
    a = math.sin(dlat/2)**2 + math.cos(math.radians(lat1))*math.cos(math.radians(lat2))*math.sin(dlng/2)**2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
    return R * c

def kmeans_simple(points: np.ndarray, k: int, max_iter: int = 100, seed: int = 42) -> Tuple[np.ndarray, np.ndarray]:
    """
    간단 K-Means. points: (N,2)[lat,lng] -> (centroids(k,2), labels(N,))
    """
    rng = np.random.default_rng(seed)
    idx = rng.choice(points.shape[0], size=k, replace=False)
    centroids = points[idx].copy()
    for _ in range(max_iter):
        dists = np.linalg.norm(points[:, None, :] - centroids[None, :, :], axis=2)  # (N,k)
        labels = np.argmin(dists, axis=1)
        new_centroids = np.array([
            points[labels == j].mean(axis=0) if np.any(labels == j) else centroids[j]
            for j in range(k)
        ])
        if np.allclose(new_centroids, centroids):
            break
        centroids = new_centroids
    return centroids, labels

def build_routes_request(current: Dict[str, float], destination: Dict[str, float], waypoints: List[Dict[str, float]]) -> Dict[str, Any]:
    """도보(WALK) 경로 요청. WALK/BICYCLE에서는 routingPreference를 설정하면 안 됨."""
    return {
        "origin": {"location": {"latLng": {"latitude": current["lat"], "longitude": current["lng"]}} },
        "destination": {"location": {"latLng": {"latitude": destination["lat"], "longitude": destination["lng"]}} },
        "travelMode": "WALK",
        "intermediates": [
            {"location": {"latLng": {"latitude": w["lat"], "longitude": w["lng"]}}}
            for w in waypoints
        ]
    }

def choose_nearest_real_points_to_centroids(
    centroids: np.ndarray,
    trash_points: List[Dict[str, float]]
) -> List[Dict[str, float]]:
    """
    각 centroid마다 가장 가까운 '실제' trash 좌표를 waypoint로 선택(중복 방지).
    """
    used = set()
    waypoints: List[Dict[str, float]] = []
    for c in centroids:
        c_lat, c_lng = float(c[0]), float(c[1])
        best_idx, best_dist = None, float("inf")
        for idx, t in enumerate(trash_points):
            if idx in used:
                continue
            d = haversine(c_lat, c_lng, t["lat"], t["lng"])
            if d < best_dist:
                best_dist, best_idx = d, idx
        if best_idx is not None:
            used.add(best_idx)
            waypoints.append({"lat": trash_points[best_idx]["lat"], "lng": trash_points[best_idx]["lng"]})
    return waypoints

def sort_waypoints_by_distance_from_current(waypoints: List[Dict[str, float]], current: Dict[str, float]) -> List[Dict[str, float]]:
    return sorted(waypoints, key=lambda w: haversine(current["lat"], current["lng"], w["lat"], w["lng"]))

def respond_merged(original_payload: Dict[str, Any], additions: Dict[str, Any]) -> JSONResponse:
    """
    원본 payload 구조를 보존하면서 fields를 추가하여 반환.
    콘솔에 prettified JSON을 출력.
    """
    merged = dict(original_payload)  # shallow copy
    for k, v in additions.items():
        merged[k] = v

    print("\n=== Response to frontend (merged) ===")
    try:
        print(json.dumps(merged, ensure_ascii=False, indent=2))
    except Exception:
        print(str(merged))
    print("=== End of response ===\n")

    return JSONResponse(merged, status_code=200)

# ===== 엔드포인트 =====
@app.post("/route/compute")
async def compute_route(req: Request):
    """
    입력: temp_trash_data.json 포맷
    처리:
      - destination 미제공 시:
          · bins가 2개 이상 → 현재 위치에서 가장 가까운 bin을 destination으로 설정
          · bins가 1개 이하 → 현재 위치를 destination으로 설정
      - (1) trash 수 < 3 → K-Means 생략, 실제 trash 좌표 전체를 waypoint로 사용
      - (2) trash 수 ≥ 3 → K=3 K-Means 후, 각 centroid에 가장 가까운 '실제' 쓰레기 좌표를 waypoint로 채택(중복 방지)
      - (3) waypoints를 current와의 거리 오름차순으로 정렬
      - (4) Routes API 호출(실패해도 success:false로 200 응답)
    응답: 요청 payload에 다음 필드를 "추가"하여 반환
         { success: bool, message?: str, waypoints: [...], route?: {...} }
    """
    # --- 입력 파싱 ---
    try:
        payload = await req.json()
    except Exception:
        return respond_merged({}, {"success": False, "message": "Invalid JSON", "waypoints": []})

    current = payload.get("current")
    destination = payload.get("destination")
    trash = payload.get("trash", [])
    bins = payload.get("bins", [])

    # 현재 위치는 필수
    if not current:
        return respond_merged(payload, {
            "success": False,
            "message": "current 필수",
            "waypoints": []
        })

    # 목적지 자동 보정 로직
    if not destination:
        auto_msg = None
        # bins가 1개 이상이면 현재 위치에서 가장 가까운 bin을 목적지로
        if isinstance(bins, list) and len(bins) >= 1:
            def _dist_bin(b):
                try:
                    return haversine(float(current["lat"]), float(current["lng"]), float(b["lat"]), float(b["lng"]))
                except Exception:
                    return float("inf")
            nearest = min(bins, key=_dist_bin)
            destination = {"lat": float(nearest["lat"]), "lng": float(nearest["lng"])}
            payload["destination"] = destination  # 원본에도 주입
            auto_msg = "destination이 없어 가장 가까운 쓰레기통을 목적지로 설정"
        else:
            # bins가 없으면 → 현재 위치를 목적지로
            destination = {"lat": float(current["lat"]), "lng": float(current["lng"])}
            payload["destination"] = destination
            auto_msg = "destination이 없어 현재 위치를 목적지로 설정"
        # 참고 메시지는 최종 message에 덧붙일 수 있음 (아래에서 처리)

    # trash 최소 1개 필요
    if not (trash and len(trash) >= 1):
        # 위에서 destination을 주입했을 수 있으니 payload는 그대로 반환
        return respond_merged(payload, {
            "success": False,
            "message": "trash(>=1) 필요",
            "waypoints": []
        })

    # --- waypoint 계산 ---
    trash_points = [{"lat": float(t["lat"]), "lng": float(t["lng"])} for t in trash]

    if len(trash_points) < 3:
        waypoints = trash_points.copy()
    else:
        k = 3
        pts = np.array([[p["lat"], p["lng"]] for p in trash_points], dtype=float)
        centroids, _ = kmeans_simple(pts, k=k, max_iter=100)
        waypoints = choose_nearest_real_points_to_centroids(centroids, trash_points)
        if len(waypoints) < k:
            chosen_set = {(w["lat"], w["lng"]) for w in waypoints}
            remain = [p for p in trash_points if (p["lat"], p["lng"]) not in chosen_set]
            remain_sorted = sort_waypoints_by_distance_from_current(remain, current)
            for p in remain_sorted:
                if len(waypoints) >= k:
                    break
                waypoints.append(p)

    # (3) 현재 위치와 가까운 순으로 정렬
    waypoints = sort_waypoints_by_distance_from_current(waypoints, current)

    # (4) Routes API 호출 (한국/키 문제 등 실패 시 success:false)
    if not GOOGLE_MAPS_API_KEY or GOOGLE_MAPS_API_KEY in {"YOUR_GOOGLE_API_KEY"}:
        msg = "GOOGLE_MAPS_API_KEY 미설정 또는 한국 지역 제한"
        # destination 자동 설정 메시지 부가
        if not payload.get("message") and 'auto_msg' in locals() and auto_msg:
            msg = f"{msg} · {auto_msg}"
        return respond_merged(payload, {
            "success": False,
            "message": msg,
            "waypoints": waypoints
        })

    headers = {
        "Content-Type": "application/json",
        "X-Goog-Api-Key": GOOGLE_MAPS_API_KEY,
        "X-Goog-FieldMask": FIELD_MASK,
    }
    routes_req = build_routes_request(current, destination, waypoints)

    try:
        r = requests.post(ROUTES_URL, headers=headers, json=routes_req, timeout=20)
        if r.status_code != 200:
            msg = f"Routes API HTTP {r.status_code}"
            if 'auto_msg' in locals() and auto_msg:
                msg = f"{msg} · {auto_msg}"
            return respond_merged(payload, {
                "success": False,
                "message": msg,
                "detail": r.text[:500],
                "waypoints": waypoints
            })

        data = r.json()
        routes = data.get("routes", [])
        if not routes:
            msg = "Routes API가 경로를 반환하지 않음"
            if 'auto_msg' in locals() and auto_msg:
                msg = f"{msg} · {auto_msg}"
            return respond_merged(payload, {
                "success": False,
                "message": msg,
                "waypoints": waypoints
            })

        route0 = routes[0]
        encoded = route0.get("polyline", {}).get("encodedPolyline")
        if not encoded:
            msg = "encoded polyline 없음"
            if 'auto_msg' in locals() and auto_msg:
                msg = f"{msg} · {auto_msg}"
            return respond_merged(payload, {
                "success": False,
                "message": msg,
                "waypoints": waypoints
            })

        msg = "ok"
        if 'auto_msg' in locals() and auto_msg:
            msg = f"{msg} · {auto_msg}"

        return respond_merged(payload, {
            "success": True,
            "message": msg,
            "waypoints": waypoints,
            "route": {
                "encodedPolyline": encoded,
                "distanceMeters": route0.get("distanceMeters"),
                "duration": route0.get("duration")
            }
        })

    except requests.RequestException as e:
        msg = "Routes API 요청 실패"
        if 'auto_msg' in locals() and auto_msg:
            msg = f"{msg} · {auto_msg}"
        return respond_merged(payload, {
            "success": False,
            "message": msg,
            "detail": str(e),
            "waypoints": waypoints
        })


if __name__ == "__main__":
    # uvicorn google_map_server:app --reload --port 8000
    uvicorn.run("google_map_server:app", host="0.0.0.0", port=8000, reload=True)
