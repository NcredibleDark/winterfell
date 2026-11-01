#!/usr/bin/env python3
"""Tiny independent Winterfell cosmetics registry (Python stdlib only).

Run behind an HTTPS reverse proxy. Never expose port 8765 directly to the
internet; the server binds to 127.0.0.1 by default.
"""
import hashlib
import hmac
import json
import os
import secrets
import sqlite3
import sys
import uuid
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import parse_qs, urlsplit

DB_PATH = Path(os.environ.get("WINTERFELL_COSMETICS_DB", str(Path(__file__).with_name("cosmetics.sqlite3"))))
ALLOWED = frozenset({"none", "halo", "red_wings", "blue_wings", "flame_aura"})

def database():
    conn = sqlite3.connect(DB_PATH, timeout=5)
    conn.execute("PRAGMA busy_timeout=5000")
    return conn

def ensure_db():
    DB_PATH.parent.mkdir(parents=True, exist_ok=True)
    with database() as db:
        db.execute("""CREATE TABLE IF NOT EXISTS players (
            uuid TEXT PRIMARY KEY,
            token_hash TEXT NOT NULL,
            cosmetic TEXT NOT NULL DEFAULT 'none'
        )""")

def valid_uuid(value):
    try:
        return str(uuid.UUID(value)) == value
    except (ValueError, AttributeError, TypeError):
        return False

def issue(player_uuid):
    if not valid_uuid(player_uuid):
        raise ValueError("Use a canonical Minecraft UUID (with hyphens).")
    token = secrets.token_urlsafe(32)
    digest = hashlib.sha256(token.encode("utf-8")).hexdigest()
    with database() as db:
        db.execute("""INSERT INTO players (uuid, token_hash, cosmetic)
                      VALUES (?, ?, 'none')
                      ON CONFLICT(uuid) DO UPDATE SET token_hash = excluded.token_hash""",
                   (player_uuid, digest))
    return token

class Handler(BaseHTTPRequestHandler):
    server_version = "WinterfellCosmetics/1.0"

    def log_message(self, fmt, *args):
        # Avoid leaking private Authorization headers into access logs.
        print("%s - %s" % (self.address_string(), fmt % args), file=sys.stderr)

    def respond(self, status, body=None):
        payload = json.dumps(body, separators=(",", ":")).encode("utf-8") if body is not None else b""
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Length", str(len(payload)))
        self.end_headers()
        if payload:
            self.wfile.write(payload)

    def do_GET(self):
        request = urlsplit(self.path)
        if request.path == "/health":
            return self.respond(200, {"status": "ok"})
        if request.path != "/api/v1/cosmetics":
            return self.respond(404, {"error": "not_found"})

        values = parse_qs(request.query, keep_blank_values=False).get("uuids", [])
        if len(values) != 1 or len(values[0]) > 1900:
            return self.respond(400, {"error": "invalid_uuids"})
        requested = list(dict.fromkeys(values[0].split(",")))
        if not requested or len(requested) > 50 or not all(map(valid_uuid, requested)):
            return self.respond(400, {"error": "invalid_uuids"})

        placeholders = ",".join("?" for _ in requested)
        with database() as db:
            rows = db.execute(
                "SELECT uuid, cosmetic FROM players WHERE uuid IN (" + placeholders + ")",
                requested).fetchall()
        return self.respond(200, {"players": {player: cosmetic for player, cosmetic in rows}})

    def do_PUT(self):
        request = urlsplit(self.path)
        prefix = "/api/v1/cosmetics/"
        if request.query or not request.path.startswith(prefix):
            return self.respond(404, {"error": "not_found"})
        player = request.path[len(prefix):]
        if not valid_uuid(player):
            return self.respond(400, {"error": "invalid_uuid"})

        authorization = self.headers.get("Authorization", "")
        if not authorization.startswith("Bearer "):
            return self.respond(401, {"error": "not_authorized"})
        token = authorization[7:]
        if len(token) < 20 or len(token) > 128:
            return self.respond(401, {"error": "not_authorized"})
        length_string = self.headers.get("Content-Length", "")
        if not length_string.isdecimal() or int(length_string) > 256:
            return self.respond(413, {"error": "payload_too_large"})
        try:
            payload = json.loads(self.rfile.read(int(length_string)))
            selection = payload.get("cosmetic") if isinstance(payload, dict) else None
        except (ValueError, UnicodeDecodeError):
            return self.respond(400, {"error": "invalid_json"})
        if selection not in ALLOWED:
            return self.respond(400, {"error": "unknown_cosmetic"})

        with database() as db:
            row = db.execute("SELECT token_hash FROM players WHERE uuid=?", (player,)).fetchone()
            actual_hash = hashlib.sha256(token.encode("utf-8")).hexdigest()
            if not row or not hmac.compare_digest(row[0], actual_hash):
                return self.respond(401, {"error": "not_authorized"})
            db.execute("UPDATE players SET cosmetic=? WHERE uuid=?", (selection, player))
        return self.respond(204)

def main():
    ensure_db()
    if len(sys.argv) >= 3 and sys.argv[1] == "issue":
        token = issue(sys.argv[2])
        print("UUID:", sys.argv[2])
        print("SECRET TOKEN (show ONLY to this player; not recoverable):", token)
        return
    if len(sys.argv) == 2 and sys.argv[1] == "serve":
        host = os.environ.get("WINTERFELL_COSMETICS_HOST", "127.0.0.1")
        port = int(os.environ.get("WINTERFELL_COSMETICS_PORT", "8765"))
        with ThreadingHTTPServer((host, port), Handler) as server:
            print(f"Winterfell-Cosmetics listening at http://{host}:{port} (HTTPS proxy required)")
            server.serve_forever()
        return
    raise SystemExit("Usage: python3 server.py serve | python3 server.py issue <player-uuid>")

if __name__ == "__main__":
    main()
