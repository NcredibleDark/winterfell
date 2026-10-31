## Winterfell-Multimenü v1.4.0 – Glasierte-Keramik-Ghost-Vorschau

Minecraft 26.3, Fabric, Java 25.

### Neu: Keramik-Ghost
- Optionaler, separat aktivierbarer **Keramik-Ghost: AN/AUS**-Button im K-Menü.
- Wenn glasierte Keramik in der Haupthand gehalten und ein Block anvisiert wird, erscheint an der tatsächlichen Platzierungsposition ein halbtransparentes 3D-Modell der Keramik.
- Die Ausrichtung entspricht der Minecraft-Platzierung: Die glasierte Keramik zeigt in die zur Blickrichtung entgegengesetzte horizontale Richtung. Drehungen werden live berücksichtigt.
- Die Vorschau übernimmt das gerenderte Blockmodell aus dem aktiven Resource-Pack, einschließlich des glasierten Musters.
- Die Vorschau platziert keine Blöcke, verändert keine Weltzustände und sendet keine neuen Netzwerkpakete.
- In der aktivierten Random-Palette ist die Ghost-Vorschau ausgeblendet, weil die zufällige Blockwahl den sichtbaren Block vor dem Platzieren ändern könnte. Mit **J** die Palette bei Bedarf temporär deaktivieren.
- Standardmäßig AUS, damit das Verhalten der bewährten v1.3.4 zunächst unverändert ist.

### Bestehende Funktionen
Smart Random, Paletten-Nachfüllen, Hotbar-Refill, Auto Tool Switch, alle HUD-Funktionen und Tastenbelegungen bleiben erhalten. Version 1.3.4 bleibt als eigenes GitHub Release bestehen.

### Installation
`winterfell-random-palette-1.3.4.jar` entfernen und `winterfell-random-palette-1.4.0.jar` in den Mods-Ordner einsetzen. Fabric API und Java 25 erforderlich, Serverinstallation nicht notwendig.

Der automatische GitHub-Build prüft die Kompilierung und stellt eine SHA-256-Prüfsumme bereit. Die Darstellung mit Iris/Shadern muss im Spiel noch getestet werden.
