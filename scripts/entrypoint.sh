#!/bin/sh

set -e

mkdir -p /home/ec2-user/logs
touch /home/ec2-user/logs/docker.log

echo "[INFO] Starting Spring Boot with Scouter Agent..."
echo "Profile : $SPRING_PROFILE"
echo "Logging to : $LOG_FILE"
echo "SIGNOZ : $SIGNOZ_PATH"

# 🧠 환경에 따라 서비스 이름 분기
if [ "$SPRING_PROFILE" = "prod" ]; then
  OTEL_SERVICE_NAME="spring-prod"
else
  OTEL_SERVICE_NAME="spring-dev"
fi

exec java \
  -javaagent:"$SIGNOZ_PATH" \
  -Dotel.service.name=$OTEL_SERVICE_NAME \
  -Dotel.exporter.otlp.endpoint=http://43.200.4.253:4318 \
  -Dotel.instrumentation.spring-web.enabled=true \
  -Dotel.instrumentation.http.enabled=true \
  -Dotel.instrumentation.servlet.enabled=true \
  -Dotel.instrumentation.jdbc.enabled=true \
  -Duser.timezone=Asia/Seoul \
  -jar app.jar >> "$LOG_FILE" 2>&1
