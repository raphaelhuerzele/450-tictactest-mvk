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

## Automatisierung

- `.github/workflows/ci.yml`: Tests + JaCoCo + 90-%-Quality-Gate + Coverage-Artifact.
- `.github/workflows/coverage-pages.yml`: Coverage-Zeitreihe und vollständiger JaCoCo-Report auf `gh-pages`.
- `.github/workflows/devcontainer-ci.yml`: Dev Container bauen, darin testen und als GHCR-Image pushen.

## Dev Container

In VS Code:

1. Docker starten.
2. Erweiterung **Dev Containers** installieren.
3. Repository öffnen.
4. `Dev Containers: Reopen in Container` ausführen.

Die Konfiguration liegt in `.devcontainer/devcontainer.json` und baut das Root-`Dockerfile`.

## GitHub Pages einmalig aktivieren

Nach dem ersten erfolgreichen Lauf von **Coverage time series** existiert der Branch `gh-pages`.

In GitHub unter **Settings → Pages**:

- Source: **Deploy from a branch**
- Branch: **gh-pages**
- Folder: **/(root)**

Danach ist die Seite normalerweise unter folgender Adresse erreichbar:

```text
https://raphaelhuerzele.github.io/450-tictactest-mvk/
```

## Dev-Container-Image

Der Workflow publiziert:

```text
ghcr.io/raphaelhuerzele/450-tictactest-mvk-devcontainer:latest
```
