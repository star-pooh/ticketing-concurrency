# Build stage
FROM gradle:8.8-jdk17 AS builder
WORKDIR /build
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY src src
RUN gradle build -x test

# Run stage
FROM eclipse-temurin:17-jdk-focal
WORKDIR /app
COPY --from=builder /build/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]