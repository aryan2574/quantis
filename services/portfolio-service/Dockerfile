# ==================== BUILD STAGE ====================
FROM eclipse-temurin:21-jdk AS build

# Set working directory
WORKDIR /workspace

# Copy Maven wrapper and pom.xml first (for better layer caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application
RUN ./mvnw clean package -DskipTests -B

# ==================== RUNTIME STAGE ====================
FROM eclipse-temurin:21-jre-alpine

# Create non-root user for security
RUN addgroup -g 1001 -S quantis && \
    adduser -u 1001 -S quantis -G quantis

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /workspace/target/portfolio-service-0.0.1-SNAPSHOT.jar app.jar

# Change ownership to non-root user
RUN chown quantis:quantis app.jar

# Switch to non-root user
USER quantis

# Expose ports (HTTP and gRPC)
EXPOSE 8082 9090

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
