## Winterfell Random Palette 1.1.1

Fabric-Client-Mod für Minecraft 26.3 (Java 25).

### Fehlerbehebung
- Die Gewichtungsliste zeigt nur noch ausgewählte Blocktypen, die **wirklich im Spielerinventar oder in der Hotbar (Slots 0–35)** vorhanden sind.
- Beim Öffnen der Palette und während sie geöffnet ist, werden veraltete Einträge automatisch aus der gespeicherten Auswahl entfernt.
- Die Seitenzahl und die Plus-/Minus-Schaltflächen werden nach einer Bereinigung neu aufgebaut. Keine leeren Seiten mehr durch längst verbrauchte Blöcke.
- Gewichtungen noch vorhandener Blöcke bleiben unverändert.
- Das automatische Hotbar-Nachfüllen aus Version 1.1.0 bleibt erhalten.

### Hinweis
Blöcke, die nur in Kisten, Shulkerboxen oder der Endertruhe liegen, zählen **nicht** als aktuell im Inventar. Sobald ein Blocktyp komplett aufgebraucht ist und du das Palettenmenü öffnest, wird seine Gewichtung entfernt; beim erneuten Beschaffen musst du ihn bei Bedarf wieder hinzufügen.

### Installation
Die alte `winterfell-random-palette-1.1.0.jar` aus dem Mod-Ordner entfernen und `winterfell-random-palette-1.1.1.jar` installieren. Fabric API und Java 25 erforderlich, kein Server-Plugin.

Die SHA-256-Prüfsumme liegt dem GitHub Release bei. GitHub Actions prüft die Kompilierung; Ingame-Test steht noch aus.
