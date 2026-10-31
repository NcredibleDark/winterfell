## Winterfell-Multimenü v1.3.3

Fabric-Mod für Minecraft 26.3 / Java 25.

### Änderung: Paletten-Nachfüllen als Standard
- Das **Paletten-Nachfüllen** ist nun ein fester Bestandteil der Random-Palette und funktioniert **automatisch, wenn die Zufallsplatzierung aktiv ist**.
- Der separate **Auto-Nachfüllen**-Button ist aus dem K-Menü entfernt.
- Die alte Konfigurationsoption `autoRefill` wird nicht mehr benötigt; auch wenn sie in einer älteren Konfiguration deaktiviert war, funktioniert das Paletten-Nachfüllen jetzt automatisch.
- Im HUD steht bei **Paletten-Nachfüllen** nun **AUTO** (grün) bei aktiver Random-Palette bzw. **AUS** (rot), wenn die Random-Palette deaktiviert ist.
- Das **allgemeine Hotbar-Refill** bleibt ein eigenständiger Schalter im K-Menü und funktioniert unabhängig von der Random-Palette.

### Unverändert
- Smart Random und Gewichtungen ausschließlich aus der Hotbar.
- Auto Tool Switch, HUD und U-Hotkey.
- Normale Inventar-SWAP-Klicks für Nachschub, keine Server-Mod notwendig.
- Alle vorherigen Releases, einschließlich Version 1.3.2, bleiben verfügbar.

### Installation
Vorherige `winterfell-random-palette-1.3.2.jar` entfernen und `winterfell-random-palette-1.3.3.jar` in den Fabric-26.3-`mods`-Ordner legen. Fabric API und Java 25 erforderlich.

Automatisierter Kompilier-Build mit SHA-256-Prüfsumme; ein Spieltest ist weiterhin empfehlenswert.
