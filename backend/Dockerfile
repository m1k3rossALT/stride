# ── Stage 1: Build ────────────────────────────────────────────────────────────
# Uses the full Maven + JDK image only during the build phase.
# Dependencies are downloaded first (separate layer) so rebuilds
# only re-download if pom.xml changes — not on every code change.
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Download dependencies first (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build the application
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Stage 2: Run ──────────────────────────────────────────────────────────────
# Minimal JRE-only Alpine image for the production container.
# Result: ~200MB image vs ~600MB if using the full JDK image.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user — never run the app as root
RUN addgroup -S tracker && adduser -S tracker -G tracker

# Create logs directory and give ownership to the app user
RUN mkdir -p /logs && chown tracker:tracker /logs

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Switch to non-root user
USER tracker

# Expose application port
EXPOSE 8080

# Health check — Docker will mark the container unhealthy if this fails
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
