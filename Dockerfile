# Use an OpenJDK base image
FROM openjdk:21-slim

# Install Ant
RUN apt-get update && \
    apt-get install -y ant && \
    rm -rf /var/lib/apt/lists/*
