# Build stage
FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B || true
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy entire target directory, then find and copy the JAR
COPY --from=builder /app/target/ /tmp/target/
RUN find /tmp/target -name "*.jar" ! -name "*-sources.jar" ! -name "*-javadoc.jar" -exec cp {} app.jar \; || \
    (echo "ERROR: No JAR file found!" && ls -la /tmp/target/ && exit 1)

# Add healthcheck
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8082/actuator/health || exit 1

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
