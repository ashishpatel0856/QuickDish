# Stage 1: Build the app
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Make Maven wrapper executable (if using ./mvnw)
RUN chmod +x mvnw

# Build the app (skip tests)
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:17-jdk-slim

# Working directory
WORKDIR /app

# Copy built JAR
COPY --from=build /app/target/QuickDish-0.0.1-SNAPSHOT.jar app.jar

# Start app using Render's PORT
ENTRYPOINT ["sh", "-c", "java -Dserver.port=$PORT -jar app.jar"]
