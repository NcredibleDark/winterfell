## Winterfell Random Palette v1.2.0

Minecraft Fabric 26.3 / Java 25. Enthält die Funktionen der Version 1.1.1.

### Neue unabhängige Extras
- **Allgemeines Hotbar-Refill** (neuer Button, standardmäßig AUS): Nach dem Aufbrauchen eines Stapels sucht die Mod im Hauptinventar nach exakt demselben stapelbaren Item (inklusive Datenkomponenten) und füllt den leeren Hotbar-Slot. Funktioniert auch bei deaktivierter Random-Palette, z. B. für Essen, Fackeln oder Baublöcke. Werkzeuge werden nicht nachgefüllt und nie ungefragt aus der Hotbar verdrängt.
- **Auto Tool Switch** (neuer Button, standardmäßig AUS): Beim Abbau von Blöcken wird ein geeignetes schnelleres Werkzeug *ausschließlich aus der Hotbar* ausgewählt. Achtet auf die Fähigkeit, korrekte Drops zu erhalten; fast kaputte Werkzeuge werden übersprungen.
- Bestehende Optionen **Random-Palette** und **Paletten-Auto-Nachfüllen** bleiben separat bedienbar.
- Die Bereinigung nicht mehr vorhandener Einträge aus der Gewichtungsliste bleibt enthalten.

### Bedienung
`K` öffnet das GUI mit allen vier Schaltern, `J` schaltet die Random-Palette um.

### Hinweise
- Für das generelle Nachfüllen muss der betreffende Itemtyp zuvor im Hotbar-Slot gewesen sein; das Nachfüllen startet nach vollständigem Verbrauch.
- Bewegt man Items im geöffneten Inventar manuell, werden leere Hotbar-Slots nicht automatisch nachgefüllt.
- Der Mod ist rein clientseitig und verschiebt Items ausschließlich über normale, vom Server geprüfte Inventar-Klicks.
- Automatische Werkzeugwahl erfolgt nur beim Abbauen, nicht im Kampf.

### Installation
Alte `winterfell-random-palette-1.1.1.jar` entfernen und `winterfell-random-palette-1.2.0.jar` im Fabric-26.3-`mods`-Ordner installieren. Fabric API und Java 25 erforderlich.

Automatisierter Java-/Gradle-Build wird ausgeführt; ein tatsächlicher Spieltest auf dem Winterfell-Server ist weiterhin sinnvoll.
