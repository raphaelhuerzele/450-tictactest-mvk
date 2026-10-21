# Abnahmenachweis: Release-Prozess für TicTacTest

Geprüft am 21.10.2026 mit Azul Java 25 und dem Gradle-Wrapper 9.7.0.
Die Aufgabe war im aktuellen GitHub-Stand bereits implementiert. Der veraltete lokale
Checkout wurde auf `main` aktualisiert; die folgenden Prüfungen wurden erneut ausgeführt.

## Anforderungen und Ergebnis

| Anforderung | Nachweis |
| --- | --- |
| Ausführbares, versioniertes JAR | Beide veröffentlichten JARs besitzen den Main-Class-Eintrag und starten mit `java -jar`. |
| MAJOR.MINOR.PATCH und Versionssprünge | In `RELEASE.md` beschrieben; Release-Version über `-PreleaseVersion` aus dem Tag. |
| Verständliches Changelog mit Datum | `CHANGELOG.md` enthält `1.0.0` und `1.0.1` vom 23.09.2026. |
| Bewusst ausgelöster Release | `.github/workflows/release.yml` reagiert ausschließlich auf `v*.*.*`-Tags und validiert das SemVer-Format. |
| Bestimmter Commit auf main | Beide Tags zeigen auf unterschiedliche Commits; beide sind Vorfahren von `origin/main`. |
| Automatisierte Tests und Build | Aktueller Stand: 23 Tests, 0 Fehler; JaCoCo-Zeilenabdeckung 93,48 %, Quality Gate 90 % bestanden. |
| Zwei GitHub Releases mit JAR | Beide Releases und ihre jeweiligen JAR-Assets sind verfügbar. |
| Download ohne Neubau starten | Beide Original-JARs erneut von GitHub heruntergeladen und jeweils eine komplette Partie erfolgreich gespielt. |
| Zweiter Release mit kleiner Änderung | `1.0.1` korrigiert den Eingabeprompt von „where to to put“ zu „where to put“. |
| Nachvollziehbare Anleitung | `RELEASE.md` beschreibt Voraussetzungen, Tag-Erstellung, Versionierung, Build, Downloads und Fehlerverhalten. |

## Veröffentlichte Releases

| Version | Commit | Erfolgreicher Release-Workflow |
| --- | --- | --- |
| [v1.0.0](https://github.com/raphaelhuerzele/450-tictactest-mvk/releases/tag/v1.0.0) | `ad2817d3ed597750701f613323d4c7fbea528e07` | [Workflow](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/35859594981) |
| [v1.0.1](https://github.com/raphaelhuerzele/450-tictactest-mvk/releases/tag/v1.0.1) | `f467445e56b1fe21ffba6d88cd038569952c36e9` | [Workflow](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/35860387348) |

Die SHA-256-Werte der Downloads stimmen jeweils mit der veröffentlichten `.sha256`-Datei überein:

```text
1.0.0  00f8c51be1c145adb411bdccaed4e72485b778eaa3044b12bd3be4da3816237e
1.0.1  7db47b0f09a8222754a689b1352a53d82599ca88ec805488a5ca3ef2aaa8dfd1
```

## Prüfungen wiederholen

JDK 25 muss als `JAVA_HOME` gesetzt sein. Build unter PowerShell:

```powershell
.\gradlew.bat clean check jar '-PreleaseVersion=1.0.1' --no-daemon
```

Erwartet: erfolgreicher Build einschließlich `jacocoTestCoverageVerification`.
Testbericht: `build/reports/tests/test/index.html`.
Coverage-Bericht: `build/reports/jacoco/test/html/index.html`.

Originalartefakte in jeweils einen neuen Ordner herunterladen:

```powershell
gh release download v1.0.0 --dir download-1.0.0
gh release download v1.0.1 --dir download-1.0.1
Get-FileHash download-1.0.0/tictactest-1.0.0.jar -Algorithm SHA256
Get-Content download-1.0.0/tictactest-1.0.0.jar.sha256
Get-FileHash download-1.0.1/tictactest-1.0.1.jar -Algorithm SHA256
Get-Content download-1.0.1/tictactest-1.0.1.jar.sha256
java -jar download-1.0.0/tictactest-1.0.0.jar
java -jar download-1.0.1/tictactest-1.0.1.jar
```

Bei jedem gestarteten Spiel nacheinander `4`, `8`, `2`, `6` eingeben.
Erwartet: `...and the winner is: CROSS` und Exit-Code 0. Für diese Prüfung
werden ausdrücklich die heruntergeladenen Dateien verwendet, ohne sie neu zu bauen.

Aus einem bestimmten Repository-Stand entsteht somit eine kontrollierte Veröffentlichung:
Ein annotierter Tag fixiert den Commit und die Version; der Workflow prüft diesen Stand,
baut und testet ihn und hängt das ausführbare JAR samt Prüfsumme an den zugehörigen
GitHub Release. Der erneute Downloadtest bestätigt die Verwendbarkeit des veröffentlichten Artefakts.
