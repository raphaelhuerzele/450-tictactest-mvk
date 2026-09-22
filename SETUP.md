# Einbau

Diese Dateien sind als Overlay für `raphaelhuerzele/450-tictactest-mvk` gedacht.

1. Inhalt dieses Ordners in den Repository-Root kopieren.
2. Vorhandene `build.gradle`, `.gitignore` und `src/test/java/ch/bbw/m450/tictactoe/TicTacToeTest.java` ersetzen.
3. Bestehende Dateien unter `src/main/...`, `gradle/`, `gradlew`, `gradlew.bat`, `settings.gradle` und `gradle.properties` unverändert lassen.
4. Lokal `./gradlew clean check` ausführen.
5. Alles nach `main` pushen.
6. GitHub Actions kontrollieren.
7. Nach dem ersten Coverage-Pages-Lauf GitHub Pages auf `gh-pages` / `root` aktivieren.
8. Unter Packages prüfen, ob das Dev-Container-Image veröffentlicht wurde.

Wichtig: `.gitignore` wurde absichtlich angepasst, damit `.devcontainer/**` nicht mehr von der bisherigen Regel `.*` verschluckt wird.
