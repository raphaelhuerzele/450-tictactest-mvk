FROM eclipse-temurin:25-jdk-alpine

LABEL org.opencontainers.image.source="https://github.com/raphaelhuerzele/450-tictactest-mvk"
LABEL org.opencontainers.image.description="Java 25 Alpine development container for TicTacTest"

RUN apk add --no-cache \
        bash \
        ca-certificates \
        curl \
        git \
        unzip \
    && addgroup -g 1000 vscode \
    && adduser -D -u 1000 -G vscode -s /bin/bash vscode \
    && printf 'export PATH="%s/bin:$PATH"\n' "$JAVA_HOME" > /etc/profile.d/java.sh

WORKDIR /workspace
USER vscode
