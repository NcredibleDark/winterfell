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
    public LinkedHashMap<String, Integer> weights = new LinkedHashMap<>();

    public static PaletteConfig load() {
        if (!Files.isRegularFile(PATH)) return new PaletteConfig();
        try (Reader reader = Files.newBufferedReader(PATH)) {
            PaletteConfig config = GSON.fromJson(reader, PaletteConfig.class);
            if (config == null) return new PaletteConfig();
            if (config.weights == null) config.weights = new LinkedHashMap<>();
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
