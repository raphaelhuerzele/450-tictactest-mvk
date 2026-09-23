# 450-tictactest-mvk

TicTacToe-Testprojekt für Modul 450 mit JUnit 5, parametrisierten Tests, Fixtures/Helpers, JaCoCo, GitHub Actions, Coverage-Zeitreihe auf GitHub Pages und Dev Container.

## Lokal testen

```bash
chmod +x ./gradlew
./gradlew clean check
```

Nur Tests und Coverage-Report:

```bash
./gradlew clean test jacocoTestReport
```

Der HTML-Report liegt unter:

```text
build/reports/jacoco/test/html/index.html
```

Das Testkonzept ist in [`TESTKONZEPT.md`](TESTKONZEPT.md) dokumentiert.

Der Anwendungs-Releaseprozess ist in [`RELEASE.md`](RELEASE.md) beschrieben; die Änderungen der Versionen stehen in [`CHANGELOG.md`](CHANGELOG.md).

Ein ausführbares JAR kann lokal so erstellt und gestartet werden:

```bash
./gradlew clean check jar -PreleaseVersion=1.0.0
java -jar build/libs/tictactest-1.0.0.jar
```

## Automatisierung

- `.github/workflows/ci.yml`: Tests + JaCoCo + 90-%-Quality-Gate + Coverage-Artifact.
- `.github/workflows/coverage-pages.yml`: Coverage-Zeitreihe und vollständiger JaCoCo-Report auf `gh-pages`.
- `.github/workflows/release.yml`: Getaggte Anwendungsversion testen und als GitHub Release mit ausführbarem JAR veröffentlichen.
- `.github/workflows/devcontainer-ci.yml`: Tests im zuletzt freigegebenen Dev-Container-Image.
- `.github/workflows/devcontainer-release.yml`: Versionierten Dev Container bauen, testen, veröffentlichen und einen Update-PR erstellen.

## Dev Container

In VS Code:

1. Docker starten.
2. Erweiterung **Dev Containers** installieren.
3. Repository öffnen.
4. `Dev Containers: Reopen in Container` ausführen.

Die Container-Definition liegt in `.devcontainer/Dockerfile` und verwendet Alpine Linux, Java 25 sowie den Benutzer `vscode` mit UID/GID `1000:1000`. Nach dem ersten Release verwendet `.devcontainer/devcontainer.json` einen exakt versionierten, freigegebenen GHCR-Tag. Der vollständige Versionierungs- und Freigabeprozess ist in [`DEVCONTAINER_RELEASE.md`](DEVCONTAINER_RELEASE.md) dokumentiert.

## Coverage auf GitHub Pages

Die GitHub-Pages-Veröffentlichung ist für den Branch `gh-pages` aktiviert. Der Workflow **Coverage time series** ergänzt bei jedem Push auf `main` die Messreihe und veröffentlicht den vollständigen JaCoCo-Report:

https://raphaelhuerzele.github.io/450-tictactest-mvk/

## Dev-Container-Image

Freigegebene Versionen werden unter folgendem Namen publiziert:

```text
ghcr.io/raphaelhuerzele/450-tictactest-mvk-devcontainer:vMAJOR.MINOR.PATCH
```
