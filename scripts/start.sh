#!/bin/bash
APP_DIR="/home/ubuntu/backend"
JAR_NAME=$(ls $APP_DIR/*.jar | head -n 1)

cd $APP_DIR

nohup java -jar $JAR_NAME > /home/ubuntu/backend/nohup.out 2>&1 &
