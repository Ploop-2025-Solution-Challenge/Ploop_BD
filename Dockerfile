# backend/Dockerfile
# 첫 번째 스테이지: 빌드 스테이지, 대문자로 AS 작성
FROM gradle:jdk17-graal-jammy AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 소스 코드와 Gradle 래퍼 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Gradle 래퍼에 실행 권한 부여
RUN chmod +x ./gradlew

# 종속성 설치
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN ./gradlew build --no-daemon

# 두 번째 스테이지: 실행 스테이지
FROM ghcr.io/graalvm/jdk-community:17

# 🟡 UTF-8 로케일 설정 추가
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# 작업 디렉토리 설정
WORKDIR /app

# 첫 번째 스테이지에서 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 실행할 JAR 파일 지장
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
