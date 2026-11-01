## Winterfell-Multimenü 1.5.0 – Winterfell-Cosmetics (erste Version)

**Neu: Gemeinsame Cosmetics für Spieler mit der Mod**
- Eigenes **Cosmetics / Garderobe**-Menü im bestehenden K-Menü
- Partikel-Cosmetics: Heiligenschein, rote Dämonenflügel, blaue Eisflügel und Flammenaura; lokal jederzeit wechselbar
- Anzeige von Cosmetics anderer Spieler mit der Mod, wenn beide Clients denselben externen HTTPS-Cosmetics-Dienst verwenden
- Einstellbar, ob andere Cosmetics dargestellt werden; Distanz- und Partikelbegrenzung
- Optionaler, eigenständiger Python-HTTP-/SQLite-Dienst unter `cosmetics-service/`
- Spielergebundene, durch Admin ausgegebene Token schützen Änderungen vor Identitätsfälschung; keine Minecraft-Kontodaten nötig
- **Keine Änderungen am Paper-Server** und keine zusätzlichen Serverpakete
- Vor der gemeinsamen Nutzung muss der Dienst extern über HTTPS bereitgestellt und pro Spieler eingerichtet werden. Ohne Dienst funktionieren lokale Cosmetics.
- Alle bisherigen Bau-, Keramik-Ghost-, HUD- und Kommando-Funktionen bleiben erhalten.

**Installation:** Alte 1.4.1-JAR entfernen, 1.5.0-JAR ins Fabric-26.3-Profil legen. Fabric API, Java 25. Bitte erst Ingame-Darstellung und Synchronisierung mit zwei Client-Instanzen prüfen.
