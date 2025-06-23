FROM openjdk:21-slim
WORKDIR /app

# 直接复制本地构建的jar包和依赖
COPY target/quarkus-app/lib/ /app/lib/
COPY target/quarkus-app/*.jar /app/
COPY target/quarkus-app/app/ /app/app/
COPY target/quarkus-app/quarkus/ /app/quarkus/

# 设置环境变量
ENV KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "/app/quarkus-run.jar"] 