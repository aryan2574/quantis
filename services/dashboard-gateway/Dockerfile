# ==================== DASHBOARD GATEWAY DOCKERFILE ====================
# Multi-stage build for optimized GraphQL Gateway Service

# Build stage
FROM maven:3.9.6-openjdk-21-slim AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM openjdk:21-jre-slim

WORKDIR /app

# Install necessary packages
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r quantis && useradd -r -g quantis quantis

# Copy built JAR
COPY --from=build /app/target/dashboard-gateway-*.jar app.jar

# Create logs directory
RUN mkdir -p /var/log && chown -R quantis:quantis /var/log

# Change ownership
RUN chown -R quantis:quantis /app

# Switch to non-root user
USER quantis

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8085/actuator/health || exit 1

# Expose port
EXPOSE 8085

# JVM optimization for GraphQL Gateway
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -Djava.security.egd=file:/dev/./urandom"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
