#!/bin/bash

# 显示执行的命令
set -x

# 如果命令失败则退出
set -e

# 构建receiver模块
echo "正在构建receiver模块..."
mvn clean package -pl receiver -am -DskipTests

# 尝试预先拉取需要的镜像
echo "正在预先拉取基础镜像..."
docker pull openjdk:21-slim || echo "警告：无法拉取基础镜像，将在构建过程中尝试"
docker pull confluentinc/cp-zookeeper:7.0.0 || echo "警告：无法拉取zookeeper镜像，将在启动时尝试"
docker pull confluentinc/cp-kafka:7.0.0 || echo "警告：无法拉取kafka镜像，将在启动时尝试"
docker pull provectuslabs/kafka-ui:latest || echo "警告：无法拉取kafka-ui镜像，将在启动时尝试"

# 关闭可能已存在的容器
echo "正在关闭旧容器..."
docker-compose down || true

# 删除旧的容器和镜像缓存
echo "清理旧的容器和镜像..."
docker rm -f kafka zookeeper kafka-ui receiver 2>/dev/null || true

# 启动Docker Compose
echo "正在启动Docker环境..."
docker-compose up -d

# 检查服务是否启动成功
echo "正在检查服务启动状态..."
if docker ps | grep -q receiver; then
  echo "服务已启动成功!"
  echo "访问Kafka UI: http://localhost:8080"
  echo "访问Receiver API: http://localhost:8081"
  echo "测试接口: curl -X POST -H \"Content-Type: application/json\" -d '{\"adr\":{\"adr\":\"test-address\"},\"content\":\"Hello QuickTube!\"}' http://localhost:8081/message"
else
  echo "警告：服务可能未正常启动，请检查日志"
  docker-compose logs 