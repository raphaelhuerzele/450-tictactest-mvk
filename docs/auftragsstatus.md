# Abgleich mit den drei Webapp-Aufträgen

Prüfstand: 25.11.2026. Grundlage sind die drei HTML-Aufträge, Version `261103-71bf6d91` vom 03.11.2026. Historische Cloud-Nachweise vom 04.11.2026 stehen in [webapp-nachweise.md](webapp-nachweise.md). Sie sind keine aktuelle Erreichbarkeitsprüfung.

Die technische Umsetzung der drei Pflichtaufträge ist abgeschlossen und veröffentlicht. Nach dem Kurswechsel wurde die Anwendung im neuen Learner Lab wiederhergestellt. Der aktuelle erfolgreiche Deployment- und Browsernachweis ist [Lauf 45563031258](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/45563031258). Aktuelle Anwendung: <http://35.171.13.1/>. Erreichbarkeit setzt eine aktive Lab-Instanz voraus; lokale Schul-Netzfilter können den Zugriff blockieren.

| Auftrag | Implementierung und Nachweise |
| --- | --- |
| 1: lokale Web-Konsole | ttyd startet direkt das unveränderte Release-JAR. Separates statisches Frontend, konfigurierbare URLs, HTTP-Versionen und Release-Archive sind vorhanden. Spielende, ungültige Eingabe, Neustart und getrennte JVMs sind dokumentiert. Siehe [webkonsole.md](webkonsole.md). |
| 2: Infrastruktur | EC2, Elastic IP, Instance Profile, gepinnte Runtime, nicht privilegierter systemd-Dienst, minimales Netzwerk und Outputs sind implementiert. Instanzneustart, user_data-Ersatz und Destroy/Apply wurden laut historischem Protokoll geprüft. nginx ist ein zulässiges Frontend-Hosting; S3 ist im Auftrag nur ein Beispiel. |
| 3: Release-Deployment | Release-Workflow ruft Deploy-Workflow auf; Release-Vergleich wählt Komponenten. Release-Assets werden geprüft und ohne Neubau installiert. HTTP-Versionstests, Serialisierung, Konfigurationsschutz und manueller Rollback sind vorhanden. Historische Nachweise zeigen Frontend-only, Backend-only, beide und main ohne Deployment. Secrets-Konzept und Promotion sind in [deployment.md](deployment.md) dokumentiert. |

## Migration und Cloud-Abnahme am 25.11.2026

Das vorherige AWS-Academy-Lab ist abgelaufen. Nach Einladung in den neuen Kurs wurde die Umgebung im Konto `912178736500`, Region `us-east-1`, neu erstellt. Die neuen temporären Credentials liegen lokal im Profil `default` und verschlüsselt im bestehenden GitHub-Environment `lab`; der private SSH-Schlüssel wurde nicht gespeichert oder benötigt.

OpenTofu verwendet den separaten Workspace `lab-912178736500`; die alten Konto-States wurden nicht überschrieben oder für das neue Konto verwendet. Amazon Linux 2023 wurde einmal über den öffentlichen SSM-AMI-Parameter aufgelöst und als `ami-0aacbf72109c53c0c` lokal in der nicht eingecheckten tfvars-Datei gepinnt. `init` erfolgreich; geprüfter Plan: sechs Ressourcen erstellen, keine ändern oder löschen. `apply`: sechs Ressourcen erstellt. Nachfolgender Plan mit `-detailed-exitcode`: Exit 0, keine Änderungen.

| Output | Neuer Wert |
| --- | --- |
| Instanz | `i-0117a365b0447c806` |
| Frontend | <http://35.171.13.1/> |
| Terminal | <http://35.171.13.1/terminal/> |
| Backend-Version | <http://35.171.13.1/version> |
| Elastic-IP-Allocation | `eipalloc-02392ca88dab41476` |

Die frischen Outputs wurden in GitHub übernommen, alle drei Lab-Secrets gemeinsam erneuert und Release-Deployments bleiben aktiviert. SSM meldet die Instanz Online. Derselbe bestehende Release v1.1.3 wurde ohne Neubau über den manuellen Deploy-Workflow installiert. [Deployment 45563031258](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/45563031258) ist vollständig erfolgreich: öffentliche HTTP-Versionen, beide Komponenten 1.1.3, vollständige Partie bis CROSS gewinnt, vier gleichzeitige Sitzungen, ungültige Eingabe und Neustart.

Persistierte Nachweise: [Deployment-JSON](evidence/deployment-20261125.json), [Browser-JSON](evidence/cloud-20261125.json), [Screenshot](evidence/cloud-20261125.png). Die früheren Adressen in den folgenden Abschnitten gehören zu historischen Nachweisen und sind nicht die aktuellen Endpoints.

## Historische Cloud-Abnahme am 18.11.2026

Im aktuellen AWS-Konto `699438354439` wurde die Infrastruktur im getrennten Workspace `lab-699438354439` erstellt. Instanz: `i-0f03ea30528917fcb`; Elastic IP: `44.193.19.185`. Der alte State gehört zu einem anderen Konto und wurde nicht überschrieben. Die Subnetzauswahl berücksichtigt die angebotenen Instanztypen je Zone. Der Deploy-Client wartet jetzt auf cloud-init, bevor er den Installer aufruft.

Die Anmeldung funktioniert mit dem lokalen AWS-Profil `default`. GitHub-Environment-Variables wurden auf die neuen Outputs umgestellt, alle drei temporären AWS-Secrets mit ausdrücklicher Zustimmung erneuert und `AWS_DEPLOY_ENABLED=true` gesetzt. Credentials und privater SSH-Schlüssel sind keine Repository-Dateien.

Die Releases v1.1.0 und v1.1.3 wurden nacheinander lokal über `node scripts/web/deploy.mjs TAG --force --verify-on-instance` auf dieselbe EC2 deployt. Beide Komponenten wurden je Release erfolgreich über den HTTP-Endpunkt auf der Instanz geprüft: [A](evidence/manual-v1.1.0-20261118.json), [B](evidence/manual-v1.1.3-20261118.json). Zwischen den App-Deployments erfolgte kein Infrastruktur-Apply. Der abschließende OpenTofu-Plan meldet `No changes. Your infrastructure matches the configuration.` Die aktive Version ist zuletzt 1.1.3.

Der lokale vollständige Browserablauf `manual-acceptance.ps1` kann hier nicht bestehen: Der Schul-Webfilter blockiert die öffentliche IP als FortiGuard-Kategorie Unrated. Es wurde keine Ausnahme oder Umgehung aktiviert. `--verify-on-instance` prüft ausschließlich HTTP über SSM und ist ausdrücklich kein Browsernachweis.

Der erste [GitHub-Workflow für die neue Instanz](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/44269433505) hat Installation und öffentliche HTTP-Versionsprüfung erfolgreich abgeschlossen, scheiterte aber im vorinstallierten Chrome bei `page.goto` mit `net::ERR_BLOCKED_BY_CLIENT`. Der Test verwendet jetzt das mit Playwright versionierte Chromium statt des vorinstallierten Systembrowsers. Es werden keine Schutzfunktionen abgeschaltet. Fehler speichern künftig Diagnose und Screenshot.

Der anschließende [Deployment-Lauf 44270388612](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/44270388612) ist vollständig erfolgreich: öffentliche HTTP-Versionsprüfungen und echtes Browser-Spiel, Frontend/Backend jeweils 1.1.3, vier gleichzeitige Sitzungen, vollständige Partie bis CROSS gewinnt, ungültige Eingabe und Neustart. Persistierte Nachweise: [Browser-JSON](evidence/cloud-20261118.json), [Screenshot](evidence/cloud-20261118.png).

Der Implementierungscommit `e324730` ist nach main gepusht. [CI](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/44270386479), [Infrastrukturvalidierung](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/44270386638) und [Coverage-Veröffentlichung](https://github.com/raphaelhuerzele/450-tictactest-mvk/actions/runs/44270387429) sind erfolgreich. Der main-Push selbst hat kein Deployment ausgelöst; der genannte Deployment-Lauf wurde ausdrücklich manuell für den bestehenden Release gestartet.

Die lokalen Prüfungen am 18.11.2026 waren erfolgreich: zwei Tests zur Änderungserkennung, Syntaxprüfung des PowerShell-Abnahmeablaufs, `tofu fmt -check`, `tofu validate` und `git diff --check`. Diese Prüfungen ersetzen keinen ausgeführten Cloud-Deploy.

Beim bereits dokumentierten echten Lab-Sessionwechsel blieb EC2 zunächst gestoppt. Nach manuellem Start derselben Instanz liefen Adresse, gespeicherte Version und Dienste wieder. Das ist eine beobachtete Lab-Einschränkung; der automatische Dienststart nach EC2-Boot ist umgesetzt. Der Auftrag verlangt hier ein Experiment mit ehrlicher Ergebnisdokumentation, nicht das automatische Starten einer beendeten Lab-Session.

Die optionalen Erweiterungen (eigenes HTTP-Spielbackend und Spielprotokolle in S3) sind für die Pflichtabgabe nicht erforderlich. Die persönliche Erklärung der Lösung und die schulische Abgabe bleiben bei der abgebenden Person.
