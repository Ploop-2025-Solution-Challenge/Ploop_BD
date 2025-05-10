from pydantic import BaseModel
from ultralytics import YOLO


class ImageUrlRequest(BaseModel):
    image_url: str

model = YOLO("YOLOv11_trash/train13/weights/best.pt")

def detect_trash(image):
    # 객체 감지 실행
    results = model(image)

    # 결과를 저장할 JSON 구조 초기화
    json_result = {
        "total_detections": 0,
        "objects": []
    }

    for result in results:
        # 박스 정보 가져오기
        boxes = result.boxes

        # 검출된 객체 수
        num_detections = len(boxes)
        json_result["total_detections"] += num_detections

        # 각 검출 객체에 대한 정보 기록
        for box, cls, conf in zip(boxes.xyxy, boxes.cls, boxes.conf):
            x1, y1, x2, y2 = box.tolist()
            class_id = int(cls.item())
            class_name = result.names[class_id]
            confidence = conf.item()

            # 객체 정보 저장
            detection_info = {
                "class": class_name,
                "confidence": float(confidence),
                "bbox": {
                    "x1": float(x1),
                    "y1": float(y1),
                    "x2": float(x2),
                    "y2": float(y2)
                }
            }
            json_result["objects"].append(detection_info)

        # 시각화 이미지 생성
    result_plot = results[0].plot()

    return json_result, result_plot, results