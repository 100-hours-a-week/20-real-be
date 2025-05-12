#!/bin/bash
APP_DIR="/home/ubuntu/backend"
JAR_NAME=$(ls $APP_DIR/*.jar | head -n 1)

PID=$(pgrep -f "$JAR_NAME")
if [ -n "$PID" ]; then
  kill -15 "$PID"
  sleep 5
fi
