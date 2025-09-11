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

app = FastAPI(title="Trash Routing Server", version="1.4.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],   # 개발 편의. 배포 시 도메인 제한 권장
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
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


if __name__ == "__main__":
    # uvicorn google_map_server:app --reload --port 8000
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
