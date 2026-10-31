## Winterfell-Multimenü v1.3.4 – Release-Kandidat

Fabric-Client-Mod für Minecraft 26.3 (Java 25).

### Kleine HUD-Bereinigung
- Die Statuszeile **Paletten-Nachfüllen** wurde vollständig aus dem HUD entfernt, weil diese Funktion seit v1.3.3 zum festen Bestandteil der aktiven Random-Palette gehört.
- Das Overlay ist eine Zeile kürzer und damit kompakter. Die vorhandenen Statuszeilen **Hotbar-Nachfüllen** und **Werkzeugwechsel** rücken nach oben.
- **Winterfell-Multimenü**, Palettenmodus (AUS/ZUFALL/SMART), Anzahl an Blocktypen und Vorrat bleiben sichtbar.

### Bestehende Funktionen unverändert
- Smart Random und gewichtete Auswahl ausschließlich aus der Hotbar.
- Automatisches Nachfüllen ausgewählter Palettenblöcke aus dem Inventar.
- Separat aktivierbares allgemeines Hotbar-Refill und Auto Tool Switch.
- HUD über **U** oder den Button im **K**-Menü an- und ausschaltbar.
- Die Konfigurationsdatei und sämtliche Bedienelemente bleiben gleich.

### Installation
Die bisherige `winterfell-random-palette-1.3.3.jar` im `mods`-Ordner durch `winterfell-random-palette-1.3.4.jar` ersetzen. Fabric API und Java 25 erforderlich; keine Server-Mod nötig.

**Release-Kandidat:** Auf der von Nutzern als funktionierend bestätigten v1.3.3 aufbauend. GitHub Actions prüft die Kompilierung und erstellt eine SHA-256-Prüfsumme; ein abschließender Ingame-Test der HUD-Darstellung ist weiterhin sinnvoll.
