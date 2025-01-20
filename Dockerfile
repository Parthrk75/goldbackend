# Use an OpenJDK base image
FROM openjdk:17-jdk-slim

# Set a working directory inside the container
WORKDIR /app

# Copy the Spring Boot application JAR file to the container
# Replace `gold-price-management.jar` with the name of your JAR file
COPY target/Gold_Price_Management-3.3.1.jar app.jar

# Expose the application port (update this to your app's port if different)
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
