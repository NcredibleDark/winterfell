# Winterfell Random Palette

Clientseitige Fabric-Mod für **Minecraft 26.3**. Sie erleichtert das Texturieren von Mauern und anderen Bauwerken durch zufällige, gewichtete Blockplatzierung.

## Installation

1. In den **GitHub Releases** die Datei `winterfell-random-palette-VERSION.jar` herunterladen (nicht den automatisch erzeugten Source-Code-ZIP).
2. Datei in den `mods`-Ordner des Minecraft-26.3-Fabric-Profils legen.
3. **Fabric API** muss installiert sein. Java **25** wird benötigt.
4. Alte Versionen der Mod vor dem Start entfernen.

## Bedienung

- **Cosmetics-Garderobe**: Im K-Menü auf **Cosmetics / Garderobe** klicken. Eigene Partikel-Cosmetics wählen: Heiligenschein, rote oder blaue Partikel-Flügel, Flammenaura oder keines. Andere Mod-Spieler mit dem gleichen Dienst sehen die Auswahl, sobald die optionale HTTPS-Synchronisierung eingerichtet ist. Details unten.
- `K`: **Winterfell-Multimenü** öffnen. Unten befinden sich die drei **Server-Schnellzugriffe**: **Werkbank** (`/wb`), **Enderchest** (`/ec`) und **Mülleimer** (`/trash`). Ein Klick schließt das Multimenü und sendet den jeweiligen Befehl an den Server. Das funktioniert ohne zusätzliche Server-Mod, sofern der Server diese Befehle anbietet und der Spieler sie ausführen darf. Der bisherige **Fertig**-Button wurde in die obere Menüzeile verschoben.
- Im GUI **nur Blöcke aus der untersten Reihe (Hotbar)** anklicken und ihre relativen Gewichte mit `+` / `-` einstellen. Die oberen drei Inventarreihen sind ausgegraut und nicht auswählbar.
- Beim Öffnen und während der Anzeige werden Einträge automatisch entfernt, wenn der Blocktyp in keinem der neun Hotbar-Slots mehr liegt. Im Hauptinventar liegende Stacks dienen nur als Nachfüllreserve und zählen nicht zur Gewichtung.
- Die ausgewählten Blöcke müssen für die tatsächliche Platzierung in der **Hotbar** liegen.
- `J`: Zufallsplatzierung ein- oder ausschalten.
- `U`: Das HUD-Overlay direkt ein-/ausblenden (Tastenbelegung in den Minecraft-Steuerungseinstellungen anpassbar). Im `K`-Menü gibt es dafür weiterhin den eigenen **HUD: AN/AUS**-Button.
- **Keramik-Ghost-Vorschau** (standardmäßig AUS): Im `K`-Menü aktivieren. Mit glasierter Keramik in der Haupthand erscheint ein halbtransparentes Blockmodell mit dem tatsächlichen Muster und der vom aktuellen Blickwinkel bestimmten Drehung am Zielort. Die Vorschau wird nur bei deaktivierter Random-Palette angezeigt, da deren Zufallsauswahl sonst nicht zuverlässig vorhergesagt werden kann. Kein Server-Plugin und kein automatisches Platzieren.
- **Smart Random:** Optional per `K`-Menü aktivieren. Vergangene Blocktypen erhalten eine geringere Wahrscheinlichkeit, sodass Texturen abwechslungsreicher wirken. Die ursprünglich festgelegten Gewichtungen bleiben die Basis.
- **Bau-HUD:** Die kompakte, abschaltbare Anzeige **Winterfell-Multimenü** zeigt Palettenmodus, verfügbare Blöcke sowie die farbigen AN/AUS-Zustände von **Hotbar-Nachfüllen** und **Werkzeugwechsel**. Das automatische Paletten-Nachfüllen ist bewusst nicht extra aufgeführt.
- Beim normalen Platzieren wird ausschließlich zwischen den ausgewählten, in der Hotbar vorhandenen Baublöcken gewechselt. Werkzeuge sind von der Auswahl ausgeschlossen.
- **Paletten-Nachfüllen ist immer aktiv, sobald die Random-Palette eingeschaltet ist.** Sobald ein ausgewählter Hotbar-Blockstack aufgebraucht ist, zieht die Mod automatisch denselben Blocktyp aus dem Inventar nach. Dafür gibt es keinen separaten Schalter mehr. Zu Beginn mindestens einen Stack der gewünschten Blocktypen in die Hotbar legen. Nachgefüllt wird nur während des Spielens, nicht bei geöffneten Inventarfenstern.
- **Hotbar-Refill** (standardmäßig AUS) bleibt als separat einstellbares Zusatzfeature erhalten. Es füllt beliebige verbrauchte, stapelbare Hotbar-Items mit exakt gleichen Items aus dem Inventar nach – unabhängig von der Random-Palette.
- **Auto Tool Switch** (standardmäßig AUS) wählt beim Blockabbau ein geeignetes Werkzeug aus der Hotbar, ohne das Inventar zu durchsuchen. Beide Extras haben eigene Schalter im K-Menü.
- Konfiguration: `config/winterfell-random-palette.json`.

## Downloads und Sicherheit

Der offizielle Download ist ein **GitHub Release Asset direkt als .jar über HTTPS**, keine temporäre Actions-ZIP. Jede Version hat außerdem eine `.jar.sha256` Prüfsumme.
Auf Windows lässt sich die Prüfsumme in PowerShell mit `Get-FileHash .\winterfell-random-palette-VERSION.jar -Algorithm SHA256` überprüfen.

Ein GitHub Release ist kein Ersatz für eine Prüfung der Dateien. Chrome kann weiterhin Sicherheitswarnungen anzeigen. Öffentliche Releases können ohne GitHub-Anmeldung heruntergeladen werden.

## Entwicklung

Ein Push auf `build/fabric-random-palette` kompiliert die Mod und veröffentlicht automatisch ein neues GitHub Release, **wenn `mod_version` in `gradle.properties` noch nicht veröffentlicht wurde**. Bestehende Releases werden absichtlich nicht überschrieben. Für Updates die Versionsnummer erhöhen.

Dieses Projekt läuft auf dem Branch `build/fabric-random-palette`, damit der Hauptzweig der Android-App unverändert bleibt.


## Winterfell-Cosmetics (v1.5.0 – erste Version)

Die Cosmetics sind zunächst **Partikel-Cosmetics** (kein massives 3D-Modell). Die eigene Auswahl und der Schalter **Andere Cosmetics anzeigen** funktionieren direkt lokal. Die Darstellung ist nur clientseitig; Vanilla- und Bedrock-Spieler sehen sie nicht.

**Damit Spieler mit der Mod gegenseitig die Cosmetics sehen**, muss der unabhängige Dienst in `cosmetics-service/server.py` auf einer HTTPS-Adresse erreichbar sein. Das ist eine eigenständige Python-Anwendung, **kein Paper-Plugin**. Ohne eingerichteten Dienst zeigt der Client nur das eigene Cosmetic.

**Einrichtung (Serveradministrator):**
1. Python 3.10+ auf einem Webhost installieren. `python3 cosmetics-service/server.py serve` startet lokal auf `127.0.0.1:8765`; davor einen HTTPS-Reverse-Proxy einrichten. Nicht unverschlüsselt ins Internet stellen.
2. Für jeden Spieler die echte Online-Minecraft-UUID ermitteln und `python3 cosmetics-service/server.py issue UUID` ausführen. Der ausgegebene Schlüssel gehört **nur diesem Spieler**. Bei erneuter Ausgabe wird sein alter Schlüssel ungültig.
3. Jeder Spieler startet die Mod einmal und trägt in `config/winterfell-random-palette.json` beispielsweise Folgendes ein (bestehende Optionen nicht löschen):
   - `"cosmeticsServiceUrl": "https://cosmetics.example.com"`
   - `"cosmeticsAccessToken": "INDIVIDUELLER_SCHLUESSEL"`
4. Minecraft neu starten oder die Mod-Konfiguration vor dem Start bearbeiten. Im Cosmetics-Menü steht anschließend der Synchronisierungsstatus.

Der Dienst speichert ausschließlich Spieler-UUID, Cosmetics-Auswahl und Hash des Zugangsschlüssels in SQLite. **Keine Minecraft-, Microsoft- oder Mojang-Zugangsdaten werden benötigt.** Den privaten Schlüssel und die Konfigurationsdatei nicht weitergeben oder auf GitHub veröffentlichen. Die UUID muss im administrativen Pairing korrekt zugeordnet sein; andernfalls ist der Schlüssel nicht für die eigene UUID gültig.

Im Singleplayer bleibt die Auswahl lokal. Die Kommunikation wird asynchron (ca. alle 5 Sekunden) vorgenommen und ist optional; bei Netzwerkausfall funktioniert das Spiel weiter. Der Betrieb des Dienstes ist nötig, um Änderungen zwischen verschiedenen Clients zu übertragen.
