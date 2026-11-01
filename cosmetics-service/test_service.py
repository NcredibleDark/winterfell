import hashlib
import json
import tempfile
import threading
import unittest
import urllib.error
import urllib.request
from http.server import ThreadingHTTPServer
from pathlib import Path
import server

PLAYER = "0e0f5ad7-58ce-4287-9e3f-cddac6fd570a"

class RegistryTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.orig = server.DB_PATH
        server.DB_PATH = Path(self.tmp.name) / "test.sqlite3"
        server.ensure_db()
        self.token = server.issue(PLAYER)
        self.server = ThreadingHTTPServer(("127.0.0.1", 0), server.Handler)
        self.thread = threading.Thread(target=self.server.serve_forever, daemon=True)
        self.thread.start()
        self.url = f"http://127.0.0.1:{self.server.server_port}"

    def tearDown(self):
        self.server.shutdown()
        self.server.server_close()
        self.thread.join(timeout=2)
        server.DB_PATH = self.orig
        self.tmp.cleanup()

    def test_health_and_authenticated_sync(self):
        with urllib.request.urlopen(self.url + "/health") as res:
            self.assertEqual(json.load(res)["status"], "ok")
        update = urllib.request.Request(
            self.url + "/api/v1/cosmetics/" + PLAYER,
            data=json.dumps({"cosmetic": "red_wings"}).encode(),
            method="PUT",
            headers={"Authorization": "Bearer " + self.token,
                     "Content-Type": "application/json"})
        with urllib.request.urlopen(update) as res:
            self.assertEqual(res.status, 204)
        with urllib.request.urlopen(self.url + "/api/v1/cosmetics?uuids=" + PLAYER) as res:
            self.assertEqual(json.load(res)["players"][PLAYER], "red_wings")

    def test_invalid_token_and_unknown_cosmetic(self):
        bad = urllib.request.Request(
            self.url + "/api/v1/cosmetics/" + PLAYER,
            data=b'{"cosmetic":"halo"}',
            method="PUT",
            headers={"Authorization": "Bearer " + "x" * 40})
        with self.assertRaises(urllib.error.HTTPError) as ctx:
            urllib.request.urlopen(bad)
        self.assertEqual(ctx.exception.code, 401)
        bad2 = urllib.request.Request(
            self.url + "/api/v1/cosmetics/" + PLAYER,
            data=b'{"cosmetic":"operator"}',
            method="PUT",
            headers={"Authorization": "Bearer " + self.token})
        with self.assertRaises(urllib.error.HTTPError) as ctx2:
            urllib.request.urlopen(bad2)
        self.assertEqual(ctx2.exception.code, 400)

    def test_token_rotation(self):
        previous = self.token
        fresh = server.issue(PLAYER)
        self.assertNotEqual(previous, fresh)
        self.assertEqual(len(hashlib.sha256(fresh.encode()).hexdigest()), 64)

if __name__ == "__main__":
    unittest.main()
