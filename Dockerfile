# Use a base image for the runtime environment (Java 17)
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the pre-built .jar file from the host system (assuming it is in the target directory)
COPY target/Gold_Price_Management-3.3.1.jar app.jar

# Expose the application port (8080 is commonly used for Spring Boot applications)
EXPOSE 8080

# Specify the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
