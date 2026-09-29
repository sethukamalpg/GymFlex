# ==============================================================================
# Build Stage
# ==============================================================================
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy Maven descriptor and pre-fetch dependencies to leverage Docker layer cache
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy application source code
COPY src ./src

# Compile and package application (skip tests during container build)
RUN mvn clean package -DskipTests -B

# ==============================================================================
# Runtime Stage
# ==============================================================================
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Run as a dedicated non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy packaged jar from builder stage
COPY --from=builder --chown=spring:spring /build/target/*.jar /app/app.jar

# Switch to unprivileged user
USER spring:spring

# Default port (Render overrides PORT environment variable at runtime)
ENV PORT=8080

# JVM memory management tuned for cloud container environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

# Execute Spring Boot application passing the dynamic Render port
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar /app/app.jar"]
