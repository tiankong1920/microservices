FROM gradle:8.12-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :core-services:invoice-service:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/core-services/invoice-service/build/libs/*.jar app.jar
EXPOSE 8095
HEALTHCHECK --interval=30s --timeout=10s --retries=3 CMD wget -qO- http://localhost:8095/actuator/health/liveness || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
