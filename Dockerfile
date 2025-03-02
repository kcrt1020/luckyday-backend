# OpenJDK 17 사용
FROM openjdk:17
WORKDIR /app
COPY build/libs/luckyday-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/app.jar"]
