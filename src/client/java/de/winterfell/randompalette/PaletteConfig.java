package de.winterfell.randompalette;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PaletteConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger("Winterfell Random Palette");
    private static final Path PATH = FabricLoader.getInstance().getConfigDir()
            .resolve("winterfell-random-palette.json");

    public boolean enabled = false;
    public boolean generalRefill = false;
    public boolean autoToolSwitch = false;
    public boolean smartRandom = false;
    public boolean showHud = true;
    public boolean ceramicGhostPreview = false;
    public boolean cosmeticsEnabled = true;
    public String headCosmetic = "none";
    public String backCosmetic = "none";
    public String auraCosmetic = "none";
    // 0=sparsam, 1=normal, 2=intensiv; older configs migrate silently.
    public int particleIntensity = 0;
    public LinkedHashMap<String, Integer> weights = new LinkedHashMap<>();

    public static PaletteConfig load() {
        if (!Files.isRegularFile(PATH)) return new PaletteConfig();
        try (Reader reader = Files.newBufferedReader(PATH)) {
            com.google.gson.JsonObject oldJson =
                    com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
            PaletteConfig config = GSON.fromJson(oldJson, PaletteConfig.class);
            if (config == null) return new PaletteConfig();
            if (config.weights == null) config.weights = new LinkedHashMap<>();
            // Upgrade cosmetic choices from v1.5.0 without retaining sync secrets.
            if (oldJson.has("cosmetic") && oldJson.get("cosmetic").isJsonPrimitive()) {
                String legacy = oldJson.get("cosmetic").getAsString();
                switch (legacy) {
                    case "halo" -> config.headCosmetic = "halo";
                    case "red_wings" -> config.backCosmetic = "demon_wings";
                    case "blue_wings" -> config.backCosmetic = "ice_wings";
                    case "flame_aura" -> config.auraCosmetic = "flame";
                    default -> {}
                }
            }
            if (!CosmeticEffects.validHead(config.headCosmetic)) config.headCosmetic = "none";
            if (!CosmeticEffects.validBack(config.backCosmetic)) config.backCosmetic = "none";
            if (!CosmeticEffects.validAura(config.auraCosmetic)) config.auraCosmetic = "none";
            config.particleIntensity = Math.max(0, Math.min(2, config.particleIntensity));
            // Re-save to remove the obsolete remote address and access token.
            config.save();
            config.weights.entrySet().removeIf(e -> e.getKey() == null || e.getKey().isBlank()
                    || e.getValue() == null || e.getValue() <= 0);
            for (Map.Entry<String, Integer> e : config.weights.entrySet()) {
                if (e.getValue() > 1000) e.setValue(1000);
            }
            return config;
        } catch (IOException | JsonParseException exception) {
            LOGGER.warn("Kann Paletten-Konfiguration nicht laden: {}", PATH, exception);
            return new PaletteConfig();
        }
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException exception) {
            LOGGER.error("Kann Paletten-Konfiguration nicht speichern: {}", PATH, exception);
        }
    }
}
