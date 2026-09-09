FROM eclipse-temurin:25-jdk-noble

LABEL org.opencontainers.image.source="https://github.com/raphaelhuerzele/450-tictactest-mvk"
LABEL org.opencontainers.image.description="Java 25 CI image for the Tic-Tac-Toe Gradle project"

RUN apt-get update \
    && apt-get install --yes --no-install-recommends git unzip \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace
