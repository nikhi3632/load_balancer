# % javac --version
# javac 21
# % java --version
# openjdk 21 2023-09-19
# OpenJDK Runtime Environment Homebrew (build 21)
# OpenJDK 64-Bit Server VM Homebrew (build 21, mixed mode, sharing)

# Use an OpenJDK 21 base image
FROM openjdk:21-slim

# Install Ant
RUN apt-get update && \
    apt-get install -y ant && \
    rm -rf /var/lib/apt/lists/*
