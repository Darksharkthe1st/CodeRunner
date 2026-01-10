#!/bin/sh
set -e

echo "Starting Docker daemon..."

# Start Docker daemon in background
dockerd > /var/log/dockerd.log 2>&1 &

DOCKER_PID=$!

echo "Docker daemon PID: $DOCKER_PID"
echo "Waiting for Docker daemon to be ready..."

# Wait up to 60 seconds for Docker to be ready
TIMEOUT=60
ELAPSED=0

while [ $ELAPSED -lt $TIMEOUT ]; do
    if docker info >/dev/null 2>&1; then
        echo "✓ Docker daemon is ready after ${ELAPSED}s!"
        break
    fi
    
    if [ $ELAPSED -eq 0 ]; then
        echo -n "Waiting"
    else
        echo -n "."
    fi
    
    sleep 1
    ELAPSED=$((ELAPSED + 1))
done

echo ""

if ! docker info >/dev/null 2>&1; then
    echo "✗ Docker daemon failed to start after ${TIMEOUT}s"
    echo "Docker daemon logs:"
    cat /var/log/dockerd.log
    exit 1
fi

# Pre-pull images for faster execution
echo "Pre-pulling Docker images..."
docker pull eclipse-temurin:21-alpine-3.23 || echo "Warning: Failed to pull openjdk image"
docker pull python:3.12-slim-bookworm || echo "Warning: Failed to pull python image"
docker pull gcc:13-bookworm || echo "Warning: Failed to pull gcc image"

echo "All images pulled successfully!"
echo "Starting Spring Boot application..."

# Start Spring Boot app
exec java -jar app.jar
