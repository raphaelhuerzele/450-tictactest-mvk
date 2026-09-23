# Release-Prozess für TicTacTest

## Überblick

Ein Release entsteht nur durch einen bewusst erstellten Git-Tag im Format `vMAJOR.MINOR.PATCH`. Ein normaler Push nach `main` startet kein Release. Dadurch ist jede veröffentlichte Version genau einem geprüften Commit zugeordnet.

```text
Änderungen und CHANGELOG nach main mergen
                    ↓
lokal Tests und JAR prüfen
                    ↓
annotierten Tag vMAJOR.MINOR.PATCH erstellen
                    ↓
Tag nach GitHub pushen
                    ↓
GitHub Actions: Tests → Build → JAR-Starttest
                    ↓
GitHub Release mit JAR und SHA-256-Prüfsumme
```

## Versionsnummer

TicTacTest verwendet Semantic Versioning:

- **MAJOR** für inkompatible Änderungen an Bedienung, Daten oder öffentlichen Schnittstellen.
- **MINOR** für neue, rückwärtskompatible Funktionen.
- **PATCH** für rückwärtskompatible Fehler- und Sicherheitskorrekturen.

Die Git-Tags tragen ein führendes `v`, beispielsweise `v1.0.0`. Gradle erhält die Versionsnummer beim Release über `-PreleaseVersion=1.0.0` und erzeugt `build/libs/tictactest-1.0.0.jar`.

## Voraussetzungen

Vor dem Release müssen alle folgenden Bedingungen erfüllt sein:

1. Alle vorgesehenen Änderungen sind per Pull Request nach `main` gemergt.
2. Der lokale Branch entspricht dem aktuellen `origin/main`.
3. `./gradlew clean check jar` ist erfolgreich.
4. `CHANGELOG.md` enthält einen verständlichen Abschnitt für die neue Version mit Datum, neuen Funktionen, Änderungen und Fehlerkorrekturen.
5. Die Versionsnummer ist noch nicht als Git-Tag oder GitHub Release vorhanden.
6. Die gewählte MAJOR-, MINOR- oder PATCH-Erhöhung entspricht der Art der Änderung.

## Release erstellen

Beispiel für Version `1.0.1`:

```bash
git switch main
git pull --ff-only origin main
./gradlew clean check jar -PreleaseVersion=1.0.1
git tag -a v1.0.1 -m "Release v1.0.1"
git push origin v1.0.1
```

Der Tag zeigt auf den aktuellen `main`-Commit. Der Workflow `.github/workflows/release.yml` prüft zusätzlich, dass der Tag exakt dem stabilen SemVer-Format entspricht und sein Commit Bestandteil von `main` ist.

## Automatischer Workflow

Nach dem Tag-Push führt **Release TicTacTest** folgende Schritte aus:

1. Repository vollständig auschecken.
2. Tag, Commit und passenden Changelog-Abschnitt prüfen.
3. Java 25 und Gradle einrichten.
4. Tests und 90-Prozent-Coverage-Gate ausführen.
5. Das versionierte ausführbare JAR bauen.
6. Das JAR mit einer vollständigen Beispielpartie starten.
7. Eine SHA-256-Prüfsumme erzeugen.
8. Ein GitHub Release erstellen und JAR sowie Prüfsumme anhängen.

Schlägt ein Test, der Build, der JAR-Starttest oder die Changelog-Prüfung fehl, endet der Workflow vor der Veröffentlichung. Es entsteht kein GitHub Release.

## Download und Kontrolle

Alle Releases stehen unter folgender Adresse bereit:

https://github.com/raphaelhuerzele/450-tictactest-mvk/releases

Das JAR kann ohne Quellcode gestartet werden:

```bash
java -jar tictactest-1.0.1.jar
```

Zur Kontrolle wird die veröffentlichte Datei heruntergeladen, die SHA-256-Prüfsumme verglichen und das JAR nochmals gestartet. Tag, GitHub Release und JAR-Dateiname enthalten dieselbe Versionsnummer; die Release-Seite zeigt außerdem den zugehörigen Commit und die aus `CHANGELOG.md` übernommenen Änderungen.

## Changelog

`CHANGELOG.md` beschreibt Änderungen aus Sicht von Benutzern und Entwicklern. Jeder Release-Abschnitt enthält Versionsnummer, Datum und – sofern vorhanden – Einträge unter `Added`, `Changed` und `Fixed`. Eine reine Liste von Git-Commits genügt nicht.
