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

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import uvicorn


# 비즈니스 로직 모듈
from google_route import handle_compute, invalid_json_response
from trash_detection import TrashDetector, DetectionError

app = FastAPI(title="Trash Routing Server", version="1.4.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],   # 개발 편의. 배포 시 도메인 제한 권장
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Detector는 서버 기동 시 1회 로드하여 재사용(모델 재로딩 방지)
detector = TrashDetector(
    model_id="IDEA-Research/grounding-dino-base",
    device_preference="cuda"  # "cuda" 또는 "cpu"
)

@app.post("/route/compute")
async def compute_route(req: Request):
    """
    통신(HTTP) 레이어만 담당.
    - 요청 JSON 파싱
    - 비즈니스 로직 모듈(google_route.handle_compute) 호출
    - JSONResponse 반환
    """
    try:
        payload = await req.json()
    except Exception:
        # JSON 파싱 실패 시 통일된 에러 응답
        return invalid_json_response()

    # 로직 처리는 전부 google_route로 위임
    return handle_compute(payload)

@app.post("/detect")
async def detect_endpoint(req: Request):
    """
    요청(JSON):
      { "image": "<base64 string>" }

    응답(JSON): (성공)
      { "success": true, "image": "<base64 PNG>", "objects": [str...], "bbox": [[x1,y1,x2,y2], ...] }

    응답(JSON): (실패)
      { "success": false, "msg": "<이유>" }
    """
    try:
        body = await req.json()
    except Exception:
        return JSONResponse({"success": False, "msg": "invalid JSON body"}, status_code=200)

    if not isinstance(body, dict):
        return JSONResponse({"success": False, "msg": "body must be a JSON object"}, status_code=200)

    image_b64 = body.get("image")
    if not image_b64 or not isinstance(image_b64, str):
        return JSONResponse({"success": False, "msg": "field 'image' (base64 string) is required"}, status_code=200)

    try:
        # 프롬프트와 임계값은 내부에서 고정 사용
        result = detector.detect_from_base64(image_b64=image_b64)
        return JSONResponse(result, status_code=200)

    except DetectionError as e:
        return JSONResponse({"success": False, "msg": str(e)}, status_code=200)
    except Exception as e:
        return JSONResponse({"success": False, "msg": f"internal error: {e}"}, status_code=200)


if __name__ == "__main__":
    # uvicorn google_map_server:app --reload --port 8000
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
