# QuickTube

QuickTube是一个基于Quarkus和Kafka的消息传输系统

## 项目结构

项目由三个模块组成

* common - 共享模型和工具类
* receiver - 消息接收服务提供REST API接收消息并发送到Kafka
* sender - 消息发送服务从Kafka读取消息并进行处理

## 技术栈

* Java 21
* Quarkus
* Apache Kafka
* Maven
* Docker

## 使用Docker快速启动

项目提供了Docker配置可以快速启动整个环境用于测试

### 使用自动构建脚本（推荐）

执行自动构建和部署脚本

```bash
# 给脚本添加执行权限（如果需要）
chmod +x build-and-run.sh

# 执行脚本
./build-and-run.sh
```

### 手动构建和部署

```bash
# 先编译receiver模块
mvn clean package -pl receiver -am -DskipTests

# 构建并启动所有服务
docker-compose up -d

# 查看服务日志
docker-compose logs -f
```

启动后可以通过以下地址访问服务

* Receiver API: http://localhost:8081
* Kafka UI管理界面: http://localhost:8080

## 测试接口

使用以下命令测试消息发送接口

```bash
curl -X POST -H "Content-Type: application/json" \
     -d '{"adr":{"adr":"test-address"},"content":"Hello QuickTube!"}' \
     http://localhost:8081/message
```

可以在Kafka UI管理界面中查看名为`quicktube_messages`的主题是否接收到了消息

## 自定义配置

可以通过环境变量修改Kafka连接地址

```bash
# 在docker-compose.yml中设置
environment:
  KAFKA_BOOTSTRAP_SERVERS: kafka:9092
```

## 从源码构建

```bash
# 编译整个项目
mvn clean package

# 仅编译receiver模块
mvn clean package -pl receiver -am
```

## 许可证

参见LICENSE文件