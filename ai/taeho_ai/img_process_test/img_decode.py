# detection.py 에서 받은 base64 이미지를 디코드하여 시각화하는 테스트 코드
# postman으로 base64 문자열을 받아서 테스트할 때 사용
import base64
from io import BytesIO
from PIL import Image
import matplotlib.pyplot as plt

def decode_base64_to_image(base64_string):
    """
    base64 문자열을 PIL Image 객체로 변환
    """
    # base64 문자열을 디코딩하여 바이트로 변환
    image_data = base64.b64decode(base64_string)
    # 바이트 데이터를 BytesIO 객체로 래핑
    image_buffer = BytesIO(image_data)
    # PIL Image 객체로 변환
    image = Image.open(image_buffer)
    return image

def visualize_image(image):
    """
    PIL Image 객체를 시각화
    """
    plt.imshow(image)
    plt.axis('off')  # 축 숨기기
    plt.show()

# 예제 base64 문자열 (테스트용)
example_base64 = ""  # 여기에 base64 문자열을 넣으세요

# base64 문자열을 이미지로 변환
image = decode_base64_to_image(example_base64)

# 이미지 시각화
visualize_image(image)
