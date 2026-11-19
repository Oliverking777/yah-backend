# syntax=docker/dockerfile:1

# ============================================================
# Stage 1: Build
# ============================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml first for dependency caching (uses Maven from the image, no mvnw needed)
COPY pom.xml .

# Download dependencies in a separate layer
RUN mvn dependency:go-offline -B

# Copy source code and build the JAR
COPY src src
RUN mvn package -B -DskipTests && \
    mv target/*.jar app.jar

# ============================================================
# Stage 2: Runtime
# ============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built JAR from the builder stage
COPY --from=builder /app/app.jar app.jar

# Set ownership and switch to non-root user
RUN chown appuser:appgroup app.jar
USER appuser:appgroup

# Local default. On Render the app listens on the PORT variable it injects.
EXPOSE 8080

# JVM options for container environments
ENV JAVA_OPTS="-XX:+UseContainerSupport \
-XX:MaxRAMPercentage=70.0 \
-XX:InitialRAMPercentage=50.0 \
-XX:+UseG1GC \
-XX:+ExitOnOutOfMemoryError"

# Health check (Render ignores this and uses its own Health Check Path)
HEALTHCHECK --interval=30s --timeout=3s --start-period=90s --retries=3 \
    CMD wget -qO- http://localhost:${PORT:-8080}/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar app.jar"]