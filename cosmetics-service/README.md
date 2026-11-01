# Winterfell-Cosmetics Synchronisierungsdienst

**Optionales externes HTTPS-Backend für Minecraft-Spieler mit Winterfell-Multimenü 1.5.0 oder neuer.**
Weder Paper noch Geyser werden verändert. Ohne diesen Dienst zeigt die Mod nur die eigene lokale Auswahl.

## Start
Python 3.10+ genügt; keine fremden Pakete notwendig.

```bash
python3 cosmetics-service/server.py serve
```

Der Dienst bindet standardmäßig nur `127.0.0.1:8765`. Richte einen eigenen HTTPS-Reverse-Proxy auf einer Domain ein, zum Beispiel `https://cosmetics.winterfell-mc.com`. Den internen Port **nicht öffentlich freigeben**. Für Linux empfiehlt sich ein Systemd-Service oder ein vergleichbarer Prozessmanager.

## Spieler freischalten

```bash
python3 cosmetics-service/server.py issue 0e0f5ad7-58ce-4287-9e3f-cddac6fd570a
```

Hierbei ist die UUID ein **Beispiel** und muss durch die echte Online-UUID des jeweiligen Spielers ersetzt werden. Ein zufälliger Token wird genau einmal angezeigt. Nur dem zugehörigen Spieler privat mitteilen. Das erneute Aufrufen von `issue` für dieselbe UUID ersetzt den vorherigen Token. Der SQLite-Datenbestand wird als `cosmetics-service/cosmetics.sqlite3` angelegt und muss sicher gesichert werden.

Jeder Spieler trägt in **seiner eigenen** `config/winterfell-random-palette.json` ein:

```json
{
  "cosmeticsServiceUrl": "https://cosmetics.example.com",
  "cosmeticsAccessToken": "PERSOENLICHER_TOKEN"
}
```

Die gezeigte JSON ist nur ein **Ausschnitt**: Bestehende JSON-Einträge einschließlich der Palette erhalten; Felder ergänzen, kein zweites Root-Objekt anlegen.

## Endpunkte
- `GET /health` – Dienstzustand
- `GET /api/v1/cosmetics?uuids=<uuid>,<uuid>` – aktuelle Cosmetics von höchstens 50 Spielern
- `PUT /api/v1/cosmetics/<uuid>` – eigene Auswahl ändern. Header `Authorization: Bearer <token>` und Body `{"cosmetic":"halo"}`. Erfolgreich: 204.

Werte: `none`, `halo`, `red_wings`, `blue_wings`, `flame_aura`.

## Sicherheit und Datenschutz
- Nur der Administrator vergibt die pro UUID gebundenen Token. Die UUIDs müssen mit dem tatsächlichen Minecraft-Online-Konto abgeglichen werden; das System verwendet **keinen Mojang-Login**.
- Token werden ausschließlich als SHA-256-Hash in SQLite gespeichert. Wer den echten Token kennt, darf die Auswahl dieser UUID ändern. Tokens dürfen nicht öffentlich verbreitet werden.
- Der Client sendet Tokens nur an seine konfigurierte HTTPS-Adresse, **nicht an den Minecraft-Server**. Backend mit TLS betreiben; optionaler Klartext-HTTP-Zugriff ist nur für localhost-Entwicklungszwecke freigegeben.
- Serveradresse und Token bleiben lokal in der JSON-Konfiguration, die nicht öffentlich geteilt werden sollte.
- Der Lese-Endpunkt ist öffentlich und gibt nur UUID + Cosmetic-ID zurück. Er trackt keine Spielerposition oder Online-Anwesenheit.
- Reverse-Proxy mit Anfrageraten-Begrenzung betreiben und Backups für die SQLite-Datei machen.

## Tests
```bash
python3 -m unittest discover -s cosmetics-service -p 'test_*.py'
```

### Beispiel für einen Nginx-Reverse-Proxy
```nginx
location / {
    proxy_pass http://127.0.0.1:8765;
    proxy_set_header Host $host;
    limit_req zone=cosmetics burst=20 nodelay;
}
```
Die `limit_req_zone`-Definition, TLS-Konfiguration sowie das Zertifikat sind abhängig vom Host; dieses Snippet ist **keine vollständige Konfiguration**.

Die Backend-Infrastruktur ist **nicht automatisch mit Veröffentlichung der Mod installiert**. Für echte Mod-zu-Mod-Synchronisierung muss sie separat betrieben und von den Spielern eingerichtet werden.
