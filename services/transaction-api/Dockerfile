# ---- Stage 1: build the jar with Maven -------------------------------
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /build

# Copy only the pom first so Docker can cache the downloaded dependencies
# in their own layer -- they only get re-downloaded when pom.xml changes,
# not on every source code edit.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Stage 2: run the jar on a minimal JRE-only image -----------------
FROM eclipse-temurin:17-jre-alpine AS runtime

# Alpine's JRE image ships no default non-root user; create one so the
# process never runs as root inside the container. Numeric UID/GID (not
# just a named user) matters here: Kubernetes' securityContext.runAsNonRoot
# check (see the base Deployment manifests) can only verify statically from
# the image metadata that the default user is non-root when it's numeric --
# a named user fails that check with "cannot verify user is non-root".
RUN addgroup -g 1000 -S spring && adduser -u 1000 -S spring -G spring

WORKDIR /app
COPY --from=builder --chown=spring:spring /build/target/transaction-api.jar app.jar

USER 1000:1000
EXPOSE 8080

# This HEALTHCHECK is for local `docker run`/`docker compose` use only --
# Kubernetes ignores it and uses its own liveness/readiness probes instead
# (configured in the K8s Deployment manifest, added in a later phase).
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

# Plain exec-form entrypoint (no shell wrapper) so the JVM is PID 1 and
# receives SIGTERM directly from Kubernetes for a clean, fast shutdown.
# Extra JVM flags (e.g. heap sizing) can be injected at runtime via the
# JAVA_TOOL_OPTIONS env var without needing to touch this ENTRYPOINT.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
