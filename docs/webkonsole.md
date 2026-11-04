# Auftrag 1 – Web-Konsole

## Architektur und Wahl

`ttyd 1.7.7` wird gewählt, weil es ein geprüftes statisches Binary und xterm.js inklusive ANSI-Unterstützung mitbringt. GoTTY ist weniger gepflegt; websocketd würde eigene Terminal-/ANSI-Behandlung erfordern. Das Spiel und sein JAR bleiben unverändert.

Ein Browser öffnet eine WebSocket-Verbindung. ttyd erzeugt dafür ein Pseudo-Terminal (PTY) und startet genau einen Java-Prozess mit festem `-jar`-Argument. Eingaben gelangen über WebSocket ins PTY, Ausgaben zurück ins Browser-Terminal. Ein zweiter Tab öffnet eine zweite Verbindung und eine zweite JVM; die Spieler teilen keinen Spielzustand.

```text
Frontend :8000 ── iframe ── ttyd :7681 ── PTY ── java -jar Release-JAR
       └──── fetch ─────── Versionsserver :7682/version
```

`--writable` aktiviert Tastatureingaben (sonst ist ttyd schreibgeschützt). `--check-origin` prüft die Herkunft des WebSocket-Handshakes; das iframe selbst wird von der Terminal-Origin geladen, deshalb bleibt die WebSocket-Origin gleich. `--max-clients 4` begrenzt Verbindungen. `disableReconnect=true` verhindert unbeabsichtigte Neustarts nach Spielende. „Neue Partie“ lädt das iframe neu und erzeugt eine neue Verbindung.

## Start ausschließlich aus Release-Assets

Voraussetzungen: Linux/Ubuntu/WSL x86_64, Bash, curl, tar, sha256sum und Python 3. Der installierte Java-/ttyd-Stand wird nicht vorausgesetzt; beide werden mit fester Version und geprüfter SHA-256 heruntergeladen. Kein Java-Quellcode oder lokaler Gradle-Build ist für den Betrieb erforderlich.

In PowerShell Release-Assets laden:

```powershell
gh release download v1.1.0 --repo raphaelhuerzele/450-tictactest-mvk --dir .tools/web-release-1.1.0
wsl -d Ubuntu -- bash -lc 'cd /mnt/c/Users/noels/Downloads/450-tictactest-mvk/.tools/web-release-1.1.0 && tar -xzf tictactest-runtime-1.1.0.tar.gz && bash local/start.sh "$PWD" v1.1.0'
```

Linux:

```bash
mkdir -p release-assets
gh release download v1.1.0 --repo raphaelhuerzele/450-tictactest-mvk --dir release-assets
cd release-assets
tar -xzf tictactest-runtime-1.1.0.tar.gz
bash local/start.sh "$PWD" v1.1.0
```

Öffnen: <http://localhost:8000>. Das Terminal liegt auf Port 7681, der Versionsserver auf 7682 – Frontend und Backend sind tatsächlich getrennte Origins. Strg+C im startenden Terminal beendet die lokalen Server. Es werden ausschließlich Loopback-Adressen gebunden.

## Version und Konfiguration

- `frontend-version.txt` wird beim Release automatisch aus `releaseVersion` erzeugt und mitarchiviert.
- Der Backend-Versionsserver erhält die Version aus dem validierten Release-Tag, der zugleich den JAR-Dateinamen bestimmt. Checksummen stellen sicher, dass das gewählte Release-JAR verwendet wird.
- `config.js` enthält ausschließlich `terminalUrl` und `backendVersionUrl`. Lokal entsteht sie aus `config.example.js`, in AWS ausschließlich aus OpenTofu-Outputs. Sie gehört nicht ins Frontend-Archiv.
- Der öffentliche `/version`-Endpunkt ist nicht vertraulich und erlaubt `Access-Control-Allow-Origin: *`. Das Frontend darf die Textantwort deshalb von seiner anderen Origin lesen. CORS ersetzt keine Authentifizierung.
- HTTP-Fehler und CORS-Probleme werden auf der Seite sowie in den Browser-Entwicklerwerkzeugen sichtbar. Beide Versionsanzeigen bleiben unabhängig: Ein Frontend-only-Release muss keine neue Backend-Version anzeigen.

## Verhalten und Sicherheit

Nach Sieg oder Unentschieden beendet sich das JAR. Das Terminal bleibt als beendete Sitzung stehen; „Neue Partie“ startet eine neue JVM. Nichtnumerische Eingaben und belegte/ungültige Felder führen beim unveränderten Spiel zu einer Exception und beenden diese Partie. Es gibt danach keine Shell. Strg+C beendet ebenfalls nur die JVM.

Die ttyd-Kommandozeile enthält **direkt Java**, niemals `bash`, `sh` oder `login`; die Option `--url-arg` wird nicht aktiviert. Eine versehentlich veröffentlichte Shell würde Besuchern die Rechte des Dienstbenutzers geben, Dateien und erreichbare AWS-Dienste gefährden. In AWS läuft der Dienst deshalb als Benutzer ohne Login, mit schreibgeschütztem Dateisystem und begrenztem Gesamtspeicher. Die Rolle am Instance Profile bleibt trotzdem ein sensibler Teil der Vertrauensgrenze.

Vier JVMs benötigen mehr als viermal ihren Heap: Metaspace, Thread-Stacks, Code-Cache, ttyd, Betriebssystem und SSM-Agent kommen hinzu. Pro JVM werden Heap (64 MiB), Metaspace (64 MiB), Code-Cache (32 MiB) und CPU-Anzahl begrenzt; die Dienstgruppe erhält zusätzlich `MemoryMax=640M`. Das ist eine Begrenzung, kein pauschaler Nachweis, dass jede 1-GB-Instanz vier Lastspitzen verkraftet. Zwei gleichzeitige Tabs und Prozessspeicher werden im Testprotokoll separat dokumentiert.

Das Lab verwendet bewusst HTTP ohne Zugangsdaten und ohne persönliche Daten; nginx-Frontend und iframe haben damit keinen HTTPS/HTTP-Mixed-Content-Konflikt. Für einen dauerhaften öffentlichen Betrieb wären HTTPS, Domain, Zugriffsschutz und gegebenenfalls WAF/Rate-Limits nötig. Ein HTTPS-Frontend darf kein HTTP-Terminal einbetten.

## Release-Zugehörigkeit

Ein Release enthält JAR, Frontend-ZIP, Runtime-TAR, SHA256SUMS und die bisherige JAR-Prüfsumme. Das kleine Frontend ist ein Artefakt, damit dieselben Bytes reproduzierbar deployt und zurückgerollt werden können. Die lokalen Startskripte gehören als Runtime-Archiv zum auslieferbaren Paket. Die systemd-/nginx-Vorlagen werden für die Betriebsnachvollziehbarkeit mitgeliefert; in AWS installiert ausschließlich OpenTofu diese Infrastruktur. Ein Release überschreibt dort keine Units und ruft kein `tofu apply` auf.

Quellen: [ttyd-Optionen](https://github.com/tsl0922/ttyd/blob/main/man/ttyd.man.md), [ttyd-Release mit Prüfsummen](https://github.com/tsl0922/ttyd/releases/tag/1.7.7), [Amazon Corretto Downloads](https://docs.aws.amazon.com/corretto/latest/corretto-25-ug/downloads-list.html).
