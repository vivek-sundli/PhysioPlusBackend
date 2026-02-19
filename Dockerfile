# Stage 1: Build the application
FROM gradle:8.6-jdk21 AS builder
WORKDIR /app

# Copy the Gradle wrapper and build files first (for caching)
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./

# Grant execution rights to the wrapper
RUN chmod +x gradlew

# Download dependencies (this layer will be cached if dependencies don't change)
# We use 'go-offline' equivalent or just run a helping task, but here we just try a dry run or proceed.
# A simple way is to copy source and build.

COPY src/ src/

# Build the application, skipping tests to speed up deployment
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the port
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
