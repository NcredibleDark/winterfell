# Winterfell Random Palette

Clientseitige Fabric-Mod für **Minecraft 26.3**. Sie erleichtert das Texturieren von Mauern und anderen Bauwerken durch zufällige, gewichtete Blockplatzierung.

## Installation

1. In den **GitHub Releases** die Datei `winterfell-random-palette-VERSION.jar` herunterladen (nicht den automatisch erzeugten Source-Code-ZIP).
2. Datei in den `mods`-Ordner des Minecraft-26.3-Fabric-Profils legen.
3. **Fabric API** muss installiert sein. Java **25** wird benötigt.
4. Alte Versionen der Mod vor dem Start entfernen.

## Bedienung

- `K`: Auswahl-GUI öffnen.
- Im GUI Baublöcke aus dem Inventar anklicken und die relativen Gewichte mit `+` / `-` einstellen.
- Die ausgewählten Blöcke müssen für die tatsächliche Platzierung in der **Hotbar** liegen.
- `J`: Zufallsplatzierung ein- oder ausschalten.
- Beim normalen Platzieren wird ausschließlich zwischen den ausgewählten, in der Hotbar vorhandenen Baublöcken gewechselt. Werkzeuge sind von der Auswahl ausgeschlossen.
- Konfiguration: `config/winterfell-random-palette.json`.

## Downloads und Sicherheit

Der offizielle Download ist ein **GitHub Release Asset direkt als .jar über HTTPS**, keine temporäre Actions-ZIP. Jede Version hat außerdem eine `.jar.sha256` Prüfsumme.
Auf Windows lässt sich die Prüfsumme in PowerShell mit `Get-FileHash .\winterfell-random-palette-VERSION.jar -Algorithm SHA256` überprüfen.

Ein GitHub Release ist kein Ersatz für eine Prüfung der Dateien. Chrome kann weiterhin Sicherheitswarnungen anzeigen. Ein privates Repository erfordert zudem GitHub-Anmeldung zum Download.

## Entwicklung

Ein Push auf `build/fabric-random-palette` kompiliert die Mod und veröffentlicht automatisch ein neues GitHub Release, **wenn `mod_version` in `gradle.properties` noch nicht veröffentlicht wurde**. Bestehende Releases werden absichtlich nicht überschrieben. Für Updates die Versionsnummer erhöhen.

Dieses Projekt läuft auf dem Branch `build/fabric-random-palette`, damit der Hauptzweig der Android-App unverändert bleibt.
