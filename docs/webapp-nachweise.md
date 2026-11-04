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
- Instanz beim ersten Nachweis: i-087b2be0146b92007. Bei der Bootstrap-Korrektur wurde die noch leere Instanz ersetzt; dieselbe Elastic IP blieb erhalten.
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
| Automatischer Backend-only-Release | [v1.1.2 erfolgreich](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/41811630576): vorher v1.1.1; backend=true, frontend=false. Öffentlicher Chrome-Test: Backend 1.1.2, Frontend unverändert 1.1.1. |
| Automatischer Release beider Komponenten | [v1.1.3 erfolgreich](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/41812482632): vorher v1.1.2; backend=true, frontend=true. Öffentlicher Chrome-Test: beide Komponenten 1.1.3. |
| Doku-only | Änderungserkennung durch automatisierte Tests geprüft; normale main-Pushes lösen kein Deployment aus. |

Persistierte Cloud-Screenshots: [Rollback 1.1.0](evidence/cloud-console-v1.1.0.png), [Version 1.1.1](evidence/cloud-console-v1.1.1.png), [kombinierter Release 1.1.3](evidence/cloud-console-v1.1.3.png), [vier Sitzungen nach Instanzersatz](evidence/cloud-four-sessions.png). [JSON-Protokoll der vier Sitzungen](evidence/cloud-four-sessions.json).

Bei GitHub traten vorübergehende Serverfehler auf: main-Push mit commit_refs, Release-Upload mit HTTP 422 und Dispatch mit HTTP 502. Wiederholungen waren erfolgreich. Der erfolgreiche Release-Lauf enthält auch die Java-Tests, Coverage-/Mutation-Gates und SonarLint-Prüfung.

## Negativtest: korrekt gehashtes, kaputtes JAR

SSM-Kommando `33e08c40-b1e6-4aa0-b552-02317d68b19a` führte den unveränderten produktiven Installationshelfer mit einer bewusst defekten JAR-Datei aus. Für diesen Test lieferte eine nur im Testprozess definierte curl-Funktion die lokale Fixture; es wurde kein kaputtes öffentliches Release veröffentlicht. Die Prüfsumme war korrekt, also wurde tatsächlich der Java-Preflight geprüft.

Ergebnis: SHA256 OK, danach `Invalid or corrupt jarfile`. Abbruch innerhalb der gemessenen Sekunde (0 volle Sekunden); Symlink und Dienst-Startzeit unverändert, Dienst aktiv, HTTP-Version weiter 1.1.3. Es gab keine Umschaltung und keine Ausfallzeit; eine Wiederherstellung war nicht nötig. Der Fixture-Transport prüft nicht den GitHub-Download, der bereits durch die regulären Releases belegt ist.

Die automatische Freigabeprüfung hatte die Veröffentlichung eines kaputten öffentlichen Releases wegen des möglichen automatischen Deployments abgelehnt. Ebenso wurde ein Überschreiben funktionierender GitHub-Secrets für den Credential-Test abgelehnt. Die sicheren Alternativen ändern weder öffentliche Release-Assets noch gespeicherte Secrets.

## Authentifizierung und user_data-Änderung

- [Authentifizierungs-Experiment erfolgreich](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/41813097282): Ein nur im Testschritt ungültiger Session-Token führt vor App-Änderungen zum Fehler `AWS authentication failed. Renew all three Lab secrets, then manually redeploy the release.` Die erwartete fehlgeschlagene Teststufe wird geprüft; danach STS erfolgreich und regulärer Redeploy inklusive Browser grün. Gespeicherte Secrets unverändert. Es wurde ein ungültiger Token getestet, kein tatsächlicher Zeitablauf behauptet.
- Einrichtung geändert: `Environment=LANG=C.UTF-8` im systemd-Dienst. OpenTofu plant und erstellt wegen user_data einen Ersatz: `i-087b2be0146b92007` wird `i-09215255eb3064554`. Elastic IP bleibt `34.226.181.248`; die App-Dateien auf dem alten Root-EBS gehen verloren.
- cloud-init danach done, nginx aktiv, neue Umgebungsvariable vorhanden; vor dem Redeploy bewusst kein game.jar. Infrastruktur installiert keine App-Version.
- Outputs in GitHub aktualisiert, [dieselben v1.1.3-Assets erneut deployt](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/41813414102): beide Versionen 1.1.3. Chrome spielt erfolgreich, inklusive **vier gleichzeitig geöffneter Sitzungen**. Das ist ein Funktionsnachweis für vier Sitzungen auf t3.micro, kein Dauerlasttest.

## Vollständiges Destroy/Apply

Am 04.11.2026 wurden nach Prüfung des Accounts und der sechs eigenen State-Ressourcen sämtliche Projektressourcen entfernt und mit demselben Infrastrukturcode neu angelegt. Vorhandene Default-VPC, Subnetze und LabInstanceProfile sind Datenquellen und wurden nicht gelöscht.

| Wert | Vorher | Nachher |
| --- | --- | --- |
| EC2 | i-09215255eb3064554 | i-0873d8927ea202ee8 |
| Elastic-IP-Allocation | eipalloc-0d97eaa2584bfd49c | eipalloc-015eadf77451ef548 |
| Öffentliche IP | 34.226.181.248 | 184.197.155.101 |
| Security Group | sg-0e3a4f880bc3d4e24 | sg-0d1aeeac5af8c72ca |

Destroy: sechs Ressourcen gelöscht. Apply: sechs Ressourcen neu erstellt. Root-EBS und lokale Release-Verzeichnisse gehören zur zerstörten Instanz; ein bloßes Apply stellt die App-Version nicht wieder her. Deshalb Outputs für GitHub aktualisieren, cloud-init/SSM abwarten und denselben veröffentlichten Release erneut deployen. Der README-Link wurde auf die neue Adresse aktualisiert.

cloud-init meldet danach done; nginx aktiv, noch keine App-Version vorhanden (SSM `95d1b6f1-b88c-4b95-91be-866416ec00ec`). [Redeploy von v1.1.3 nach Destroy/Apply erfolgreich](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/41813829011): Beide Versionen 1.1.3, vier gleichzeitige Spiele, vollständige Partie, ungültige Eingabe und Neustart im öffentlichen Chrome-Test erfolgreich. Abschließender OpenTofu-Plan: keine Änderungen. [JSON](evidence/cloud-after-destroy.json), [Screenshot](evidence/cloud-after-destroy.png).

Noch nicht abgeschlossen: tatsächliches Ende und Neustart der AWS-Academy-Lab-Session. Dafür ist die angemeldete Learner-Lab-Oberfläche erforderlich; der normale EC2-Neustart wurde bereits separat geprüft.

Ablauf, Secrets-Konzept und Promotion-Strategie: [deployment.md](deployment.md).
