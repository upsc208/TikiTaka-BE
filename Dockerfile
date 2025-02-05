# 기본 이미지 선택
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY build/libs/tikitaka-0.0.1-SNAPSHOT.jar app.jar

# 환경 변수를 사용하여 실행
CMD ["java", "-jar", "app.jar"]

# 컨테이너 포트 노출
EXPOSE 8080
