# ---- Stage 1: build the jar with Maven -------------------------------
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Stage 2: run the jar on a minimal JRE-only image -----------------
FROM eclipse-temurin:17-jre-alpine AS runtime

# Numeric UID/GID so Kubernetes' runAsNonRoot check can verify statically
# from image metadata -- a named user fails that check (see transaction-api's
# Dockerfile for the full explanation).
RUN addgroup -g 1000 -S spring && adduser -u 1000 -S spring -G spring

WORKDIR /app
COPY --from=builder --chown=spring:spring /build/target/settlement-worker.jar app.jar

USER 1000:1000
EXPOSE 8080

# Only meaningful when run in "scheduled" mode (docker compose). When run as
# a K8s CronJob in "oneshot" mode the process exits right after settling, so
# this HEALTHCHECK simply never gets a chance to fire there -- fine, since
# K8s Jobs are graded on exit code, not on being "healthy".
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
