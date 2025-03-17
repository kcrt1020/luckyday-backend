# OpenJDK 17 사용
FROM openjdk:17

# 환경 변수 설정 (파일 저장 경로)
ENV FILE_UPLOAD_DIR=/app/uploads

WORKDIR /app
COPY build/libs/luckyday-backend-0.0.1-SNAPSHOT.jar app.jar

# 업로드 폴더 생성
RUN mkdir -p /app/uploads

EXPOSE 8080
CMD ["java", "-jar", "/app/app.jar"]
