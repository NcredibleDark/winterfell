# Winterfell Random Palette

Clientseitige Fabric-Mod für **Minecraft 26.3**. Sie erleichtert das Texturieren von Mauern und anderen Bauwerken durch zufällige, gewichtete Blockplatzierung.

## Installation

1. In den **GitHub Releases** die Datei `winterfell-random-palette-VERSION.jar` herunterladen (nicht den automatisch erzeugten Source-Code-ZIP).
2. Datei in den `mods`-Ordner des Minecraft-26.3-Fabric-Profils legen.
3. **Fabric API** muss installiert sein. Java **25** wird benötigt.
4. Alte Versionen der Mod vor dem Start entfernen.

## Bedienung

- `K`: Auswahl-GUI öffnen.
- Im GUI **nur Blöcke aus der untersten Reihe (Hotbar)** anklicken und ihre relativen Gewichte mit `+` / `-` einstellen. Die oberen drei Inventarreihen sind ausgegraut und nicht auswählbar.
- Beim Öffnen und während der Anzeige werden Einträge automatisch entfernt, wenn der Blocktyp in keinem der neun Hotbar-Slots mehr liegt. Im Hauptinventar liegende Stacks dienen nur als Nachfüllreserve und zählen nicht zur Gewichtung.
- Die ausgewählten Blöcke müssen für die tatsächliche Platzierung in der **Hotbar** liegen.
- `J`: Zufallsplatzierung ein- oder ausschalten.
- **Smart Random:** Optional per `K`-Menü aktivieren. Vergangene Blocktypen erhalten eine geringere Wahrscheinlichkeit, sodass Texturen abwechslungsreicher wirken. Die ursprünglich festgelegten Gewichtungen bleiben die Basis.
- **Bau-HUD:** Ein kompaktes, abschaltbares HUD neben der Hotbar zeigt Palettenmodus, verfügbare Blöcke und den Status der Nachfüll-/Werkzeugfunktionen.
- Beim normalen Platzieren wird ausschließlich zwischen den ausgewählten, in der Hotbar vorhandenen Baublöcken gewechselt. Werkzeuge sind von der Auswahl ausgeschlossen.
- **Paletten-Auto-Nachfüllen** ist im GUI separat schaltbar (standardmäßig AN). Sobald ein so belegter Hotbar-Slot aufgebraucht ist, holt die Mod den gleichen Blocktyp aus dem Inventar nach. Nur beim Spielen, nicht während offener Inventarfenster. Zu Beginn mindestens einen passenden Blocktyp in die Hotbar legen.
- **Hotbar-Refill** (standardmäßig AUS) füllt beliebige verbrauchte, stapelbare Hotbar-Items mit exakt gleichen Items aus dem Inventar nach – unabhängig von der Random-Palette.
- **Auto Tool Switch** (standardmäßig AUS) wählt beim Blockabbau ein geeignetes Werkzeug aus der Hotbar, ohne das Inventar zu durchsuchen. Beide Extras haben eigene Schalter im K-Menü.
- Konfiguration: `config/winterfell-random-palette.json`.

## Downloads und Sicherheit

Der offizielle Download ist ein **GitHub Release Asset direkt als .jar über HTTPS**, keine temporäre Actions-ZIP. Jede Version hat außerdem eine `.jar.sha256` Prüfsumme.
Auf Windows lässt sich die Prüfsumme in PowerShell mit `Get-FileHash .\winterfell-random-palette-VERSION.jar -Algorithm SHA256` überprüfen.

Ein GitHub Release ist kein Ersatz für eine Prüfung der Dateien. Chrome kann weiterhin Sicherheitswarnungen anzeigen. Öffentliche Releases können ohne GitHub-Anmeldung heruntergeladen werden.

## Entwicklung

Ein Push auf `build/fabric-random-palette` kompiliert die Mod und veröffentlicht automatisch ein neues GitHub Release, **wenn `mod_version` in `gradle.properties` noch nicht veröffentlicht wurde**. Bestehende Releases werden absichtlich nicht überschrieben. Für Updates die Versionsnummer erhöhen.

Dieses Projekt läuft auf dem Branch `build/fabric-random-palette`, damit der Hauptzweig der Android-App unverändert bleibt.
