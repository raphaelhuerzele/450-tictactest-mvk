# Webapp-Aufträge: tatsächlicher Prüfstand

Stand: 04.11.2026. Dieses Protokoll trennt ausgeführte Prüfungen von noch nicht durchgeführten AWS-Experimenten. Eine gültige Konfiguration ist kein Nachweis einer laufenden Cloud-Umgebung.

## Lokal ausgeführt

| Prüfung | Tatsächliches Ergebnis |
| --- | --- |
| Bestehendes Release-Artefakt | v1.0.1 von GitHub geladen, unverändertes JAR; SHA256 7db47b0f09a8222754a689b1352a53d82599ca88ec805488a5ca3ef2aaa8dfd1 stimmt mit Release überein |
| Runtime | ttyd 1.7.7 und Amazon Corretto 25.0.5.0.10 unter Ubuntu/WSL, beide mit festem Download und geprüfter SHA256 |
| Browser | Chrome headless öffnet das Frontend auf :8000 und das eingebettete Terminal auf :7681 |
| Konsoleneingabe | Züge 4, 8, 2, 6 über echte Tastaturereignisse im iframe: CROSS gewinnt |
| Ungültige Eingabe | abc erzeugt NumberFormatException und beendet ausschließlich die Partie; keine Shell |
| Neustart | „Neue Partie“ lädt das iframe neu, neues Brett und neuer Eingabeprompt erscheinen |
| Zwei Tabs | Zwei unabhängige JVMs, RSS beim gemessenen Start 43.872 und 43.848 KiB; kein allgemeiner Lasttest für EC2 |
| Versionen/CORS | Frontend 1.1.0 und Backend 1.0.1 werden unabhängig angezeigt; cross-origin Versionsabfrage funktioniert |
| Infrastruktur | OpenTofu 1.13.2 init ohne Backend, fmt und validate erfolgreich; AWS-Provider 6.71.0 gelockt |
| Änderungserkennung | Automatisierte Tests für Frontend-only, Backend-only, beide, Doku-only, ersten Release und erzwungenen Rollback erfolgreich |
| Spieltests | 77 Java-Tests grün, 100 % Branch-Coverage, 78/78 Mutationen erkannt, SonarLint ohne Befunde |
| Artefakt-Paketierung | Release v1.1.0 veröffentlicht; JAR, Frontend-ZIP ohne config.js, Runtime-TAR und SHA256SUMS erneut von GitHub geladen und alle Prüfsummen erfolgreich geprüft |
| Betrieb nur aus Release-Assets | Ausschließlich heruntergeladene v1.1.0-Assets gestartet, ohne Quellcode/Gradle: beide Versionen 1.1.0; Partie, ungültige Eingabe, Neustart und zwei Tabs bestehen erneut; zwei JVMs mit 44.176 und 43.920 KiB RSS |
| Workflow-Syntax | Alle YAML-Dateien erfolgreich geparst |

Lokale Browser-Evidenz: `build/web-evidence/browser.json` und `browser.png` (generiert, nicht Quellcode). Reproduzierbar mit `node scripts/web/browser-check.mjs`, wenn Playwright Core unter `.tools/browser` und ein lokaler Chrome verfügbar sind. Der Test baut das JAR nicht.

Persistierter Screenshot des tatsächlichen v1.1.0-Tests: [Web-Konsole](evidence/web-console-v1.1.0.png).

## Veröffentlichter Release und entfernte Prüfungen

- [Release v1.1.0 mit fünf Assets](https://github.com/raphaelhuerzele/450-tictactest-mvk/releases/tag/v1.1.0)
- [Release-Workflow erfolgreich](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/41804161028): Build, Tests, JAR-Start und Veröffentlichung erfolgreich; `deploy / deploy` ausdrücklich **skipped**, weil AWS_DEPLOY_ENABLED noch nicht gesetzt ist.
- [CI auf main erfolgreich](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/41804158435)
- [Infrastrukturvalidierung erfolgreich](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/41804158417)
- [Dev-Container-CI erfolgreich](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/41804158464)
- [Coverage-Veröffentlichung erfolgreich](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/41804158514)

Ein normaler main-Push hat diese Prüfungen, aber kein Cloud-Deployment ausgelöst. Der Cloud-Erfolg wird dadurch nicht behauptet. Ubuntu hatte lokal keine funktionierende DNS-Auflösung; die beiden gepinnten Runtime-Dateien wurden deshalb über Windows geladen, anhand der festgehaltenen SHA256 geprüft und als Runtime-Cache verwendet. Es wurden keine Spielartefakte für den Betriebsnachweis lokal neu gebaut.

## AWS-Prüfung am 04.11.2026

- STS-Anmeldung mit erneuerten temporären Lab-Zugangsdaten erfolgreich.
- OpenTofu apply erfolgreich: EC2, Elastic IP 34.226.181.248, HTTP-Sicherheitsgruppe und vorhandenes LabInstanceProfile. Keine neuen IAM-Rollen.
- S3-Variante durch eine explizite Organisationssperre für GetBucketObjectLockConfiguration blockiert. Die nachweislich leeren Test-Buckets wurden entfernt; das erlaubte EC2/nginx-Hosting ersetzt S3.
- cloud-init meldet done; nginx ist aktiv. Amazon Linux benötigt curl-minimal statt curl.
- Backend v1.1.0 aus dem unveränderten GitHub-Release-JAR per SSM installiert; Smoke-Partie erfolgreich, Dienst aktiv, lokaler HTTP-Endpunkt liefert 200 und Version 1.1.0.
- Der externe HTTP-Check und Chrome-Test dieses Rechners scheitern am Schul-Webfilter (FortiGuard, Kategorie Unrated). Das ist kein erfolgreicher Cloud-Browsernachweis. Die Sperre wird nicht umgangen.
- Drei temporäre AWS-Secrets mit ausdrücklicher Zustimmung verschlüsselt in GitHub Environment lab hinterlegt; keine Zugangsdaten in Git oder Terraform-Variablen.
- Aktuelle Instanz: i-087b2be0146b92007. Bei der Bootstrap-Korrektur wurde die noch leere Instanz ersetzt; dieselbe Elastic IP blieb erhalten.
- Frontend v1.1.0 mit geprüftem ZIP installiert; HTTP liefert Version 1.1.0 und die HTML-Seite. Kein Neustart des Spieldiensts beim Frontend-Deploy.
- OpenTofu plan nach dem App-Deploy: No changes. Your infrastructure matches the configuration.
- EC2-Neustart ausgeführt: nginx und tictactest starten automatisch; beide Komponenten bleiben 1.1.0, JAR-SHA256 unverändert.
- Absichtlich falschen JAR-Digest getestet: Deployment scheitert vor dem Umschalten; aktive Version und Dienst bleiben erhalten. Das ersetzt keinen Test mit einem gültig signierten, aber fachlich kaputten JAR.

## Release-Deployment und öffentliche Browser-Abnahme

| Prüfung | Gemessener Nachweis |
| --- | --- |
| Automatischer Frontend-only-Release | [v1.1.1-Workflow erfolgreich](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/41809066109): vorher v1.1.0; backend=false, frontend=true. Frontend danach 1.1.1, Backend weiterhin 1.1.0. Dienst-Startzeit unverändert 09:39:31 UTC. |
| Gezielter Rollback | [Manueller Dispatch auf v1.1.0 erfolgreich](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/41809429976): beide Komponenten 1.1.0, öffentliche Chrome-Abnahme erfolgreich. |
| Zweiter Release ohne Infrastrukturänderung | [Manueller Dispatch auf v1.1.1 erfolgreich](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/41809560344): beide Komponenten 1.1.1; gleiche EC2-ID und Elastic IP, abschließender OpenTofu-Plan ohne Änderungen. |
| Öffentliche Browserprüfung | Chrome auf GitHub-Runner öffnet die echte EC2-Adresse, spielt 4, 8, 2, 6 bis CROSS gewinnt, prüft zweiten Tab mit abc und startet eine neue Partie. Versionen jeweils exakt 1.1.0 bzw. 1.1.1. JSON und Screenshots im Workflow-Artefakt deployment-evidence. |
| main ohne Tag | [CI erfolgreich](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/41809374606); der Push von 6bd5a22 startet kein Deployment. Deployments entstehen nur aus Release-Workflow oder bewusstem Dispatch. |
| Backend-only / beide / Doku-only | Änderungserkennung durch automatisierte Tests geprüft. Beide Komponenten außerdem durch die zwei manuellen Dispatches real installiert. Ein eigenständiger automatischer Backend-only-Release wurde nicht zusätzlich erzeugt. |

Persistierte Cloud-Screenshots: [Rollback 1.1.0](evidence/cloud-console-v1.1.0.png), [aktuelle Version 1.1.1](evidence/cloud-console-v1.1.1.png).

Bei GitHub traten vorübergehende Serverfehler auf: main-Push mit commit_refs, Release-Upload mit HTTP 422 und Dispatch mit HTTP 502. Wiederholungen waren erfolgreich. Der erfolgreiche Release-Lauf enthält auch die Java-Tests, Coverage-/Mutation-Gates und SonarLint-Prüfung.

Nicht durchgeführt: tatsächliches Lab-Session-Ende, vollständiges destroy/apply, ein separat veröffentlichter fachlich kaputter JAR-Release sowie ein absichtlich mit abgelaufenen Secrets gestarteter GitHub-Job. Der Neustart und die falsche Prüfsumme sind echte, oben abgegrenzte Tests. Für die offenen Experimente wird kein Erfolg behauptet.

Ablauf, Secrets-Konzept und Promotion-Strategie: [deployment.md](deployment.md).
