#!/bin/bash
docker stop spring-app || true
docker rm spring-app || true

aws ecr get-login-password --region ap-northeast-2 | \
docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.ap-northeast-2.amazonaws.com

docker pull <AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com/<ECR_REPOSITORY>:<IMAGE_TAG>
docker run -d --name spring-app -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=<ENV> \
  -v /home/ec2-user/logs:/home/ec2-user/logs \
  -v /home/ec2-user/otel-agent.jar:/home/ec2-user/otel-agent.jar \
  <AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com/<ECR_REPOSITORY>:<IMAGE_TAG>
