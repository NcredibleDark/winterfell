## Winterfell Random Palette 1.1.0

Fabric-Client-Mod für Minecraft 26.3 (Java 25).

### Neu in 1.1.0
- **Auto-Nachfüllen**: Sobald ein ausgewählter Hotbar-Blockstack aufgebraucht ist, wird derselbe Blocktyp aus dem Hauptinventar (27 Plätze) in diesen Hotbar-Slot verschoben.
- Ein-/Ausschalter **Auto-Nachfüllen** im `K`-Menü, standardmäßig aktiviert.
- Verwendet ausschließlich normale Minecraft-Inventar-SWAP-Klicks, die vom Server verarbeitet werden; keine Server-Mod erforderlich.
- Keine Werkzeuge, kein anderer Blocktyp und keine Gegenstände außerhalb der gewählten Palette werden als Baumaterial platziert.
- Sicherheitsbegrenzung bei Inventartausch und kein automatisches Nachfüllen während geöffneter Inventar-/GUI-Fenster.

### Bedienung
- `K`: Palettenmenü, Gewichtung und Auto-Nachfüllen.
- `J`: Zufallsplatzierung ein-/ausschalten.
- Mindestens einen Stack der ausgewählten Blocktypen zunächst in die Hotbar legen. Weitere passende Stacks können im normalen Inventar liegen.

### Installation
Alte `winterfell-random-palette-1.0.1.jar` entfernen und `winterfell-random-palette-1.1.0.jar` in den `mods`-Ordner legen. Fabric API erforderlich. Kein Server-Plugin nötig.

SHA-256-Prüfsumme liegt als separate `.jar.sha256` bei. Der Java-/Gradle-Build wurde geprüft; Gameplay-Tests auf mehreren Servern stehen noch aus.

Dieses Release befindet sich in einem privaten GitHub-Repository; für den Download wird entsprechender GitHub-Zugriff benötigt.
