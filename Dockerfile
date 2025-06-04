# ✅ 1단계: Gradle 빌드
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app
COPY . .
RUN ./gradlew clean bootJar

# ✅ 2단계: 실행 이미지
FROM eclipse-temurin:21-jdk

ENV APP_DIR=/app \
    LOG_DIR=/home/ec2-user/logs \
    LOG_FILE=/home/ec2-user/logs/docker.log \
    SIGNOZ_PATH="/home/ec2-user/otel-agent.jar"

WORKDIR $APP_DIR

COPY --from=builder /app/build/libs/*.jar app.jar
COPY scripts/entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]
