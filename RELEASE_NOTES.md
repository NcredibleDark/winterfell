## Winterfell-Multimenü v1.7.0 – Homes & Warps im K-Menü

### Neu
- Neuer **Homes**-Button direkt neben Cosmetics im bestehenden Winterfell-Multimenü.
- Eigenes GUI mit getrennten Tabs **Eigene Homes** und **Öffentliche Warps**.
- Home-/Warp-Namen einmalig im Client als Favoriten hinterlegen; anschließend durch Anklicken sofort `/home <name>` oder `/warp <name>` ausführen.
- Favoriten werden pro Multiplayer-Server und Minecraft-Konto getrennt gespeichert und sind über mehrere Seiten navigierbar.
- **Serverliste anzeigen:** `/homelist` für eigene Homes beziehungsweise `/warplist` für Warps.
- **Home hier setzen:** direkt über `/sethome <name>`.
- **Öffentliche Homes:** Link zum vorhandenen HuskHomes-Befehl `/phomelist`.
- `✕` entfernt nur einen lokalen Favoriten, niemals das echte Server-Home.
- Serverseitige Berechtigungen, Cooldowns und Teleports bleiben vollständig unter HuskHomes-Kontrolle.

### Einschränkung
Ohne Serverplugin/API kann die Mod die komplette HuskHomes-Home-Liste nicht direkt lesen. Die klickbaren Favoriten werden deshalb einmalig von Hand hinzugefügt; die bereits existierende Serverliste ist über den Button weiterhin zugänglich. Es ist **kein neues Paper-Plugin** nötig.

### Unverändert
Random-Palette, Smart Random, automatisches Nachfüllen, Hotbar-Refill, Werkzeugwechsel, Ghost-Vorschau, HUD, Cosmetics (nur lokal) und /wb /ec /trash.

### Installation
Vorherige Version entfernen und `winterfell-random-palette-1.7.0.jar` mit Fabric 26.3 und Java 25 nutzen. Build wird automatisch getestet; die Bedienung muss anschließend im Spiel auf Winterfell geprüft werden.
