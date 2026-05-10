# 后端 Dockerfile - Spring Boot 2.7 + JDK 8
# 多阶段构建：先编译，再运行

# 阶段1：构建
FROM maven:3.8-eclipse-temurin-8 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# 使用阿里云镜像加速构建（国内服务器）
RUN mvn clean package -DskipTests -q \
    -Dmaven.repo.local=/tmp/m2

# 阶段2：运行
FROM eclipse-temurin:8-jre-alpine

WORKDIR /app

# 从构建阶段复制 JAR
COPY --from=builder /app/target/blog-backend-1.0.0.jar app.jar

# 设置时区
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# JVM 参数优化（容器环境）
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/v1/articles?page=1&size=1 > /dev/null || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
