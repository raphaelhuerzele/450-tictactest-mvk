# Testkonzept – TicTacTest

Projekt: 450-tictactest-mvk

Repository: raphaelhuerzele/450-tictactest-mvk

Stand: 09.09.2026

Bezugsstand: refaktorierte Tests aus Pull Request #1 (in main gemergt)

## 1. Zweck

Dieses Testkonzept dokumentiert den IST-Zustand der Tests: Testobjekt, Strategie, Testdaten, Automatisierung und Abdeckung. Es beschreibt vorhandene Tests; bestehende Testlücken sind separat aufgeführt.

## 2. Testobjekt

Das Projekt implementiert TicTacToe in Java mit einem eindimensionalen Spielfeld aus neun Feldern (Indizes 0 bis 8).

| Klasse | Aufgabe |
| --- | --- |
| `TicTacToeMain` | Spielstart, Ablauf über `play(...)`, Gewinnerkennung über `isWin(...)` und Spielfelddarstellung über `toString(...)` |
| `TicTacToePlayer` | Spielerschnittstelle mit `Stone.CROSS`, `Stone.CIRCLE` und `opponent()` zur Bestimmung des Gegenspielsteins |
| `GreedyPlayer` | Wählt das erste freie Feld von links oben nach rechts unten |
| `HumanPlayer` | Liest einen Spielzug über die Standardeingabe ein |

```text
0 | 1 | 2
--+---+--
3 | 4 | 5
--+---+--
6 | 7 | 8
```

## 3. Testumgebung und Automatisierung

Das Projekt verwendet Java und Gradle. Der Gradle Wrapper wird lokal und in der CI eingesetzt; die Testtask verwendet die JUnit Platform.

| Werkzeug | Version | Verwendung |
| --- | --- | --- |
| JUnit Jupiter | 5.11.4 | `@Test`, `@BeforeEach`, `@ParameterizedTest` und `@MethodSource` |
| AssertJ | 3.27.3 | Vergleich tatsächlicher und erwarteter Ergebnisse |
| JUnit Platform Launcher | 1.11.4 | Laufzeitunterstützung für die JUnit-Plattform |

GitHub Actions führt bei `push` und `pull_request` auf `ubuntu-latest` mit Java 25 (Zulu) den Befehl `./gradlew clean build --no-daemon` aus. Damit sind die Tests Teil des automatisierten Builds.

## 4. Teststrategie

Die automatisierten Tests konzentrieren sich auf deterministisch prüfbare Spiellogik auf Klassen- und Methodenebene:

- **Unit Tests:** Prüfen `Stone.opponent()`, `TicTacToeMain.isWin(...)` und `GreedyPlayer.play(...)` mit direkt vorbereiteten Eingaben und AssertJ-Assertions.
- **Parametrisierte Tests:** Führen dieselbe Testlogik für mehrere Board-Konstellationen aus. Die Datenquellen sind `winBoardFixtures()` und `greedyPlayerFixtures()`; beide werden über `@MethodSource` eingebunden.
- **Spielablauftest:** Prüft `TicTacToeMain.play(...)` mit zwei deterministischen Spielern als Lambdas. Die vorgegebenen Züge führen zu einem Sieg von CROSS. Dieser Test prüft mehrere Teile des Spielablaufs gemeinsam.

## 5. Teststruktur und Testdaten

Alle Tests liegen in `src/test/java/ch/bbw/m450/tictactoe/TicTacToeTest.java`.

### Fixtures

`@BeforeEach` erzeugt vor jedem Test einen neuen `GreedyPlayer`. Dadurch verwenden die Tests jeweils eine frische Instanz.

Die Datenquellen liefern `Stream<Arguments>`:

- `winBoardFixtures()`: zehn Konstellationen – drei horizontale, drei vertikale und zwei diagonale Gewinnlinien sowie zwei Prüfungen ohne Gewinn.
- `greedyPlayerFixtures()`: drei Belegungen zur Auswahl des ersten freien Feldes.

### Helper

`board(String layout)` wandelt genau neun Zeichen in ein `Stone[]` um: `X` steht für CROSS, `O` für CIRCLE und `.` für ein leeres Feld (`null`). Eine falsche Länge oder unbekannte Zeichen führen zu einer `IllegalArgumentException`.

Beispiel: `XXX.O.O..` entspricht folgendem Spielfeld:

```text
X X X
. O .
O . .
```

`playerWithMoves(int... moves)` erzeugt einen Spieler, der vorgegebene Positionen nacheinander zurückgibt. Im Spielablauftest spielt CROSS auf 0, 1, 2 und CIRCLE auf 3, 4.

Alle Testdaten stehen direkt im Testcode. Externe Dateien, Datenbanken oder Zufallsdaten werden nicht benötigt; die Tests sind reproduzierbar.

## 6. Testziele

| Testziel | Nachgewiesenes Verhalten |
| --- | --- |
| TZ-01 – Gegenspielstein | `CROSS.opponent()` ergibt CIRCLE und `CIRCLE.opponent()` ergibt CROSS |
| TZ-02 – Gewinnerkennung | `isWin(...)` erkennt alle acht Gewinnlinien und liefert für zwei Nicht-Gewinn-Konstellationen `false` |
| TZ-03 – GreedyPlayer | Wählt das erste freie Feld bei leerem Board, drei belegten Anfangsfeldern und nur einem freien letzten Feld |
| TZ-04 – Spielablauf | `play(...)` liefert bei der festgelegten Zugfolge `Stone.CROSS` als Gewinner |

## 7. Testfälle und Zuordnung

| Testfall | Testziel | Test / Fixture | Eingabe / Situation | Erwartung |
| --- | --- | --- | --- | --- |
| TC-01 | TZ-01 | `opponentShouldReturnTheOtherStone` | CROSS und CIRCLE | Jeweils anderer Stein |
| TC-02 | TZ-02 | `winBoardFixtures` | `XXX.O.O..`, CROSS | Gewinn = true |
| TC-03 | TZ-02 | `winBoardFixtures` | `O..XXX.O.`, CROSS | Gewinn = true |
| TC-04 | TZ-02 | `winBoardFixtures` | `O.O...XXX`, CROSS | Gewinn = true |
| TC-05 | TZ-02 | `winBoardFixtures` | `OXXO.XO.X`, CIRCLE | Gewinn = true |
| TC-06 | TZ-02 | `winBoardFixtures` | `XOXXO..O.`, CIRCLE | Gewinn = true |
| TC-07 | TZ-02 | `winBoardFixtures` | `XXO.XO..O`, CIRCLE | Gewinn = true |
| TC-08 | TZ-02 | `winBoardFixtures` | `OXX.OX..O`, CIRCLE | Gewinn = true |
| TC-09 | TZ-02 | `winBoardFixtures` | `O.X.X.XO.`, CROSS | Gewinn = true |
| TC-10 | TZ-02 | `winBoardFixtures` | `XOXOXOOX.`, CROSS | Gewinn = false |
| TC-11 | TZ-02 | `winBoardFixtures` | `XOXOXOOX.`, CIRCLE | Gewinn = false |
| TC-12 | TZ-03 | `greedyPlayerFixtures` | `.........` | Feld 0 |
| TC-13 | TZ-03 | `greedyPlayerFixtures` | `XOX......` | Feld 3 |
| TC-14 | TZ-03 | `greedyPlayerFixtures` | `XOXOXOXO.` | Feld 8 |
| TC-15 | TZ-04 | `crossPlayerShouldWinACompleteGame` | CROSS: 0,1,2; CIRCLE: 3,4 | Gewinner = CROSS |

Der Testbestand umfasst vier Testziele mit insgesamt 15 ausgeführten Testfall-Konstellationen.

## 8. Aktueller Abdeckungsstand

Automatisiert getestet sind der Gegenspielstein, alle acht Gewinnlinien, zwei Nicht-Gewinn-Konstellationen, der GreedyPlayer bei freien Feldern und ein vollständiges Spiel mit Sieg von CROSS.

Folgende Funktionen und Situationen sind bisher nicht durch automatisierte Tests abgedeckt:

- `TicTacToeMain.toString(...)`
- Vollständiges Unentschieden oder Spiel mit Sieg von CIRCLE
- Ungültiger Zug ausserhalb von 0–8 oder auf ein bereits belegtes Feld
- Identische Spielerinstanz für X und O
- GreedyPlayer bei vollständig belegtem Board
- `HumanPlayer.play(...)` / Konsoleneingabe
- Schutz vor Seiteneffekten durch die Board-Kopie in `play(...)`

## 9. Testausführung und Nachvollziehbarkeit

| Umgebung | Befehl |
| --- | --- |
| Linux/macOS | `./gradlew clean test` |
| Windows | `.\gradlew.bat clean test` |
| Vollständiger Build (Linux/macOS) | `./gradlew clean build` |
| GitHub Actions | `./gradlew clean build --no-daemon` |

Sprechende Methodennamen und benannte Parametersätze machen die Ergebnisse nachvollziehbar. JUnit zeigt beispielsweise `cross wins in top row`, `circle wins on main diagonal` oder `cross has no winning line` für die jeweilige Parameterkombination an. Die Zuordnung zu den Testzielen steht in Kapitel 7.
