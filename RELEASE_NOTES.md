## Winterfell-Multimenü v1.4.1 – Server-Schnellzugriffe

Minecraft Fabric 26.3 / Java 25 / rein clientseitig.

### Neue Schnellzugriffe im K-Menü
- **Werkbank** sendet `/wb`.
- **Enderchest** sendet `/ec`.
- **Mülleimer** sendet `/trash`.
- Die drei neuen Buttons stehen nebeneinander unten im Multimenü. Der bestehende **Fertig**-Button befindet sich jetzt oben.
- Beim Klick schließt sich das Multimenü; der Client sendet den gewählten Befehl an den verbundenen Server. Der Server übernimmt sämtliche Berechtigungen und öffnet das zugehörige Interface.
- Keine Serverinstallation notwendig. Auf anderen Servern funktionieren die Buttons nur, wenn dort die entsprechenden Befehle existieren und erlaubt sind.

### Weiterhin unverändert
- Random-Palette, Smart Random und fest integriertes Paletten-Nachfüllen
- Separates Hotbar-Refill, Auto Tool Switch, an-/ausschaltbares HUD mit U
- Glasierte-Keramik-Ghost-Vorschau und alle bisherigen Einstellungen
- Version 1.4.0 bleibt als separate Rückfallversion verfügbar.

### Installation
Alte `winterfell-random-palette-1.4.0.jar` entfernen und `winterfell-random-palette-1.4.1.jar` einsetzen. Fabric API und Java 25 erforderlich.

GitHub Actions kompiliert die Mod und prüft eine SHA-256-Prüfsumme. Die neuen Buttons müssen zur Bestätigung mit den echten Serverbefehlen auf Winterfell getestet werden.
