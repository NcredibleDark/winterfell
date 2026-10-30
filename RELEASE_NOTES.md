## Winterfell Random Palette 1.2.1

Minecraft Fabric 26.3 / Java 25.

### Fehlerbehebung: Gewichtung ausschließlich aus der Hotbar
- Im Auswahlmenü ist nur die **unterste Inventarreihe (Hotbar)** anklickbar. Die oberen drei Reihen sind ausgegraut und können nicht mehr zur zufälligen Blockpalette hinzugefügt werden.
- Beim Öffnen und während des Menüs werden ausgewählte Blocktypen entfernt, die in keinem der neun Hotbar-Slots mehr vorhanden sind. Die Seitenzahl und Gewichtungs-Buttons aktualisieren sich entsprechend.
- Gewichtungen verbleibender Hotbar-Blöcke bleiben erhalten; Blöcke im Hauptinventar zählen **nicht** zur Gewichtung.
- Das Hauptinventar bleibt als Nachfüllvorrat verfügbar. Für den gewählten Blocktyp bleibt das automatische Hotbar-Nachfüllen aus 1.1.0 erhalten.
- Allgemeines Hotbar-Refill und Auto Tool Switch aus 1.2.0 bleiben unverändert und separat schaltbar.

### Installation
Die bisherige `winterfell-random-palette-1.2.0.jar` entfernen und `winterfell-random-palette-1.2.1.jar` in den Fabric-26.3-`mods`-Ordner legen. Fabric API und Java 25 erforderlich. Auf dem Server muss nichts installiert werden.

Bei der erstmaligen Anzeige des neuen Menüs werden frühere Paletteneinträge, die nur noch im Hauptinventar liegen, dauerhaft aus der Palette entfernt. Der Build wird automatisiert geprüft; der Ingame-Test steht noch aus.
