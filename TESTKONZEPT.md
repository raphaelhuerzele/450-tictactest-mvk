# Testkonzept – TicTacTest

## Ziel und Abgrenzung

Die ursprüngliche Spiellogik und das Verhalten der vorhandenen Spieler bleiben unverändert; hinzu kommen lediglich begründete Sonar-Ausnahmen für Konsolenausgabe und die feste main-Signatur. Ergänzt wird ein optimaler `PerfectPlayer`. Tests prüfen normale Spiele, Grenzfälle, Konsoleninteraktion, unveränderte Eingabebretter und optimale Spielentscheidungen. Mockingframeworks werden nicht verwendet.

## Werkzeuge

| Werkzeug | Stabile Version | Verwendung |
| --- | --- | --- |
| JUnit Jupiter / Platform | 6.1.3 | Unit-Tests, MethodSource, CsvSource, EnumSource, ValueSource |
| AssertJ | 3.27.7 | Fluent Assertions auf Ergebnisse, Arrays, Ausgaben und Exceptions |
| JUnit Pioneer | 2.3.0 | `@StdIo` und `StdOut` für isolierte Ein-/Ausgabetests |
| QuickTheories | 0.26 | Zusätzliches Testingframework: generierte Spielbretter und Properties |
| JaCoCo | 0.8.15 | HTML-, XML-, CSV-Reports und mindestens 90 % Branch-Coverage |
| Gradle-PITest-Plugin | 1.19.0 | Automatisiertes Mutation Testing |
| PITest / JUnit-Adapter | 1.30.1 / 1.2.3 | Mutation der Produktionsklassen und Ausführung der Jupiter-Tests |
| SonarLint Core / SonarJava | 12.2.0.87867 / 8.45.0.59462 | Statische Prüfung aller Java-Produktions- und Testdateien |

Versionen wurden am 25.10.2026 anhand von Maven Central und dem Gradle Plugin Portal geprüft. Milestone- und RC-Versionen werden nicht als stabile Releases verwendet. JUnit 6 behält die Jupiter-API und deren Paketnamen bei; die Tests verwenden weiterhin `org.junit.jupiter`.

## Testdaten und Struktur

`TicTacToeFixtures` stellt ein volles Unentschieden-Brett und eine Factory für Gewinnlinien bereit. `QueuePlayer` ist ein handgeschriebenes, deterministisches Test-Double für vollständige Partien. Die Produktion benötigt weder diese Hilfsstrukturen noch Testbibliotheken.

Alle Tests laufen ohne Benutzereingaben. Pioneer verwaltet und restauriert die Standardstreams; es gibt kein manuelles `System.setIn`. Die Tests laufen sequenziell im Testprozess, damit Konsolentests nicht gegenseitig ihre Streams verändern.

## Testfälle

| Bereich | Abgedeckte Fälle |
| --- | --- |
| Stone | Gegenspieler für beide Farben, zweimaliges Wechseln ergibt ursprüngliche Farbe |
| Gewinnerkennung | Alle acht Gewinnlinien für beide Farben; leeres Brett; Unentschieden; jedes fehlende und gegnerische Feld einer Gewinnlinie |
| GreedyPlayer | Jedes mögliche erste freie Feld; volles Brett; keine Veränderung der Eingabe |
| Spielschleife | Identische Spielerinstanzen; negative und zu große Positionen einschließlich Integer-Grenzen; belegtes Feld; Farbwechsel; defensive Kopien |
| Spielende | CROSS-Sieg, CIRCLE-Sieg, Unentschieden; Sieg beim neunten Zug hat Vorrang vor Unentschieden; keine zusätzlichen Züge nach Sieg |
| Darstellung | Exakte ANSI-Sequenzen, X/O, freie Feldnummern, Abstände und Zeilenumbrüche |
| HumanPlayer | Mehrere aufeinanderfolgende Eingaben, beide Farben im Prompt, exakte Ausgabe, nichtnumerische Eingabe |
| Programmeinstieg | Vollständige Konsolenpartie über `main` und Pioneer |
| PerfectPlayer | Sofort gewinnen und Verlust blockieren für beide Farben; perfektes Spiel gegeneinander ergibt Unentschieden; Nullargumente, falsche Brettgröße, volles und bereits gewonnenes Brett |

## Vollständige Prüfung des perfekten Spielers

`PerfectPlayer` verwendet Negamax: Jeder mögliche Zug erhält den Wert Gewinn, Unentschieden oder Verlust aus Sicht des ziehenden Spielers. Der Spieler wählt einen Zug mit maximalem Wert und arbeitet auf einer Kopie des Eingabebretts.

`PerfectPlayerTest` prüft **alle 4.520 legal erreichbaren, nichtterminalen Spielstellungen**, unabhängig davon, wie die vorherigen Spieler gezogen haben. Dadurch werden auch Stellungen geprüft, die der perfekte Spieler selbst nie herbeiführen würde.

Die Referenz ist unabhängig implementiert: zwei Integer-Bitboards, acht Gewinnmasken und ein Minimax-Algorithmus, der aus Sicht von CROSS maximiert und aus Sicht von CIRCLE minimiert. Die Referenz ruft weder die Produktions-Gewinnerkennung noch die Produktions-Suche auf. Für jede Stellung wird geprüft:

1. Der gewählte Index liegt zwischen 0 und 8 und das Feld ist frei.
2. Das Eingabebrett bleibt unverändert.
3. Der gewählte Zug erhält denselben spieltheoretischen Wert wie die optimale Referenzentscheidung.

Das ist stärker als einzelne Beispielpartien und prüft neben Unbesiegbarkeit auch das Nutzen erzwingbarer Siege.

## Zusätzliches Framework: QuickTheories

`BoardPropertiesTest` führt zwei Properties mit jeweils 1.000 Beispielen und festen Seeds 450 und 451 aus. Ein ternär kodierter Wert erzeugt ein Brett aus dem Raum aller 3^9 Belegungen. Die Stichproben sind reproduzierbar; sie sind keine vollständige Enumeration.

- GreedyPlayer wählt stets das erste freie Feld oder wirft bei vollem Brett die erwartete Exception; das Brett bleibt unverändert.
- Gleichzeitiges Vertauschen beider Farben erhält die Gewinnlinien für die jeweils vertauschte Farbe.

Diese Properties benötigen keine legalen Spielhistorien, weil sie lokale Brettfunktionen prüfen. Die Legalität vollständiger Partien wird separat geprüft.

## Coverage und Mutation Testing

`check` hängt sowohl von `jacocoTestCoverageVerification` als auch von `pitest` ab. Das JaCoCo-Gate verwendet explizit `BRANCH`, `COVEREDRATIO`, mindestens `0.90`; keine Produktionsklasse ist ausgeschlossen.

PITest mutiert die Produktionsklassen mit dem standardmäßigen Mutatorensatz. HTML- und XML-Ausgaben haben feste Pfade. Es werden keine Methoden oder Klassen ausgeschlossen. Ein vollständig grüner Basistestlauf ist Voraussetzung für Mutation Testing. Die Aufgabe nennt keinen Mindest-Mutationsscore; der tatsächliche Score steht im Prüfprotokoll.

## SonarLint-Prüfung

`check` führt außerdem `sonarLint` aus. Ein kleiner Build-Adapter unter `tools/sonarlint/` verwendet die offiziellen SonarLint-Core- und SonarJava-Artefakte, aktiviert die 608 standardmäßig aktiven Java-Regeln und analysiert alle elf Java-Dateien unter `src/main`, `src/test` und `tools/sonarlint` mit Java-25-Binaries und vollständigem Test-/Tool-Classpath. Auch der Adapter selbst wird analysiert. Er ist ein separates Source Set und wird nicht in das Spiel-JAR aufgenommen. Er benötigt keinen Sonar-Server und überträgt keinen Quellcode an einen Analyse-Service.

Jeder gemeldete Befund sowie fehlgeschlagene Dateianalysen lassen den Build scheitern; ein nicht geladenes Java-Plugin oder eine leere Regelmenge ebenfalls. Der Bericht liegt unter `build/reports/sonarlint/report.txt` und wird automatisch von CI archiviert. Die Befunde wurden nicht durch pauschale Regeldeaktivierung ausgeblendet. Drei methodenspezifische, kommentierte Ausnahmen bleiben bewusst bestehen:

- `java:S106` in `HumanPlayer.play` und `TicTacToeMain.play`: stdout ist die Benutzerschnittstelle dieses Konsolenspiels; ein Diagnose-Logger wäre hier nicht passend.
- `java:S1172` in `TicTacToeMain.main`: der standardisierte Programmeinstieg benötigt keine Argumente.

Das Ergebnis bezieht sich auf diese dokumentierten Standardregeln. Eine abweichende schulische Connected-Mode-Regelkonfiguration müsste zusätzlich geprüft werden.

## Lokal ausführen

JDK 25 muss verfügbar sein. Die Java-Toolchain bleibt auf Version 25.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot'
$env:GRADLE_USER_HOME = "$env:USERPROFILE\.gradle"
.\gradlew.bat clean check jar --no-daemon --no-build-cache --rerun-tasks
```

Unter Linux bzw. im Dev Container:

```bash
sh ./gradlew clean check jar --no-daemon --no-build-cache --rerun-tasks
```

Für gezielte Läufe:

```text
gradlew.bat test jacocoTestReport
gradlew.bat pitest
gradlew.bat sonarLint
```

| Ergebnis | Pfad |
| --- | --- |
| JUnit / QuickTheories HTML | build/reports/tests/test/index.html |
| JUnit XML | build/test-results/test/ |
| JaCoCo HTML | build/reports/jacoco/test/html/index.html |
| JaCoCo XML / CSV | build/reports/jacoco/test/ |
| PITest HTML | build/reports/pitest/index.html |
| PITest XML | build/reports/pitest/mutations.xml |
| SonarLint | build/reports/sonarlint/report.txt |

## CI, Pages und Abgabe

`.github/workflows/ci.yml` läuft bei jedem Push sowie bei Pull Requests auf main und auf manuellen Aufruf. `clean check jar` umfasst alle drei Testansätze, Branch-Gate, Mutation Testing und SonarLint. Der Upload mit `if: always()` archiviert `build/reports/` und `build/test-results/`, einschließlich vorhandener Fehlerberichte. Ein früher Konfigurationsfehler kann die Erzeugung von Reports verhindern.

Die Pages-Zeitreihe verwendet Branch-Coverage aus `scripts/coverage.sh` und speichert sie separat als `data/branch-coverage.csv`; frühere Zeilenwerte werden nicht mit Branch-Werten vermischt. Anwendungs- und Dev-Container-Releases verwenden ebenfalls `check` und damit Mutation Testing. Die bestehenden Release-Anleitungen gelten weiterhin.

Ein lokaler grüner Lauf ist kein Nachweis eines entfernten CI-Laufs. Private Repository-Sichtbarkeit, Zugriff der Lehrperson und Abgabetermin müssen separat erfüllt sein. Der Dev Container empfiehlt zusätzlich zum automatischen SonarLint-Lauf die SonarQube-for-IDE-Erweiterung für direktes Feedback beim Bearbeiten.

Offizielle Dokumentation: [JUnit](https://junit.org/), [AssertJ](https://github.com/assertj/assertj/releases), [Pioneer](https://junit-pioneer.org/docs/standard-input-output/), [QuickTheories](https://github.com/quicktheories/QuickTheories), [JaCoCo](https://www.jacoco.org/jacoco/), [PITest](https://pitest.org/), [Gradle-PITest-Plugin](https://plugins.gradle.org/plugin/info.solidsoft.pitest).
