# Stage 1: Build the application with Maven using JDK 21
FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /workspace
COPY . .

# Add execute permission to the Maven wrapper script
RUN chmod +x ./mvnw

# Run the build
RUN ./mvnw clean package -DskipTests

# Stage 2: Create a minimal final image using JRE 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built JAR from the builder stage
# !!! IMPORTANT: Change this name if your pom.xml artifactId is different !!!
COPY --from=builder /workspace/target/currency-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]