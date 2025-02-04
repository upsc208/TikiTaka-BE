# 빌드 스테이지: Gradle을 이용하여 프로젝트 빌드
FROM gradle:8.0-jdk17 AS builder
WORKDIR /app
# 프로젝트 전체를 복사
COPY . .
# Gradle을 통해 프로젝트 빌드 (JAR 파일 생성)
RUN gradle clean build --no-daemon

# 실행 스테이지: 빌드 결과물만 복사하여 경량 이미지 생성
FROM openjdk:17-jdk-slim
WORKDIR /app
# 빌드 스테이지에서 생성된 JAR 파일을 실행 스테이지로 복사
COPY --from=builder /app/build/libs/your-project.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
