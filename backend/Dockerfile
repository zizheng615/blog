# 后端 Dockerfile - Spring Boot 2.7 + JDK 8
# 多阶段构建：先编译，再运行

# ===== 阶段1：构建 =====
FROM maven:3.8-eclipse-temurin-8 AS builder

WORKDIR /app

# 阿里云 Maven 镜像(国内拉依赖加速,避免在 ECS 上卡 Maven Central)
COPY maven-settings.xml /root/.m2/settings.xml

# 先复制 pom.xml 预热依赖,代码改动时不需要重新下载所有依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B -q || true

# 再复制源码并打包
COPY src ./src
RUN mvn clean package -DskipTests -q

# ===== 阶段2：运行 =====
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

# 健康检查(URL 用引号包,避免 & 被 shell 当后台运算符截断)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- "http://localhost:8080/api/v1/articles?page=1&size=1" > /dev/null || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
