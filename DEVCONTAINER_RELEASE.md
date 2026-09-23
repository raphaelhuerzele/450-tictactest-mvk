# Continuous Deployment des Dev Containers

## Ziel und Ablauf

Der Dev Container wird als versioniertes Image in der GitHub Container Registry (GHCR) veröffentlicht. CI und lokale Entwicklungsumgebungen verwenden immer dieselbe, zuletzt freigegebene Version.

```text
Änderung an .devcontainer/Dockerfile
        ↓
Review und Merge nach main
        ↓
Release-Tag devcontainer-vX.Y.Z
        ↓
Build → Tests → Push nach GHCR
        ↓
automatischer Pull Request mit neuer Image-Version
        ↓
Merge → CI und lokale Dev Container verwenden die neue Version
```

## Dateien und Verantwortlichkeiten

| Datei | Aufgabe |
|---|---|
| `.devcontainer/Dockerfile` | Alpine-basierter Inhalt des Dev-Container-Images |
| `.devcontainer/release/devcontainer.json` | Build-Konfiguration für ein Release |
| `.devcontainer/devcontainer.json` | Von VS Code und CI verwendete, freigegebene Image-Version |
| `.devcontainer/VERSION` | Aktuell im Repository verwendete Version |
| `.github/workflows/devcontainer-release.yml` | Baut, testet, veröffentlicht und erstellt den Update-PR |
| `.github/workflows/devcontainer-ci.yml` | Führt die Tests im freigegebenen Image aus; veröffentlicht keine Images |
| `scripts/update-devcontainer-version.sh` | Aktualisiert Konfiguration und Versionsdatei im Release-PR |

## Versionierungskonzept

Der Dev Container verwendet [Semantic Versioning](https://semver.org/) im Format `vMAJOR.MINOR.PATCH`:

- **MAJOR**, wenn eine inkompatible Änderung der Entwicklungsumgebung erfolgt, beispielsweise eine neue Java-Hauptversion.
- **MINOR**, wenn Werkzeuge oder kompatible Funktionen ergänzt werden.
- **PATCH**, wenn Pakete aktualisiert, Sicherheitskorrekturen eingebaut oder kleine Fehler behoben werden.

Ein Git-Release-Tag trägt zur eindeutigen Abgrenzung den Präfix `devcontainer-`, beispielsweise `devcontainer-v1.0.1`. Das veröffentlichte Container-Image erhält diese Tags:

```text
ghcr.io/raphaelhuerzele/450-tictactest-mvk-devcontainer:v1.0.1
ghcr.io/raphaelhuerzele/450-tictactest-mvk-devcontainer:v1.0
ghcr.io/raphaelhuerzele/450-tictactest-mvk-devcontainer:v1
ghcr.io/raphaelhuerzele/450-tictactest-mvk-devcontainer:stable
```

CI und lokale Entwicklung verwenden immer den unveränderlichen vollständigen Tag `vMAJOR.MINOR.PATCH`. Die beweglichen Tags dienen nur zur Orientierung und als Build-Cache. `latest` wird bewusst nicht verwendet.

## Schutz vor nicht freigegebenen Images

Die Release-Pipeline setzt folgende Regeln technisch durch:

1. Normale Branch- und Pull-Request-Builds haben nur Leserechte auf Packages und führen `push: never` aus.
2. Nur ein Tag im exakten Format `devcontainer-vMAJOR.MINOR.PATCH` startet die Veröffentlichung.
3. Der Tag muss auf einen Commit zeigen, der Bestandteil von `main` ist.
4. Vor dem Push wird geprüft, dass der vollständige Image-Tag noch nicht existiert. Freigegebene Versionen werden dadurch nicht überschrieben.
5. Das Image wird vor dem Push mit `./gradlew clean check` getestet. Ein Fehler verhindert die Veröffentlichung und den Update-PR.
6. Erst nach erfolgreichem Registry-Push erstellt der Workflow einen Pull Request für die neue Version.
7. CI und lokale Konfiguration nutzen einen exakten Versionstag. Ein noch nicht freigegebenes Image kann deshalb nicht versehentlich über einen beweglichen Tag übernommen werden.

Der Release-Tag ist die bewusste Freigabeentscheidung. Schreibrechte für Tags und `main` sollten deshalb nur Maintainer besitzen. Optional kann für besonders strenge Projekte zusätzlich ein geschütztes GitHub-Environment mit Reviewern vor den Release-Job geschaltet werden.

## Release durchführen

Zuerst werden Änderungen an `.devcontainer/Dockerfile` und `.devcontainer/release/devcontainer.json` per Pull Request geprüft und nach `main` gemergt. Danach wird abhängig von der Änderung die nächste SemVer-Version gewählt.

Beispiel für Release `v1.0.1`:

```bash
git switch main
git pull --ff-only
git tag -a devcontainer-v1.0.1 -m "Release Dev Container v1.0.1"
git push origin devcontainer-v1.0.1
```

Der Workflow **Release Dev Container** erledigt anschließend automatisch:

1. Tag und Zugehörigkeit zu `main` prüfen.
2. Prüfen, dass `v1.0.1` in GHCR noch nicht existiert.
3. Dev Container bauen und den vollständigen Gradle-Check darin ausführen.
4. Versionierte Tags und `stable` nach GHCR pushen.
5. Einen Branch `chore/use-devcontainer-v1.0.1` erstellen.
6. `.devcontainer/devcontainer.json` und `.devcontainer/VERSION` aktualisieren.
7. Einen Pull Request gegen `main` erstellen.

Nach Review und Merge dieses Pull Requests verwenden der Workflow **Dev Container CI** und alle lokalen VS-Code-Dev-Container automatisch die neue freigegebene Version.

## Lokale Verwendung

Das Repository in VS Code öffnen und **Dev Containers: Reopen in Container** ausführen. Die Erweiterung liest `.devcontainer/devcontainer.json` und lädt den dort eingetragenen GHCR-Tag. Nach dem Merge eines Versions-PRs führt **Dev Containers: Rebuild Container** zur neuen Version.

Die aktuell konfigurierte Version steht zusätzlich in `.devcontainer/VERSION`.

## Rollback

Ein Rollback verändert oder überschreibt kein veröffentlichtes Image. Stattdessen wird per Pull Request der letzte bekannte, funktionierende vollständige Tag in `.devcontainer/devcontainer.json` und `.devcontainer/VERSION` wieder eingetragen. Dadurch bleibt jede verwendete Umgebung reproduzierbar.
