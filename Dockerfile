FROM eclipse-temurin:25-jdk-noble

LABEL org.opencontainers.image.source="https://github.com/raphaelhuerzele/450-tictactest-mvk"
LABEL org.opencontainers.image.description="Java 25 development container for the TicTacToe Gradle project"

RUN apt-get update \
    && apt-get install --yes --no-install-recommends git unzip curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --create-home --shell /bin/bash vscode

WORKDIR /workspace
USER vscode
