# Einbau

Diese Dateien sind als Overlay für `raphaelhuerzele/450-tictactest-mvk` gedacht.

1. Inhalt dieses Ordners in den Repository-Root kopieren.
2. Vorhandene `build.gradle`, `.gitignore` und `src/test/java/ch/bbw/m450/tictactoe/TicTacToeTest.java` ersetzen.
3. Bestehende Dateien unter `src/main/...`, `gradle/`, `gradlew`, `gradlew.bat`, `settings.gradle` und `gradle.properties` unverändert lassen.
4. Lokal `./gradlew clean check` ausführen.
5. Alles nach `main` pushen.
6. GitHub Actions kontrollieren.
7. Prüfen, ob GitHub Pages von `gh-pages` / `root` veröffentlicht wird.
8. Den Dev-Container-Releaseprozess aus `DEVCONTAINER_RELEASE.md` verwenden.

Wichtig: `.gitignore` wurde absichtlich angepasst, damit `.devcontainer/**` nicht mehr von der bisherigen Regel `.*` verschluckt wird.
