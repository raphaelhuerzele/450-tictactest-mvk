# Changelog

Alle relevanten Änderungen an TicTacTest werden in dieser Datei dokumentiert. Das Format orientiert sich an Keep a Changelog und verwendet Semantic Versioning.

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
