# Install Java on top of Docker image
FROM docker:24-dind as builder

# Install Java 21
RUN apk add --no-cache openjdk21 openjdk21-jdk maven

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk
ENV PATH="$JAVA_HOME/bin:$PATH"

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
FROM docker:24-dind

RUN apk add --no-cache openjdk21-jre

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk
ENV PATH="$JAVA_HOME/bin:$PATH"

WORKDIR /app

# Copy the built jar
COPY --from=0 /app/target/*.jar app.jar

# Copy the startup script, make it executable
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/docker-entrypoint.sh"]

