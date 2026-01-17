#!/bin/sh
set -e

echo "Verifying Docker connection..."
if ! docker info >/dev/null 2>&1; then
    echo "✗ Cannot connect to Docker daemon!"
    exit 1
fi
echo "✓ Docker is available!"   

echo "Pre-pulling docker images..."
docker pull eclipse-temurin:21-alpine
docker pull python:3.12-alpine
docker pull alpine:latest
echo "All images pulled successfully!"

echo "Starting Spring Boot application..."
exec java -jar app.jar
