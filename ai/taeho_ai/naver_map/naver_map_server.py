# server.py
import os, json, math
from typing import List, Tuple, Optional
from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import numpy as np
from sklearn.cluster import KMeans

app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], allow_credentials=True, allow_methods=["*"], allow_headers=["*"],
)

# -------------------------------
# Utils
# -------------------------------
def haversine(p1: Tuple[float,float], p2: Tuple[float,float]) -> float:
    """거리(m), p=(lng,lat)"""
    lng1, lat1 = p1; lng2, lat2 = p2
    R = 6371000.0
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dphi = math.radians(lat2 - lat1)
    dlng = math.radians(lng2 - lng1)
    a = math.sin(dphi/2)**2 + math.cos(phi1)*math.cos(phi2)*math.sin(dlng/2)**2
    return 2 * R * math.asin(math.sqrt(a))

def unique_trash_points(trash: list) -> List[Tuple[float, float]]:
    """
    trash 배열에서 (lng,lat) 튜플로 변환 + 중복 제거(입력 순서 보존)
    """
    seen = set()
    uniq: List[Tuple[float, float]] = []
    for t in trash:
        lng = float(t["lng"]); lat = float(t["lat"])
        key = (lng, lat)
        if key not in seen:
            seen.add(key)
            uniq.append(key)
    return uniq

def kmeans_centroids_from_points(points: List[Tuple[float,float]], k: int) -> List[Tuple[float,float]]:
    """
    points: [(lng,lat), ...]
    KMeans(K=k) → [[lng,lat], ...] 반환
    """
    if len(points) < k or k < 1:
        raise HTTPException(status_code=400, detail=f"need at least {k} unique trash points")
    arr = np.array(points, dtype=float)
    km = KMeans(n_clusters=k, n_init="auto", random_state=42)
    km.fit(arr)
    centers = km.cluster_centers_.tolist()
    return [ (float(c[0]), float(c[1])) for c in centers ]

def assign_centroids_to_nearest_trash(
    centroids: List[Tuple[float,float]],
    trash_points_unique: List[Tuple[float,float]]
) -> List[Tuple[float,float]]:
    """
    각 센트로이드를 가장 가까운 '미사용' 실제 쓰레기 위치로 매핑(그리디).
    trash_points_unique 의 개수는 k 이상이라고 가정.
    """
    remaining = trash_points_unique[:]  # 미사용 후보
    assigned: List[Tuple[float,float]] = []
    for c in centroids:
        # 가장 가까운 미사용 trash 선택
        best = min(remaining, key=lambda p: haversine(c, p))
        assigned.append(best)
        remaining.remove(best)
    return assigned

def resolve_destination_if_missing(body: dict) -> Optional[dict]:
    """
    destination이 없으면: current에서 가장 가까운 bin을 찾아 destination으로 설정한 사본을 반환.
    destination이 이미 있으면 None 반환.
    """
    if "destination" in body and body["destination"]:
        return None
    if "current" not in body or "bins" not in body or not body["bins"]:
        return None  # 결정 불가 → 그대로 둠(필수는 아님)
    cur = (float(body["current"]["lng"]), float(body["current"]["lat"]))
    bins = [(float(b["lng"]), float(b["lat"])) for b in body["bins"]]
    near = min(bins, key=lambda p: haversine(cur, p))
    new_body = json.loads(json.dumps(body))  # deep copy
    new_body["destination"] = {"lng": near[0], "lat": near[1]}
    return new_body

# -------------------------------
# Endpoint
# -------------------------------
@app.post("/waypoints")
async def waypoints(request: Request, k: int = 3):
    """
    프론트에서 보낸 temp_trash_data.json 본문을 받아서:
    - destination 없으면 current에서 가장 가까운 bin으로 보정(선택적)
    - 쓰레기 좌표가 KMeans 불가( len(unique_trash)<k 또는 k<1 )면,
      waypoints = 실제 쓰레기 위치들(중복 제거) 그대로 반환
    - KMeans 가능하면 KMeans 수행 후, 각 센트로이드를 가장 가까운 '실제 쓰레기 위치'로 스냅하여 waypoints 반환
    """
    try:
        body = await request.json()
    except Exception:
        raise HTTPException(status_code=400, detail="Invalid JSON body")

    # 필수 키 검증
    if "trash" not in body or not isinstance(body["trash"], list) or len(body["trash"]) == 0:
        raise HTTPException(status_code=400, detail="JSON must include non-empty 'trash' array")
    if "current" not in body:
        raise HTTPException(status_code=400, detail="JSON must include 'current'")

    # destination 자동 보정(없을 때만)
    replaced = resolve_destination_if_missing(body)
    effective = replaced if replaced is not None else body

    # 고유 쓰레기 포인트(중복 제거)
    uniq_trash = unique_trash_points(effective["trash"])
    if len(uniq_trash) == 0:
        raise HTTPException(status_code=400, detail="No valid trash points")

    # --- 요구사항 1: KMeans 불가 시, 쓰레기 위치들 자체를 반환 ---
    fallback = False
    if k < 1 or len(uniq_trash) < k:
        fallback = True
        waypoints_points = uniq_trash  # [(lng,lat), ...]
    else:
        # --- KMeans 수행 ---
        # (KMeans는 중복 포함 원데이터로 하든 uniq로 하든 큰 차이는 없지만, 안정적으로 uniq 사용)
        centroids = kmeans_centroids_from_points(uniq_trash, k=k)  # [(lng,lat), ...]

        # --- 요구사항 2: 센트로이드를 실제 쓰레기 위치로 스냅 ---
        waypoints_points = assign_centroids_to_nearest_trash(centroids, uniq_trash)

    # 응답 구성
    resp = {
        "waypoints": [[lng, lat] for (lng, lat) in waypoints_points],  # [[lng,lat], ...]
        "k": k,
        "count": len(waypoints_points),
        "fallback": fallback,  # True면 KMeans 대신 쓰레기 위치들을 그대로 반환한 경우
    }
    if replaced is not None:
        resp["destinationResolved"] = effective["destination"]  # 선택적 정보

    return resp

# -------------------------------
if __name__ == "__main__":
    # 필요 시 포트 조정
    uvicorn.run(app, host="0.0.0.0", port=8080)
