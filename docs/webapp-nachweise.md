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

## Noch offen: echte AWS-Nachweise

Die vorhandene AWS-Anmeldung wurde mit STS geprüft und als `InvalidClientTokenId` zurückgewiesen. Es wurde kein erfolgreicher AWS-Zugriff, kein apply, kein EC2-Deployment und kein Rollback behauptet. Das GitHub-Repository besitzt bisher keine eingerichteten Lab-Deploy-Secrets/Variables.

| Geforderter Nachweis | Status / nachzuholende Beobachtung |
| --- | --- |
| Release A auf EC2 und S3 | Offen: gültiges Lab-Profil, plan/apply, Outputs und manuellen Deploy ausführen, im Browser spielen |
| Release B ohne Infrastrukturänderung | Offen: zweite Veröffentlichung deployen, Instanz-ID gleich, tofu plan Exitcode 0 |
| Session-Ende/Neustart | Offen: gleiche Elastic IP, systemd aktiv, gleiche Backend-Version prüfen |
| Änderung an user_data | Offen: erwarteten Instanzersatz im Plan prüfen; EIP bleibt, Version muss nach Ersatz erneut deployt werden |
| Destroy/Apply | Offen: ausdrücklich gewollter destruktiver Lab-Test; neue Outputs übernehmen und dieselben Assets erneut deployen |
| Vier selektive Fälle | Offen: Frontend-only, Backend-only, beide und Push ohne Tag mit echten AWS-Workflow-Links dokumentieren |
| Rollback | Offen: zwei Web-Releases bereitstellen, älteren Release per Dispatch installieren und beide Versionen prüfen |
| Kaputtes JAR | Offen: separaten bewusst kaputten Testrelease verwenden, rote Prüfung und Erhalt/Wiederherstellung der letzten guten Version messen |
| Abgelaufene Pipeline-Credentials | Offen: echten fehlgeschlagenen AWS-Job sowie erfolgreichen Secret-Refresh und manuellen Retry nachweisen |

Anleitungen, erwartetes Verhalten und Secrets-Konzept stehen in [deployment.md](deployment.md). Diese offenen Punkte benötigen eine laufende Learner-Lab-Session und werden nicht durch simulierte Erfolgsberichte ersetzt.
