import os
import sys
import json
import math
from typing import List, Tuple
import requests
import numpy as np
from sklearn.cluster import KMeans

# ===== 설정 =====
DIRECTIONS_ENDPOINT = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving"

# 환경변수(반드시 세팅)
NCP_API_KEY_ID = "..."     # ID
NCP_API_KEY = "..."   # SECRET ID

def die(msg: str, code: int = 1):
    print(f"[ERROR] {msg}")
    # sys.exit(code)

def haversine(p1: Tuple[float, float], p2: Tuple[float, float]) -> float:
    """거리(m), p=(lng,lat)"""
    lng1, lat1 = p1; lng2, lat2 = p2
    R = 6371000.0
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dphi = math.radians(lat2 - lat1)
    dlng = math.radians(lng2 - lng1)
    a = math.sin(dphi/2)**2 + math.cos(phi1)*math.cos(phi2)*math.sin(dlng/2)**2
    return 2 * R * math.asin(math.sqrt(a))

def greedy_order(start: Tuple[float,float], points: List[Tuple[float,float]]) -> List[Tuple[float,float]]:
    """현재(start)에서 가까운 점부터 방문하는 간단한 순서"""
    remaining = points[:]
    order = []
    cur = start
    while remaining:
        nxt = min(remaining, key=lambda p: haversine(cur, p))
        order.append(nxt)
        remaining.remove(nxt)
        cur = nxt
    return order

def load_json(path: str) -> dict:
    if not os.path.exists(path):
        die(f"JSON 파일을 찾을 수 없습니다: {path}")
    with open(path, "r", encoding="utf-8") as f:
        try:
            return json.load(f)
        except Exception as e:
            die(f"JSON 파싱 실패: {e}")

def main():
    if not NCP_API_KEY_ID or not NCP_API_KEY:
        die("환경변수 NCP_API_KEY_ID(=Client ID), NCP_API_KEY(=Client Secret)를 설정하세요.")

    # 인자: 경로 미지정 시 현재 폴더의 temp_trash_data.json 사용
    base_dir = os.path.dirname(os.path.abspath(__file__))
    json_path = sys.argv[1] if len(sys.argv) > 1 else os.path.join(base_dir, "temp_trash_data.json")
    data = load_json(json_path)

    # 필수 키 검사
    for k in ("current", "destination", "trash"):
        if k not in data:
            die(f"입력 JSON에 '{k}' 키가 없습니다. 파일: {json_path}")

    cur = (float(data["current"]["lng"]), float(data["current"]["lat"]))             # (lng,lat)
    goal = (float(data["destination"]["lng"]), float(data["destination"]["lat"]))

    trash_list = data.get("trash", [])
    if len(trash_list) < 3:
        die(f"trash 좌표가 {len(trash_list)}개입니다. K=3을 위해 최소 3개 필요합니다.")

    # K-Means(K=3)
    trash_arr = np.array([[float(t["lng"]), float(t["lat"])] for t in trash_list], dtype=float)
    kmeans = KMeans(n_clusters=3, n_init="auto", random_state=42)
    kmeans.fit(trash_arr)
    centroids = kmeans.cluster_centers_.tolist()   # [[lng,lat], ...]

    # 경유지 순서 결정(현재에서 가까운 순)
    ordered = greedy_order(cur, [tuple(c) for c in centroids])  # [(lng,lat), ...]
    waypoints_param = "|".join([f"{lng},{lat}" for (lng,lat) in ordered])

    # Directions 5 API 요청
    params = {
        "start": f"{cur[0]},{cur[1]}",
        "goal": f"{goal[0]},{goal[1]}",
        "waypoints": waypoints_param,
        "option": "trafast",  # 필요 시 traoptimal/traeco 등으로 변경
    }
    headers = {
        "X-NCP-APIGW-API-KEY-ID": NCP_API_KEY_ID,
        "X-NCP-APIGW-API-KEY": NCP_API_KEY,
    }


    print("[INFO] 요청 파라미터:", params)
    r = requests.get(DIRECTIONS_ENDPOINT, params=params, headers=headers, timeout=15)

    # 응답 저장
    save_path = os.path.join(base_dir, "response_test.json")
    try:
        resp_json = r.json()
    except Exception:
        resp_json = {"error": "Non-JSON", "text": r.text}
    with open(save_path, "w", encoding="utf-8") as f:
        json.dump(resp_json, f, ensure_ascii=False, indent=2)
    print(f"[INFO] 응답 저장: {save_path}")

    if r.status_code != 200:
        die(f"Directions API 실패: HTTP {r.status_code} — 콘솔의 response.json을 확인하세요.", r.status_code)

    # 대표 경로 파싱: route.trafast/traoptimal 등 중 첫 번째
    route_obj = None
    for key in ("traoptimal", "trafast", "tracomfort", "traeco"):
        if "route" in resp_json and key in resp_json["route"] and resp_json["route"][key]:
            route_obj = resp_json["route"][key][0]
            break
    if not route_obj or "path" not in route_obj:
        die("응답에 path가 없습니다. response.json의 내용을 확인하세요.")

    summary = route_obj.get("summary", {})
    print("[RESULT] 경유지(순서):")
    for i, (lng, lat) in enumerate(ordered, 1):
        print(f"  {i}. lng={lng:.6f}, lat={lat:.6f}")
    print("[RESULT] 요약:", {k: summary.get(k) for k in ("distance", "duration")})
    print("[RESULT] path 좌표 수:", len(route_obj["path"]))
    print("완료 ✅")

if __name__ == "__main__":
    main()
