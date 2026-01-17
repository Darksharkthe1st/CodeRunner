# Install Java on top of Docker image
FROM eclipse-temurin:21-alpine as builder

WORKDIR /app

# Copy Maven Wrapper and build
COPY ./mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Build the application
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# Application running stage
FROM eclipse-temurin:21-alpine
WORKDIR /app

# Copy the built jar
COPY --from=0 /app/target/*.jar app.jar

# Copy the startup script, make it executable
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
RUN apk add --no-cache docker-cli

EXPOSE 8080

ENTRYPOINT ["/docker-entrypoint.sh"]

