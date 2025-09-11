# -*- coding: utf-8 -*-

import io
import re
import base64
from typing import Dict, Any, List, Tuple

# 디스플레이 없는 서버 환경에서도 렌더링 가능하게 설정
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import matplotlib.patches as patches

import torch
from PIL import Image
from transformers import AutoProcessor, AutoModelForZeroShotObjectDetection

# ===== 고정 프롬프트 & 임계값 =====
PROMPT = "wastebasket. trash. garbage."   # 요구사항: 프롬프트 고정
CONF_THRESHOLD = 0.35                      # 시각화/리스트 필터 임계값
TEXT_THRESHOLD = 0.30                      # post-process 텍스트 매칭 임계값


class DetectionError(Exception):
    """탐지 파이프라인에서 사용자에게 보여줄 에러 메시지 포맷용 예외."""
    pass


def _strip_data_uri_prefix(b64: str) -> str:
    """
    data:image/png;base64,XXXX 형태의 접두사가 있으면 제거
    """
    return re.sub(r"^data:image\/[a-zA-Z0-9.+-]+;base64,", "", b64)


def _draw_boxes(
    image: Image.Image,
    boxes: List[Tuple[float, float, float, float]],
    scores: List[float],
    labels: List[str],
    conf_threshold: float
) -> Image.Image:
    """
    바운딩박스/라벨을 그려서 PNG 이미지(PIL)로 반환
    """
    fig, ax = plt.subplots(1, figsize=(12, 8))
    ax.imshow(image)

    for (x1, y1, x2, y2), score, label in zip(boxes, scores, labels):
        if score < conf_threshold:
            continue
        w, h = x2 - x1, y2 - y1
        rect = patches.Rectangle(
            (x1, y1), w, h,
            linewidth=2, edgecolor="red", facecolor="none"
        )
        ax.add_patch(rect)
        ax.text(
            x1, max(0, y1 - 5),
            f"{label} {score:.2f}",
            color="white", fontsize=10,
            bbox=dict(facecolor="red", alpha=0.5, edgecolor="none")
        )

    ax.axis("off")
    buf = io.BytesIO()
    plt.tight_layout(pad=0)
    fig.savefig(buf, format="png", bbox_inches="tight", pad_inches=0)
    plt.close(fig)
    buf.seek(0)
    return Image.open(buf).convert("RGB")


def _to_base64_png(img: Image.Image) -> str:
    """PIL.Image -> base64 PNG 문자열"""
    buf = io.BytesIO()
    img.save(buf, format="PNG")
    return base64.b64encode(buf.getvalue()).decode("utf-8")


class TrashDetector:
    """
    Grounding DINO 기반 제로샷 객체 탐지기 (프롬프트 고정)
    서버 기동 시 1회 로드 후 요청마다 detect 호출
    """
    def __init__(self, model_id: str = "IDEA-Research/grounding-dino-base", device_preference: str = "cuda"):
        self.device = "cuda" if (device_preference == "cuda" and torch.cuda.is_available()) else "cpu"
        try:
            self.processor = AutoProcessor.from_pretrained(model_id)
            self.model = AutoModelForZeroShotObjectDetection.from_pretrained(model_id).to(self.device)
            self.model.eval()
        except Exception as e:
            raise DetectionError(f"model load failed: {e}")

    def detect_from_base64(self, image_b64: str) -> Dict[str, Any]:
        """
        JSON으로 받은 base64 이미지를 디코드하여 탐지 수행
        """
        if not image_b64 or not isinstance(image_b64, str):
            raise DetectionError("image(base64) is empty")

        try:
            raw = _strip_data_uri_prefix(image_b64)
            img_bytes = base64.b64decode(raw, validate=True)
        except Exception:
            raise DetectionError("invalid base64 image")

        return self.detect(img_bytes)

    def detect(self, image_bytes: bytes) -> Dict[str, Any]:
        """
        실제 탐지 수행. 프롬프트/임계값 고정.
        성공 시:
          { success: True, image: <base64 PNG>, objects: [...], bbox: [[x1,y1,x2,y2], ...] }
        실패 시:
          { success: False, msg: ... }  (이 포맷은 app.py에서 래핑)
        """
        try:
            if not image_bytes:
                raise DetectionError("no image bytes")

            image = Image.open(io.BytesIO(image_bytes)).convert("RGB")

            # 전처리 & 추론
            inputs = self.processor(images=image, text=PROMPT, return_tensors="pt")
            inputs = {k: v.to(self.device) for k, v in inputs.items()}

            with torch.no_grad():
                outputs = self.model(**inputs)

            # post process (grounded)
            results = self.processor.post_process_grounded_object_detection(
                outputs=outputs,
                input_ids=inputs["input_ids"],
                text_threshold=float(TEXT_THRESHOLD),
                target_sizes=[image.size[::-1]]  # (H, W)
            )

            # 탐지 없음
            if not results or len(results[0]["boxes"]) == 0:
                return {
                    "success": True,
                    "image": _to_base64_png(image),  # 원본 반환(박스 없음)
                    "objects": [],
                    "bbox": []
                }

            r0 = results[0]
            boxes_tensor = r0["boxes"]    # (N, 4)
            scores_tensor = r0["scores"]  # (N,)
            labels_list = r0["labels"]    # (N,) -> 문자열 라벨들

            # Python 리스트로 변환
            boxes: List[Tuple[float, float, float, float]] = [tuple(map(float, b.tolist())) for b in boxes_tensor]
            scores: List[float] = [float(s.item()) for s in scores_tensor]
            labels: List[str] = [str(l) for l in labels_list]

            # 시각화 이미지 생성 (CONF_THRESHOLD 이상만 그림)
            vis_img = _draw_boxes(image, boxes, scores, labels, conf_threshold=CONF_THRESHOLD)
            vis_b64 = _to_base64_png(vis_img)

            # CONF_THRESHOLD 이상만 결과로 반환
            filtered_objects: List[str] = []
            filtered_bboxes: List[List[float]] = []
            for (x1, y1, x2, y2), score, label in zip(boxes, scores, labels):
                if score >= CONF_THRESHOLD:
                    filtered_objects.append(label)
                    filtered_bboxes.append([round(x1, 2), round(y1, 2), round(x2, 2), round(y2, 2)])

            return {
                "success": True,
                "image": vis_b64,
                "objects": filtered_objects,
                "bbox": filtered_bboxes
            }

        except DetectionError:
            raise
        except Exception as e:
            raise DetectionError(f"detection failed: {e}")
