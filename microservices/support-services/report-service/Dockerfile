FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

COPY common/build.gradle.kts common/
COPY support-services/report-service/build.gradle.kts support-services/report-service/

RUN ./gradlew dependencies --no-daemon --stacktrace || true

COPY common common
COPY support-services/report-service support-services/report-service

RUN ./gradlew :support-services:report-service:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Inventory Management Team"
LABEL service="report-service"
LABEL version="1.0.0"

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

RUN apk add --no-cache curl

COPY --from=builder /app/support-services/report-service/build/libs/*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV SPRING_PROFILES_ACTIVE="docker"

EXPOSE 8094

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8094/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
