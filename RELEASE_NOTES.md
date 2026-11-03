## Winterfell-Multimenü v1.4.2 – Homes & Warps ohne Cosmetics

Diese Version basiert auf **v1.4.1** – vor dem späteren Cosmetics-System.
Keine Änderungen an den veröffentlichten Dateien der v1.4.1.

### Neu
- Neues **Homes & Warps**-Menü im Winterfell-Multimenü (Taste K).
- Öffnet mit Tab **Eigene Homes**, lädt vorhandene Namen via HuskHomes-/home-TAB-Vervollständigung.
- Tab **Öffentliche Warps**: lädt verfügbare Warps über die /warp-TAB-Vervollständigung des Servers.
- Anklicken sendet den serverseitigen Befehl /home <name> bzw. /warp <name>.
- Aktualisieren lädt die Namen neu; Seitenwechsel bei längeren Listen.
- **Serverliste** öffnet /homelist oder /warplist als Fallback.
- Kein manuelles Speichern von Favoriten und kein zusätzliches Paper-Plugin.

### Grenzen
Die Mod fragt **Servervorschläge** ab, nicht die HuskHomes-Datenbank. Bei deaktivierter TAB-Vervollständigung, fehlenden Berechtigungen oder vom Server begrenzten Antworten kann die Liste fehlen oder unvollständig sein. Erst beim Klick auf ein Ziel wird ein Teleport ausgeführt.

### Unverändert
Alle Funktionen der v1.4.1: Smart Random, automatische Palettenfüllung, Hotbar-Refill, Auto Tool Switch, HUD, Keramik-Ghost und /wb /ec /trash. **Keine Cosmetics.**

### Installation
`winterfell-random-palette-1.4.1.jar` aus dem Mods-Ordner entfernen, `winterfell-random-palette-1.4.2.jar` einsetzen. Fabric 26.3 und Java 25 erforderlich. Kompilierung in GitHub Actions; Ingame-Verhalten mit eurem HuskHomes sollte anschließend geprüft werden.
