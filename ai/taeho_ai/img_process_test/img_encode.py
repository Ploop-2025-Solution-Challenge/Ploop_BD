import base64
import os

def encode_image_to_base64(image_path, output_file="encoded.txt"):
    """
    이미지 파일을 base64로 인코딩하고 결과를 파일에 저장
    :param image_path: 인코딩할 이미지 파일 경로
    :param output_file: base64 결과를 저장할 파일 이름
    """
    try:
        # 이미지 파일 열기 (바이너리 모드)
        with open(image_path, "rb") as image_file:
            # 이미지 데이터를 base64로 인코딩
            encoded_string = base64.b64encode(image_file.read()).decode("utf-8")
        
        # 출력 경로 설정
        output_dir = "ai/taeho_ai/img_process_test"
        os.makedirs(output_dir, exist_ok=True)  # 디렉토리가 없으면 생성
        output_path = os.path.join(output_dir, output_file)
        
        # 인코딩된 문자열을 파일에 저장
        with open(output_path, "w") as output:
            output.write(encoded_string)
        
        print(f"이미지가 성공적으로 base64로 인코딩되어 '{output_path}'에 저장되었습니다.")
    except FileNotFoundError:
        print(f"파일을 찾을 수 없습니다: {image_path}")
    except Exception as e:
        print(f"오류 발생: {e}")

# 사용 예제
image_path = "ai/taeho_ai/img_process_test/trash_bin.jpg"  # 인코딩할 이미지 파일 경로
encode_image_to_base64(image_path)