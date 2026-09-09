Testkonzept – TicTacTest

Projekt: 450-tictactest-mvk
Repository: raphaelhuerzele/450-tictactest-mvk
Stand: 09.09.2026
Bezugsstand: Branch refactor-tictactoe-tests, Pull Request #1

1. Zweck des Testkonzepts

Dieses Testkonzept dokumentiert den aktuell vorhandenen IST-Zustand der Tests im TicTacTest-Projekt. Es beschreibt ausschliesslich Tests, Teststrukturen, Testdaten und Automatisierungen, die im betrachteten Projektstand tatsächlich vorhanden sind.

Nicht vorhandene oder erst zukünftig gewünschte Tests werden nicht als Bestandteil der Teststrategie dargestellt. Bestehende Testlücken werden separat als aktueller Abdeckungsstand dokumentiert.

2. Testobjekt

Das Testobjekt ist eine Java-Implementierung von TicTacToe mit einem eindimensionalen Spielfeld aus neun Feldern.

Zum betrachteten Anwendungscode gehören insbesondere:

TicTacToeMain

Start des Spiels

Spielablauf über play(...)

Gewinnerkennung über isWin(...)

textuelle Darstellung des Spielfelds über toString(...)

TicTacToePlayer

Schnittstelle für Spieler

Enum Stone mit CROSS und CIRCLE

Ermittlung des Gegenspielsteins über opponent()

GreedyPlayer

wählt das erste freie Feld von links oben nach rechts unten

HumanPlayer

liest einen Spielzug über die Standardeingabe ein

Das Spielfeld besteht aus neun Positionen mit den Indizes 0 bis 8:

0 | 1 | 2
--+---+--
3 | 4 | 5
--+---+--
6 | 7 | 8

3. IST-Zustand der Testumgebung

3.1 Programmiersprache und Build

Das Projekt verwendet Java und Gradle. Der Gradle Wrapper ist Bestandteil des Repositories und wird sowohl lokal als auch in der CI verwendet.

3.2 Testframeworks

Im Projekt sind aktuell folgende Testabhängigkeiten konfiguriert:

Werkzeug

Version

Verwendung

JUnit Jupiter

5.11.4

Testausführung, @Test, @BeforeEach, @ParameterizedTest und @MethodSource

AssertJ

3.27.3

Assertions in den Tests

JUnit Platform Launcher

1.11.4

Laufzeitunterstützung für die JUnit-Plattform

Die Gradle-Testtask verwendet die JUnit Platform.

3.3 Continuous Integration

Im Repository ist ein GitHub-Actions-Workflow vorhanden. Er wird bei push und pull_request ausgeführt.

Die CI verwendet aktuell:

Ubuntu (ubuntu-latest)

Java 25, Distribution Zulu

Gradle Wrapper

Build- und Testkommando:

./gradlew clean build --no-daemon

Damit werden die automatisierten Tests bei Pushes und Pull Requests im Build ausgeführt.

4. Teststrategie

Im aktuellen Projekt werden automatisierte Tests auf Klassen- und Methodenebene verwendet. Die Tests konzentrieren sich auf deterministisch prüfbare Spiellogik.

4.1 Unit Tests

Der überwiegende Teil der vorhandenen Tests prüft einzelne Methoden oder Klassen isoliert:

Stone.opponent()

TicTacToeMain.isWin(...)

GreedyPlayer.play(...)

Dabei werden Eingaben direkt vorbereitet, die getestete Methode aufgerufen und das Resultat mit AssertJ geprüft.

4.2 Parametrisierte Tests

Für mehrere gleichartige Board-Konstellationen werden JUnit-5-Parameterized-Tests mit @MethodSource eingesetzt.

Aktuell existieren zwei Datenquellen:

winBoardFixtures() für Gewinn- und Nicht-Gewinn-Konstellationen

greedyPlayerFixtures() für unterschiedliche Belegungen des Spielfelds

Dadurch wird dieselbe Testlogik mit mehreren Eingabedaten ausgeführt.

4.3 Spielablauftest

Der vollständige Spielablauf wird in einem automatisierten Test über TicTacToeMain.play(...) geprüft. Dafür werden zwei deterministische TicTacToePlayer-Implementierungen als Lambdas erzeugt. Die Spielzüge sind vorgegeben und führen zu einem Sieg von CROSS.

Dieser Test prüft mehrere Teile des Spielablaufs gemeinsam und ist damit breiter als die isolierten Methodentests.

4.4 Assertions

Die fachlichen Assertions werden mit AssertJ formuliert, beispielsweise durch Vergleiche mit erwarteten Werten oder Wahrheitswerten.

5. Teststruktur

Die automatisierten Tests befinden sich in:

src/test/java/ch/bbw/m450/tictactoe/TicTacToeTest.java

Die Testklasse verwendet aktuell folgende Struktur:

5.1 Fixture mit @BeforeEach

Vor jedem passenden Test wird ein neuer GreedyPlayer erzeugt:

@BeforeEach
void setUp() {
greedyPlayer = new GreedyPlayer();
}

Damit besitzt jeder Test eine frische Instanz und ist nicht von Zustandsänderungen eines vorherigen Tests abhängig.

5.2 Testdaten-Fixtures

Mehrere Testfälle werden als Stream<Arguments> bereitgestellt.

Die Methode winBoardFixtures() enthält aktuell zehn Board-Konstellationen:

drei horizontale Gewinnlinien

drei vertikale Gewinnlinien

zwei diagonale Gewinnlinien

zwei Konstellationen ohne Gewinnlinie

Die Methode greedyPlayerFixtures() enthält aktuell drei Konstellationen für die Auswahl des ersten freien Feldes.

5.3 Helper für Spielfelder

Der Helper board(String layout) wandelt eine kompakte Zeichenkette in ein Stone[] um.

Dabei gilt:

X = Stone.CROSS

O = Stone.CIRCLE

. = leeres Feld (null)

Beispiel:

XXX.O.O..

entspricht:

X X X
. O .
O . .

Der Helper akzeptiert genau neun Zeichen und bricht bei unbekannten Zeichen mit einer IllegalArgumentException ab.

5.4 Helper für deterministische Spieler

Der Helper playerWithMoves(int... moves) erzeugt einen TicTacToePlayer, der vorgegebene Positionen der Reihe nach zurückgibt.

Dieser Helper wird im vorhandenen Test des kompletten Spielablaufs eingesetzt.

6. Testziele

Die Testziele beschreiben, was durch die aktuell implementierten Tests nachgewiesen wird.

TZ-01 – Gegenspielstein bestimmen

Es wird geprüft, dass CROSS.opponent() den Wert CIRCLE und CIRCLE.opponent() den Wert CROSS zurückgibt.

TZ-02 – Gewinnzustände erkennen

Es wird geprüft, dass TicTacToeMain.isWin(...) vorhandene Dreierreihen auf dem Spielfeld erkennt.

Abgedeckt sind aktuell:

obere horizontale Reihe

mittlere horizontale Reihe

untere horizontale Reihe

linke vertikale Spalte

mittlere vertikale Spalte

rechte vertikale Spalte

Hauptdiagonale

Gegendiagonale

Zusätzlich wird geprüft, dass zwei Board-Konstellationen ohne passende Dreierreihe nicht als Gewinn erkannt werden.

TZ-03 – Verhalten des GreedyPlayer

Es wird geprüft, dass der GreedyPlayer das erste freie Feld auswählt.

Aktuell werden drei Belegungssituationen ausgeführt:

komplett leeres Spielfeld

erste drei Felder belegt

nur das letzte Feld frei

TZ-04 – Vollständiger Spielablauf mit Sieg von CROSS

Es wird geprüft, dass TicTacToeMain.play(...) bei einer fest vorgegebenen Zugfolge einen Sieg von CROSS erkennt und Stone.CROSS zurückgibt.

7. Testfälle und Bezug zu den Testzielen

Testfall

Testziel

Automatisierter Test / Fixture

Eingabe / Situation

Erwartetes Resultat

TC-01

TZ-01

opponentShouldReturnTheOtherStone

CROSS und CIRCLE

jeweils der andere Stein

TC-02

TZ-02

winBoardFixtures

XXX.O.O.., CROSS

Gewinn = true

TC-03

TZ-02

winBoardFixtures

O..XXX.O., CROSS

Gewinn = true

TC-04

TZ-02

winBoardFixtures

O.O...XXX, CROSS

Gewinn = true

TC-05

TZ-02

winBoardFixtures

OXXO.XO.X, CIRCLE

Gewinn = true

TC-06

TZ-02

winBoardFixtures

XOXXO..O., CIRCLE

Gewinn = true

TC-07

TZ-02

winBoardFixtures

XXO.XO..O, CIRCLE

Gewinn = true

TC-08

TZ-02

winBoardFixtures

OXX.OX..O, CIRCLE

Gewinn = true

TC-09

TZ-02

winBoardFixtures

O.X.X.XO., CROSS

Gewinn = true

TC-10

TZ-02

winBoardFixtures

XOXOXOOX., CROSS

Gewinn = false

TC-11

TZ-02

winBoardFixtures

XOXOXOOX., CIRCLE

Gewinn = false

TC-12

TZ-03

greedyPlayerFixtures

.........

Feld 0

TC-13

TZ-03

greedyPlayerFixtures

XOX......

Feld 3

TC-14

TZ-03

greedyPlayerFixtures

XOXOXOXO.

Feld 8

TC-15

TZ-04

crossPlayerShouldWinACompleteGame

CROSS: 0,1,2; CIRCLE: 3,4

Gewinner = CROSS

Damit bestehen im betrachteten Teststand vier formulierte Testziele mit insgesamt 15 ausgeführten Testfall-Konstellationen.

8. Testdaten

Die Testdaten werden direkt im Testcode definiert und benötigen keine externen Dateien oder Datenbanken.

Für Spielfeldtests werden Strings mit neun Zeichen verwendet. Dadurch sind die Board-Konstellationen kompakt und reproduzierbar.

Für den vollständigen Spielablauf werden feste Zuglisten verwendet:

CROSS:  0, 1, 2
CIRCLE: 3, 4

Die Tests sind dadurch deterministisch und verwenden keine Zufallsdaten.

9. Aktueller Abdeckungsstand

Folgende produktive Funktionen oder Situationen besitzen im betrachteten Stand einen automatisierten Test:

Bereich

Aktuell automatisiert getestet

Stone.opponent()

Ja

TicTacToeMain.isWin(...)

Ja

alle acht Gewinnlinien

Ja

Nicht-Gewinn-Konstellationen

Ja

GreedyPlayer.play(...) bei freien Feldern

Ja

TicTacToeMain.play(...) mit Sieg von CROSS

Ja

TicTacToeMain.toString(...)

Nein

vollständiges Unentschieden über play(...)

Nein

vollständiges Spiel mit Sieg von CIRCLE

Nein

ungültiger Zug ausserhalb von 0–8

Nein

Zug auf bereits belegtes Feld

Nein

identische Spielerinstanz für X und O

Nein

GreedyPlayer bei vollständig belegtem Board

Nein

HumanPlayer.play(...) / Konsoleneingabe

Nein

Schutz vor Seiteneffekten durch die Board-Kopie in play(...)

Nein

Diese Tabelle beschreibt ausschliesslich den aktuellen IST-Zustand. Die mit „Nein“ gekennzeichneten Punkte sind im betrachteten Projektstand nicht durch automatisierte Tests abgedeckt.

10. Testausführung

Lokal unter Linux/macOS

./gradlew clean test

oder für den vollständigen Build:

./gradlew clean build

Lokal unter Windows

.\gradlew.bat clean test

In GitHub Actions

Bei push und pull_request wird automatisch ausgeführt:

./gradlew clean build --no-daemon

11. Testresultate und Nachvollziehbarkeit

Die Tests sind über sprechende Methodennamen sowie über Namen der parametrisierten Datensätze nachvollziehbar.

Beispiele für Testnamen der parametrisierten Gewinnprüfung sind:

cross wins in top row
circle wins in left column
circle wins on main diagonal
cross has no winning line

JUnit zeigt diese Bezeichnungen bei der Ausführung der jeweiligen Parameterkombination an.

Die Zuordnung zwischen Testzielen und Testfällen ist in Kapitel 7 dokumentiert.

12. Aktueller Repository-Status zum Dokumentationsstand

Der für dieses Testkonzept betrachtete refaktorierte Testcode befindet sich am 09.09.2026 im Branch refactor-tictactoe-tests und ist Gegenstand von Pull Request #1 gegen main.

Der Pull Request enthält eine Änderung an der Testdatei und wurde von Reviewer bernedom genehmigt. Zum Zeitpunkt dieses Testkonzepts ist der Pull Request noch offen.

Damit beschreibt dieses Dokument den aktuellen Teststand dieses Branches und nicht den älteren Teststand auf main vor dem Merge des Pull Requests.

13. Zusammenfassung

Der aktuelle Testbestand des TicTacTest-Projekts verwendet JUnit 5 und AssertJ. Die Spiellogik wird mit normalen und parametrisierten Tests geprüft. Wiederverwendbare Fixtures und Helper reduzieren doppelte Testdaten und ermöglichen mehrere Board-Konstellationen mit derselben Testlogik.

Aktuell werden die Gegnerfarbe, alle acht Gewinnlinien, Nicht-Gewinn-Konstellationen, drei Situationen des GreedyPlayer sowie ein vollständiger Spielablauf mit Sieg von CROSS automatisiert geprüft. Die Tests werden zusätzlich durch GitHub Actions bei Pushes und Pull Requests ausgeführt.

Dieses Testkonzept dokumentiert den IST-Zustand des Projekts zum angegebenen Stand.