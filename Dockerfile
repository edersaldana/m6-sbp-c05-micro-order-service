# Dockerfile for User Service
# Use Eclipse Temurin JRE 21 as the base image
FROM eclipse-temurin:17-jre

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file from the target directory to the container
COPY target/*.jar /app/order-service.jar

# Expose port 8083 for the Order Service
EXPOSE 8084

# Define the command to run the Order Service
CMD ["java", "-jar", "/app/order-service.jar"]