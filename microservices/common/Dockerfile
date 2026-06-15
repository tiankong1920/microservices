# 多阶段构建Dockerfile
# 用于构建Inventory Management System - Common Library模块

# 第一阶段：构建阶段
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# 复制pom.xml和源代码
COPY pom.xml .
COPY src ./src

# 构建项目（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests -B

# 第二阶段：运行阶段
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 从构建阶段复制构建产物
COPY --from=builder /app/target/*.jar app.jar

# 创建非root用户运行应用
RUN addgroup -g 1000 appuser && \
    chown -R appuser:appuser /app

USER appuser

# 暴露应用端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseStringDeduplication"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 CMD curl -f http://localhost:8080/actuator/health || exit 1

# 运行应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]