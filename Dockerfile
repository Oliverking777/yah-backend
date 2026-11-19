# syntax=docker/dockerfile:1

# ============================================================
# Stage 1: Build
# ============================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first for dependency caching
COPY .mvn .mvn
COPY mvnw mvnw.cmd
COPY pom.xml .

# Download dependencies in a separate layer
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build the JAR
COPY src src
RUN ./mvnw package -B -DskipTests && \
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

# Expose the application port
EXPOSE 8080

# JVM optimizations for container environments
ENV JAVA_OPTS="-XX:+UseContainerSupport \
-XX:MaxRAMPercentage=70.0 \
-XX:InitialRAMPercentage=50.0 \
-XX:+UseG1GC \
-XX:+ExitOnOutOfMemoryError"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD ["sh", "-c", "exec java ${JAVA_OPTS} -jar app.jar"] || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar app.jar"]