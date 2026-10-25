# Changelog

Alle relevanten Änderungen an TicTacTest werden in dieser Datei dokumentiert. Das Format orientiert sich an Keep a Changelog und verwendet Semantic Versioning.

## [Unreleased]

### Added

- Automatisches SonarLint-Gate mit den offiziellen Java-Analyzern, allen aktiven Standardregeln und archiviertem Prüfbericht.
- Optimaler `PerfectPlayer` mit Negamax und unabhängiger Prüfung aller 4.520 erreichbaren nichtterminalen Spielstellungen.
- Pioneer-Tests für Konsoleneingaben und Ausgaben sowie QuickTheories-Properties mit reproduzierbaren Seeds.
- Automatisches PITest-Mutation-Testing als Bestandteil von `check` mit HTML- und XML-Reports.
- CI-Artefakte mit sämtlichen Test-, Coverage- und Mutation-Reports.

### Changed

- Das 90-%-Coverage-Gate und die Coverage-Zeitreihe verwenden Branch-Coverage; die neue Messreihe ist von der bisherigen Zeilenmetrik getrennt.
- Testbibliotheken auf die am 25.10.2026 ermittelten neuesten stabilen Versionen aktualisiert.

## [1.0.1] - 2026-09-23

### Fixed

- Tippfehler in der Eingabeaufforderung des menschlichen Spielers korrigiert.

## [1.0.0] - 2026-09-23

### Added

- Erste vollständig spielbare TicTacToe-Version für einen menschlichen und einen automatischen Spieler.
- Automatisierte Tests für Spiellogik, Gewinnlinien, ungültige Spielzüge und Spielerimplementierungen.
- JaCoCo-Abdeckungsberichte mit einem Quality Gate von 90 Prozent.
- CI-, GitHub-Pages- und versionierter Dev-Container-Prozess.
- Ausführbares und versioniertes JAR als GitHub-Release-Artefakt.

### Changed

- Die Standardeingabe verwendet während einer Partie einen gemeinsamen Scanner und unterstützt dadurch mehrere aufeinanderfolgende Spielzüge zuverlässig.
- Der Dev Container verwendet Alpine Linux, Java 25 und einen Benutzer mit UID/GID `1000:1000`.

### Fixed

- Ungültige oder bereits belegte Spielpositionen werden kontrolliert abgelehnt.
