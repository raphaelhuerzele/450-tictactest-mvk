# Testkonzept – TicTacTest

## 1. Ziel

Das Projekt wird automatisiert auf korrekte Spiellogik, gültige Spielerzüge und stabile Randfallbehandlung geprüft. Die Tests sollen schnell, reproduzierbar und ohne manuelle Eingaben laufen. Zusätzlich wird die Testabdeckung mit JaCoCo gemessen und in CI sowie als Zeitreihe auf GitHub Pages veröffentlicht.

## 2. Teststrategie

Verwendet werden Unit-Tests mit JUnit 5 und AssertJ. Wiederkehrende Testdaten sind als benannte Fixtures in `TicTacToeFixtures` abgelegt. Für deterministische Spielabläufe dient `QueuePlayer` als Test-Helper/Test-Double. Gleichartige Fälle, insbesondere alle acht möglichen Gewinnlinien, werden als parametrisierte Tests ausgeführt.

Die Tests folgen soweit sinnvoll dem Given-When-Then-Prinzip:

- **Given:** Ausgangslage, Fixture oder Test-Double wird vorbereitet.
- **When:** Die zu prüfende Methode wird ausgeführt.
- **Then:** Das Ergebnis oder die erwartete Exception wird geprüft.

## 3. Testobjekte

| Bereich | Was wird geprüft? |
|---|---|
| `Stone` | Gegenspieler von CROSS/CIRCLE |
| `TicTacToeMain.isWin` | 3 Reihen, 3 Spalten, 2 Diagonalen und kein Gewinner |
| `TicTacToeMain.play` | Sieg CROSS, Sieg CIRCLE, Unentschieden, ungültige Züge |
| `TicTacToeMain.toString` | Darstellung von X, O und freien Feldern |
| `GreedyPlayer` | erstes freies Feld und volles Brett |
| `HumanPlayer` | Einlesen einer gültigen Position aus `System.in` |

## 4. Fixtures und Helper

`src/test/java/ch/bbw/m450/tictactoe/TicTacToeFixtures.java` enthält wiederverwendbare Boards:

- `ROW_WIN_CROSS`
- `DIAGONAL_WIN_CIRCLE`
- `FULL_BOARD_DRAW`

Die Helper-Methode `boardWithWinningLine(...)` erzeugt für parametrisierte Tests gezielt ein Board mit einer Gewinnlinie.

`src/test/java/ch/bbw/m450/tictactoe/QueuePlayer.java` ist ein deterministisches Test-Double. Es liefert eine vorgegebene Folge von Zügen und ermöglicht damit vollständige Spiele ohne Benutzereingabe.

## 5. Parametrisierte Tests

`isWinDetectsAllWinningLines(...)` verwendet `@ParameterizedTest` und `@MethodSource`. Damit werden alle acht Gewinnlinien mit derselben Testlogik geprüft:

1. obere Reihe
2. mittlere Reihe
3. untere Reihe
4. linke Spalte
5. mittlere Spalte
6. rechte Spalte
7. Hauptdiagonale
8. Gegendiagonale

`opponentReturnsOtherStone(...)` verwendet `@CsvSource` für beide Enum-Werte.

## 6. Testfälle und Erwartungen

| ID | Given | When | Then |
|---|---|---|---|
| T01 | CROSS oder CIRCLE | `opponent()` | jeweils anderer Stein |
| T02 | Board mit einer der 8 Gewinnlinien | `isWin` | `true` für Gewinner |
| T03 | volles Draw-Board | `isWin` | `false` für beide Farben |
| T04 | teilweise belegtes Board | `GreedyPlayer.play` | erstes freies Feld |
| T05 | volles Board | `GreedyPlayer.play` | `IllegalStateException` |
| T06 | gleiche Player-Instanz zweimal | `play` | `IllegalArgumentException` |
| T07 | Position kleiner als 0 | `play` | `IllegalStateException` |
| T08 | bereits belegte Position | `play` | `IllegalStateException` |
| T09 | deterministische CROSS-Züge | vollständiges `play` | CROSS gewinnt |
| T10 | deterministische CIRCLE-Züge | vollständiges `play` | CIRCLE gewinnt |
| T11 | deterministisches volles Board | vollständiges `play` | Rückgabewert `null` |
| T12 | Board mit X/O/leeren Feldern | `toString` | Ausgabe enthält X, O und Index |
| T13 | Eingabe `4` | `HumanPlayer.play` | Position 4 wird zurückgegeben |

## 7. Coverage

JaCoCo wird über das Gradle-Plugin ausgeführt. Erzeugt werden XML-, HTML- und CSV-Reports unter `build/reports/jacoco/test/`.

Lokal:

```bash
./gradlew clean test jacocoTestReport
```

Der HTML-Report liegt danach unter:

```text
build/reports/jacoco/test/html/index.html
```

`./gradlew check` enthält zusätzlich ein Quality Gate von mindestens **90 % Line Coverage**.

## 8. CI

`.github/workflows/ci.yml` führt bei Pushes und Pull Requests die Tests, JaCoCo und das Quality Gate aus. Der HTML-Report wird als GitHub-Actions-Artifact hochgeladen und die aktuelle Coverage im Workflow Summary angezeigt.

## 9. Coverage-Zeitreihe / GitHub Pages

`.github/workflows/coverage-pages.yml` läuft auf `main`, liest die aktuelle Coverage aus dem JaCoCo-XML, ergänzt `coverage.csv` um Datum, Commit und Prozentwert und veröffentlicht die Seite auf dem Branch `gh-pages`.

Die Darstellung liegt in `pages/index.html` und zeigt:

- aktuelle Coverage,
- Änderung gegenüber der vorherigen Messung,
- Status des 90-%-Quality-Gates,
- Coverage-Verlauf,
- Tabelle aller Messungen,
- Link zum vollständigen JaCoCo-HTML-Report.

## 10. Dev Container

`.devcontainer/devcontainer.json` verweist auf einen exakt versionierten und freigegebenen Dev Container aus GHCR. Der Workflow `.github/workflows/devcontainer-ci.yml` führt `./gradlew clean check` in genau diesem Image aus und darf selbst keine Images veröffentlichen.

Releases werden ausschließlich durch `.github/workflows/devcontainer-release.yml` erzeugt. Der Workflow akzeptiert stabile SemVer-Tags, baut und testet das Image und pusht es erst danach nach GHCR. Anschließend erstellt er automatisch einen Pull Request, der CI und lokale Entwicklung auf die neue unveränderliche Version aktualisiert. Der Prozess ist in `DEVCONTAINER_RELEASE.md` dokumentiert.

## 11. Anwendungs-Releases

Der Workflow `.github/workflows/release.yml` startet nur bei stabilen Tags wie `v1.0.0`. Er wiederholt alle Tests und das Coverage-Gate, baut das ausführbare JAR, startet damit eine vollständige Beispielpartie und veröffentlicht es zusammen mit einer SHA-256-Prüfsumme als GitHub Release. Der reproduzierbare Ablauf und seine Voraussetzungen stehen in `RELEASE.md`; benutzerrelevante Änderungen stehen in `CHANGELOG.md`.
