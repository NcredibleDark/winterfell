package de.winterfell.randompalette;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An optional HTTPS cosmetics registry independent of the Paper server.
 * No Minecraft account credentials are transmitted. Admin-issued bearer tokens
 * authorize ONLY updates to a single verified-by-admin player UUID.
 */
public final class CosmeticsSync {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4)).build();
    private static final ExecutorService IO = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "Winterfell Cosmetics Sync");
        thread.setDaemon(true);
        return thread;
    });
    private static final Map<UUID, String> REMOTE = new ConcurrentHashMap<>();
    private static volatile String status = "Nicht eingerichtet";
    private static String identity = "";
    private static String lastPublished = "";
    private static Object lastLevel;
    private static boolean busy = false;
    private static int ticks = 0;

    private CosmeticsSync() {}

    public static String status() { return status; }

    public static String choiceFor(UUID uuid) {
        return REMOTE.getOrDefault(uuid, "none");
    }

    public static void refreshSoon() { ticks = 99; }

    public static void tick(Minecraft mc) {
        PaletteConfig config = RandomPaletteClient.config;
        if (config == null || mc.player == null || mc.level == null) {
            if (lastLevel != null) reset();
            return;
        }
        // Registry pertains to the connected multiplayer world only.
        if (mc.hasSingleplayerServer()) {
            if (lastLevel != null) reset();
            status = "Nur auf Multiplayer-Servern";
            return;
        }
        String base = config.cosmeticsServiceUrl.trim();
        String token = config.cosmeticsAccessToken.trim();
        String current = base + "|" + token + "|" + mc.player.getUUID();
        if (lastLevel != mc.level || !current.equals(identity)) {
            reset();
            lastLevel = mc.level;
            identity = current;
        }
        if (base.isEmpty() || token.isEmpty()) {
            status = "URL/Token fehlen (Config)";
            REMOTE.clear();
            return;
        }
        URI uri;
        try {
            uri = URI.create(base);
            boolean local = "http".equalsIgnoreCase(uri.getScheme())
                    && ("localhost".equalsIgnoreCase(uri.getHost())
                    || "127.0.0.1".equals(uri.getHost()));
            if ((!local && !"https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getRawUserInfo() != null
                    || uri.getQuery() != null || uri.getFragment() != null
                    || !token.matches("[a-zA-Z0-9_\\-]{20,128}"))
                throw new IllegalArgumentException("URL oder Token ungueltig");
        } catch (Exception e) {
            status = "Ungültige HTTPS-URL / Token";
            REMOTE.clear();
            return;
        }
        if (++ticks < 100 || busy) return;
        ticks = 0;

        String selected = CosmeticEffects.isValid(config.cosmetic) ? config.cosmetic : "none";
        UUID self = mc.player.getUUID();
        List<UUID> visible = new ArrayList<>();
        for (Player player : mc.level.players()) {
            if (visible.size() >= 50) break;
            if (!self.equals(player.getUUID())) visible.add(player.getUUID());
        }
        final String host = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        final String currentIdentity = identity;
        busy = true;
        IO.execute(() -> {
            try {
                if (!selected.equals(lastPublished)) {
                    var put = HttpRequest.newBuilder(URI.create(host + "/api/v1/cosmetics/" + self))
                            .timeout(Duration.ofSeconds(6))
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + token)
                            .PUT(HttpRequest.BodyPublishers.ofString("{\"cosmetic\":\"" + selected + "\"}"))
                            .build();
                    var response = HTTP.send(put, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 401 || response.statusCode() == 403) {
                        status = "Token für UUID nicht freigeschaltet";
                        return;
                    }
                    if (response.statusCode() != 204) {
                        status = "Sync-Fehler HTTP " + response.statusCode();
                        return;
                    }
                    lastPublished = selected;
                }
                if (visible.isEmpty()) {
                    REMOTE.clear();
                    status = "Synchronisiert (keine Spieler in Sicht)";
                    return;
                }
                String ids = String.join(",", visible.stream().map(UUID::toString).toList());
                var get = HttpRequest.newBuilder(URI.create(host + "/api/v1/cosmetics?uuids=" + ids))
                        .timeout(Duration.ofSeconds(6)).GET().build();
                var response = HTTP.send(get, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    status = "Sync-Fehler HTTP " + response.statusCode();
                    return;
                }
                JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonObject players = root.getAsJsonObject("players");
                if (players == null) throw new IllegalStateException("missing players");
                Map<UUID, String> received = new ConcurrentHashMap<>();
                for (UUID id : visible) {
                    if (!players.has(id.toString())) continue;
                    String cosmetic = players.get(id.toString()).getAsString();
                    if (CosmeticEffects.isValid(cosmetic) && !"none".equals(cosmetic))
                        received.put(id, cosmetic);
                }
                // Do not apply results from a previous connection or account.
                if (currentIdentity.equals(identity)) {
                    REMOTE.clear();
                    REMOTE.putAll(received);
                    status = "Synchronisiert";
                }
            } catch (Exception e) {
                status = "Dienst nicht erreichbar";
            } finally {
                busy = false;
            }
        });
    }

    private static void reset() {
        REMOTE.clear();
        lastLevel = null;
        identity = "";
        lastPublished = "";
        ticks = 99;
    }
}
