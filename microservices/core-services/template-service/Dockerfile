# 构建阶段
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

COPY common/build.gradle.kts common/
COPY core-services/template-service/build.gradle.kts core-services/template-service/

RUN ./gradlew dependencies --no-daemon --stacktrace || true

COPY common common
COPY core-services/template-service core-services/template-service

RUN ./gradlew :core-services:template-service:bootJar --no-daemon -x test

# 运行阶段
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Inventory Management Team"
LABEL service="template-service"
LABEL version="1.0.0"

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

RUN apk add --no-cache curl

COPY --from=builder /app/core-services/template-service/build/libs/*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV SPRING_PROFILES_ACTIVE="docker"

EXPOSE 8089

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8089/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
