# Changelog

Alle relevanten Änderungen an TicTacTest werden in dieser Datei dokumentiert. Das Format orientiert sich an Keep a Changelog und verwendet Semantic Versioning.

## [Unreleased]

## [1.1.3] - 2026-11-04

### Changed

- JAR-Manifest um Titel und Release-Version ergänzen, damit das Backend-Artefakt selbst eindeutig identifizierbar ist.
- Im Frontend erklären, warum getrennt deployte Komponenten unterschiedliche Versionsnummern anzeigen können.
- Beide Komponenten gemeinsam als Nachweis des kombinierten Release-Deployments aktualisieren.

## [1.1.2] - 2026-11-04

### Changed

- Gradle-Warnungen im Backend-Build als Zusammenfassung anzeigen, statt sie vollständig zu unterdrücken. Dieser Backend-only-Release prüft das selektive Deployment bei unverändertem Frontend.

## [1.1.1] - 2026-11-04

### Fixed

- Frontend auf EC2 mit nginx statt S3 bereitstellen, da das Learner Lab den erforderlichen S3-Provider-Zugriff sperrt.
- Release-Assets mit SHA256 über SSM installieren und Frontend unabhängig vom Spielprozess aktivieren.
- Amazon-Linux-Bootstrap mit dem vorinstallierten curl-minimal kompatibel machen.

### Changed

- Die Web-Konsole erklärt die unabhängigen Spielsitzungen.

## [1.1.0] - 2026-11-04

### Added

- Statisches Web-Frontend mit eingebettetem ttyd-Terminal und getrennten Versionsanzeigen.
- Versionierte Frontend- und Runtime-Archive sowie SHA256SUMS als Release-Assets.
- Reproduzierbare lokale ttyd-/Corretto-Laufzeit mit einem unveränderten Release-JAR.
- OpenTofu-Konfiguration für EC2, feste Elastic IP, Instance Profile, S3-Hosting und geschützten Artefakt-Bucket.
- Release-basiertes, komponentenweises Deployment über S3 und SSM, HTTP-Versionsprüfungen und manueller Rollback.
- Anleitungen und getrennte Nachweise für die Webapp-Aufträge 1 bis 3.

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
