# ===========================================
# Stage 1: Build with Gradle
# ===========================================
FROM gradle:8-jdk17 AS build

WORKDIR /app

# Copy Gradle build files first for dependency caching
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build the application
RUN gradle bootJar --no-daemon

# ===========================================
# Stage 2: Runtime
# ===========================================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
