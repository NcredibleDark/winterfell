# Winterfell Random Palette

Clientseitige Fabric-Mod für **Minecraft 26.3**. Sie erleichtert das Texturieren von Mauern und anderen Bauwerken durch zufällige, gewichtete Blockplatzierung.

## Installation

1. In den **GitHub Releases** die Datei `winterfell-random-palette-VERSION.jar` herunterladen (nicht den automatisch erzeugten Source-Code-ZIP).
2. Datei in den `mods`-Ordner des Minecraft-26.3-Fabric-Profils legen.
3. **Fabric API** muss installiert sein. Java **25** wird benötigt.
4. Alte Versionen der Mod vor dem Start entfernen.

## Bedienung

- **Cosmetics-Garderobe**: Im K-Menü auf **Cosmetics / Garderobe** klicken. Es gibt jetzt drei separat kombinierbare Kategorien: Kopf, Rücken (3D-Accessoires) und Aura (Partikel). Die Auswahl ist **nur für den eigenen Spieler sichtbar** und wird lokal gespeichert. Kein Dienst, keine API-Schlüssel, kein Paper-Plugin. 3D-Accessoires sieht man in der Drittpersonenansicht (F5).
- **Homes & Warps (v1.7.0):** Im `K`-Menü auf **Homes** klicken. Eine eigene Client-GUI erlaubt das einmalige Speichern von Home- und Warp-Namen als klickbare Favoriten. Ein Klick sendet `/home <name>` oder `/warp <name>` an HuskHomes. **Serverliste anzeigen** führt `/homelist` beziehungsweise `/warplist` aus, **Home hier setzen** führt `/sethome <name>` aus. Zusätzlich ist `/phomelist` erreichbar. Favoriten sind pro Server und Minecraft-Konto getrennt gespeichert und können mit `✕` nur lokal entfernt werden; das entfernt keine echten Homes. **Eine automatische Liste aller Server-Homes ist ohne serverseitige Schnittstelle nicht verfügbar.** Keine zusätzliche Serverinstallation notwendig.
- `K`: **Winterfell-Multimenü** öffnen. Unten befinden sich die drei **Server-Schnellzugriffe**: **Werkbank** (`/wb`), **Enderchest** (`/ec`) und **Mülleimer** (`/trash`). Ein Klick schließt das Multimenü und sendet den jeweiligen Befehl an den Server. Das funktioniert ohne zusätzliche Server-Mod, sofern der Server diese Befehle anbietet und der Spieler sie ausführen darf. Der bisherige **Fertig**-Button wurde in die obere Menüzeile verschoben.
- Im GUI **nur Blöcke aus der untersten Reihe (Hotbar)** anklicken und ihre relativen Gewichte mit `+` / `-` einstellen. Die oberen drei Inventarreihen sind ausgegraut und nicht auswählbar.
- Beim Öffnen und während der Anzeige werden Einträge automatisch entfernt, wenn der Blocktyp in keinem der neun Hotbar-Slots mehr liegt. Im Hauptinventar liegende Stacks dienen nur als Nachfüllreserve und zählen nicht zur Gewichtung.
- Die ausgewählten Blöcke müssen für die tatsächliche Platzierung in der **Hotbar** liegen.
- `J`: Zufallsplatzierung ein- oder ausschalten.
- `U`: Das HUD-Overlay direkt ein-/ausblenden (Tastenbelegung in den Minecraft-Steuerungseinstellungen anpassbar). Im `K`-Menü gibt es dafür weiterhin den eigenen **HUD: AN/AUS**-Button.
- **Keramik-Ghost-Vorschau** (standardmäßig AUS): Im `K`-Menü aktivieren. Mit glasierter Keramik in der Haupthand erscheint ein halbtransparentes Blockmodell mit dem tatsächlichen Muster und der vom aktuellen Blickwinkel bestimmten Drehung am Zielort. Die Vorschau wird nur bei deaktivierter Random-Palette angezeigt, da deren Zufallsauswahl sonst nicht zuverlässig vorhergesagt werden kann. Kein Server-Plugin und kein automatisches Platzieren.
- **Smart Random:** Optional per `K`-Menü aktivieren. Vergangene Blocktypen erhalten eine geringere Wahrscheinlichkeit, sodass Texturen abwechslungsreicher wirken. Die ursprünglich festgelegten Gewichtungen bleiben die Basis.
- **Bau-HUD:** Die kompakte, abschaltbare Anzeige **Winterfell-Multimenü** zeigt Palettenmodus, verfügbare Blöcke sowie die farbigen AN/AUS-Zustände von **Hotbar-Nachfüllen** und **Werkzeugwechsel**. Das automatische Paletten-Nachfüllen ist bewusst nicht extra aufgeführt.
- Beim normalen Platzieren wird ausschließlich zwischen den ausgewählten, in der Hotbar vorhandenen Baublöcken gewechselt. Werkzeuge sind von der Auswahl ausgeschlossen.
- **Paletten-Nachfüllen ist immer aktiv, sobald die Random-Palette eingeschaltet ist.** Sobald ein ausgewählter Hotbar-Blockstack aufgebraucht ist, zieht die Mod automatisch denselben Blocktyp aus dem Inventar nach. Dafür gibt es keinen separaten Schalter mehr. Zu Beginn mindestens einen Stack der gewünschten Blocktypen in die Hotbar legen. Nachgefüllt wird nur während des Spielens, nicht bei geöffneten Inventarfenstern.
- **Hotbar-Refill** (standardmäßig AUS) bleibt als separat einstellbares Zusatzfeature erhalten. Es füllt beliebige verbrauchte, stapelbare Hotbar-Items mit exakt gleichen Items aus dem Inventar nach – unabhängig von der Random-Palette.
- **Auto Tool Switch** (standardmäßig AUS) wählt beim Blockabbau ein geeignetes Werkzeug aus der Hotbar, ohne das Inventar zu durchsuchen. Beide Extras haben eigene Schalter im K-Menü.
- Konfiguration: `config/winterfell-random-palette.json`.

## Downloads und Sicherheit

Der offizielle Download ist ein **GitHub Release Asset direkt als .jar über HTTPS**, keine temporäre Actions-ZIP. Jede Version hat außerdem eine `.jar.sha256` Prüfsumme.
Auf Windows lässt sich die Prüfsumme in PowerShell mit `Get-FileHash .\winterfell-random-palette-VERSION.jar -Algorithm SHA256` überprüfen.

Ein GitHub Release ist kein Ersatz für eine Prüfung der Dateien. Chrome kann weiterhin Sicherheitswarnungen anzeigen. Öffentliche Releases können ohne GitHub-Anmeldung heruntergeladen werden.

## Entwicklung

Ein Push auf `build/fabric-random-palette` kompiliert die Mod und veröffentlicht automatisch ein neues GitHub Release, **wenn `mod_version` in `gradle.properties` noch nicht veröffentlicht wurde**. Bestehende Releases werden absichtlich nicht überschrieben. Für Updates die Versionsnummer erhöhen.

Dieses Projekt läuft auf dem Branch `build/fabric-random-palette`, damit der Hauptzweig der Android-App unverändert bleibt.

## Winterfell-Cosmetics v1.6.0 – komplett lokal

**Keine externe Synchronisierung mehr.** Cosmetics gehören nur dem Spieler, der sie auswählt: Andere Java-/Bedrock-Spieler sehen sie nicht, selbst wenn sie die Mod installiert haben. Die bisherige `CosmeticsSync` ist entfernt; die gespeicherten Dienst-URLs/Tokens werden nach dem ersten Start aus der JSON-Konfiguration entfernt. Bereits ausgewählte Cosmetics werden nach Möglichkeit in die neuen Slots übernommen.

### Drei unabhängige Kategorien
- **Kopf – 3D:** Goldkrone, Eiskrone, Dämonenhörner, schwebender Heiligenschein, Zylinder, Katzenohren, Kopfhörer.
- **Rücken – 3D:** Dämonen-, Engels-, Eis- und Drachenflügel, roter oder königlicher Umhang, Rucksack, Jetpack, Rückenschwert.
- **Aura – Partikel:** Feuer, Seelenflammen, Schnee, Ender, Herzen, Sterne, Magie, Wolken, violette Magie, Funken, Totem, mystischer Nebel und Elektrosturm.

Die geometrischen Accessoires werden ausschließlich beim eigenen Charakter gerendert, ohne Netzwerkpakete oder Entities. Sie sind im **Drittpersonenmodus** sichtbar. Animationen und Körperhaltungen sind derzeit einfach ausgeführt; insbesondere beim Schleichen, Schwimmen und Elytra-Flug kann die Ausrichtung noch von der Vanilla-Spieleranimation abweichen. Eine Shader-/Iris-Prüfung im Spiel ist sinnvoll.

Im Cosmetics-Menü lässt sich das gesamte System ein-/ausschalten und die **Partikeldichte** auf Sparsam (Standard), Normal oder Intensiv ändern. Bei deinen früheren FPS-Problemen mit vielen Partikeln würde ich **Sparsam** bevorzugen.

### Unverändert
Random-Palette, Smart Random, Paletten-Nachfüllen, Hotbar-Refill, Auto Tool Switch, HUD, Keramik-Ghost und die Server-Buttons /wb /ec /trash. In v1.7.0 kommt zusätzlich die Homes-GUI mit lokalen Favoriten dazu.

**Installation:** Die vorige Mod-JAR vollständig entfernen und die v1.6.0-JAR in das Minecraft-26.3-Fabric-Profil kopieren. Fabric API und Java 25 sind erforderlich.
