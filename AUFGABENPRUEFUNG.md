# Abschlussprüfung TicTacTest

Stand: 25.10.2026. Grundlage: „TicTacTest Assignment“, Version 260902-Ce08d70a vom 02.09.2026.

Auf ausdrücklichen Wunsch wurden Implementierungen durch KI erstellt. Die schulische Vorgabe gegen LLM-Codegenerierung wird damit nicht erfüllt. Dieses Protokoll beschreibt technische Ergebnisse und behauptet keine eigenständig erbrachte Schülerleistung.

## Prüfung aller Bewertungspunkte

| Nr. | Anforderung | Ergebnis / Nachweis |
| --- | --- | --- |
| 1 | Unit-Tests mit AssertJ und Hilfsstrukturen | Umgesetzt: genaue Ergebnis-, Array-, Ausgabe- und Exception-Assertions; Fixtures und deterministischer QueuePlayer. |
| 2 | Parametrisierte Tests, wenig Wiederholung | Umgesetzt: alle Gewinnlinien für beide Farben, fehlende/gemischte Linien, beide Gegnerfarben, alle ersten freien Felder, ungültige Positionen und komplette Partien. |
| 3 | JaCoCo-Reports, 90 % Branch-Coverage erzwingen | Umgesetzt: BRANCH-Gate 0.90; HTML/XML/CSV; letzter vollständiger Lauf 100 % (102/102 Branches), 98,73 % Zeilen (78/79). |
| 4 | StdIn/StdOut-Testing mit Pioneer | Umgesetzt: mehrere Eingaben, exakte Prompts, ungültige Eingaben, Spielausgaben, main. Streams werden von Pioneer verwaltet. |
| 5 | CI bei jedem Commit, alle Reports als Artefakte, grüner Build | Lokal grün; CI führt check einschließlich Mutation Testing und SonarLint aus und archiviert alle Reports und XML-Testergebnisse auch bei Fehlern. Commit 585dd3c wurde bereits erfolgreich in CI, Coverage und Dev-Container-CI geprüft; der aktuelle SonarLint-Nachtrag wird beim Push erneut geprüft. |
| 6 | Zusätzlicher perfekt spielender Player | Umgesetzt: PerfectPlayer; optimale Entscheidungen auf allen 4.520 erreichbaren nichtterminalen Stellungen gegen unabhängige Bitboard-Referenz geprüft. |
| 7 | Zusätzliches Testingframework mit automatischem Beispieltest | Umgesetzt: QuickTheories, zwei Properties mit je 1.000 Beispielen und festen Seeds; automatisch in test/check/CI. |
| 8 | Automatisiertes PITest | Umgesetzt: Bestandteil von check; 78/78 Mutationen KILLED, keine überlebenden oder ungetesteten Mutationen im Standard-Mutatorensatz. HTML/XML vorhanden. |
| 9 | SonarLint, Stil, var, aktuelle Libraries | Umgesetzt: var soweit sinnvoll, neueste stabile Libraries ermittelt; offizieller SonarLint-Core 12.2.0.87867 mit SonarJava 8.45.0.59462 analysiert alle elf Java-Dateien einschließlich des Build-Adapters mit 608 Standardregeln: 0 Befunde. Drei begründete methodenspezifische Ausnahmen betreffen ausschließlich stdout als Konsolen-UI und die feste main-Signatur. Spiellogik unverändert. |
| 10 | Pünktliche private GitHub-Abgabe | Offen: GitHub-API bestätigt private=false für raphaelhuerzele/450-tictactest-mvk. Auf ausdrücklichen Wunsch erfolgt die Veröffentlichung im bestehenden öffentlichen Repository; die geforderte private Sichtbarkeit wird damit nicht erfüllt. Abgabetermin und Lehrpersonenzugriff sind nicht bekannt. |

## Tatsächliche Verifikation

```text
gradlew.bat clean check jar --no-daemon --no-build-cache --rerun-tasks
```

- Vollständiger Build erfolgreich, ohne Übernahme von Testresultaten aus dem Build-Cache.
- 77 Testausführungen: TicTacToeTest 62, PerfectPlayerTest 13, BoardPropertiesTest 2.
- Keine Fehler, Fehlschläge oder übersprungenen Tests.
- Die beiden QuickTheories-Tests führen zusätzlich jeweils 1.000 generierte Beispiele aus.
- Eine der Testausführungen prüft alle 4.520 erreichbaren nichtterminalen Stellungen.
- Branch-Coverage: 100 %; Line-Coverage: 98,73 %.
- PITest: 78 Mutationen erzeugt, alle 78 durch Tests erkannt; keine Timeouts und keine Mutationen ohne Coverage.
- Ausführbares JAR mit den Eingaben 4, 8, 2, 6 gestartet: CROSS gewinnt erwartungsgemäß.
- Coverage-Skript liefert 100.0; Bash-Syntaxprüfung erfolgreich.
- SonarLint: elf Dateien, 608 aktive Standardregeln, 0 Befunde. Der erste Lauf fand 14 Befunde und scheiterte erwartungsgemäß; nach Korrektur und begründeten Ausnahmen ist das Gate grün. Drei weitere Befunde im anschließend einbezogenen Build-Adapter wurden durch Refactoring und Diagnose-Logging behoben.
- Bereits veröffentlichter CI-Nachweis: [CI für 585dd3c](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/40234378968); vollständiges Report-Artefakt heruntergeladen und kontrolliert.

## Verbleibende Abgabeschritte

1. Nach dem aktuellen Push den tatsächlichen CI-Lauf samt herunterladbarem SonarLint-Bericht prüfen. Der technische SonarLint-Nachweis liegt lokal bereits vor; bei abweichendem schulischem Regelprofil dieses zusätzlich verwenden.
2. Für die Aufgabenanforderung bleibt eine private Abgabe notwendig; der ausdrücklich gewünschte Push ins öffentliche Repository ersetzt diese Vorgabe nicht.
3. Zugriff der Lehrperson und die rechtzeitige Abgabe sicherstellen. Die Vorgabe zur Eigenarbeit bleibt durch die KI-generierten Änderungen unerfüllt.

Details und Wiederholungsbefehle: [TESTKONZEPT.md](TESTKONZEPT.md). Generierte Reports liegen unter build/reports/ und werden nicht als Quellcode eingecheckt.
