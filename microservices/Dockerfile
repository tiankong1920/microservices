# 多阶段构建Dockerfile
FROM openjdk:21-slim-buster

# 设置工作目录
WORKDIR /app

# 复制依赖文件
COPY --from=builder CACHE /app/target/*.jar /app/ .

# 设置环境变量
ENV JAVA_OPTS="-Xmx2g -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=dev
ENV SPRING_DATASOURCE_URL=jdbc:h2:mem:inventory
ENV SPRING_DATASOURCE_USERNAME=sa
ENV SPRING_DATASOURCE_PASSWORD=password
ENV SPRING_DATASOURCE_DRIVER=org.h2.Driver

# 暴露端口
EXPOSE 8080

# 应用启动命令
CMD ["java", "-jar", "app.jar"]

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3