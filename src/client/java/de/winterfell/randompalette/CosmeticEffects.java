package de.winterfell.randompalette;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

/** Small, capped particle auras attached only to the local player. */
public final class CosmeticEffects {
    public static final String[] HEAD_OPTIONS = {
            "none", "crown", "horns", "halo", "top_hat", "cat_ears", "headphones", "ice_crown"
    };
    public static final String[] BACK_OPTIONS = {
            "none", "demon_wings", "angel_wings", "ice_wings", "dragon_wings",
            "red_cape", "royal_cape", "backpack", "jetpack", "back_sword"
    };
    public static final String[] AURA_OPTIONS = {
            "none", "flame", "soul", "snow", "ender", "heart", "stars",
            "magic", "cloud", "crimson", "spark", "totem", "mist", "storm"
    };

    private CosmeticEffects() {}

    public static boolean validHead(String id) { return contains(HEAD_OPTIONS, id); }
    public static boolean validBack(String id) { return contains(BACK_OPTIONS, id); }
    public static boolean validAura(String id) { return contains(AURA_OPTIONS, id); }

    private static boolean contains(String[] array, String id) {
        if (id == null) return false;
        for (String known : array) if (known.equals(id)) return true;
        return false;
    }

    public static String displayName(String id) {
        return switch (id == null ? "" : id) {
            case "crown" -> "Goldkrone";
            case "ice_crown" -> "Eiskrone";
            case "horns" -> "Dämonenhörner";
            case "halo" -> "3D-Heiligenschein";
            case "top_hat" -> "Zylinder";
            case "cat_ears" -> "Katzenohren";
            case "headphones" -> "Kopfhörer";
            case "demon_wings" -> "Dämonenflügel";
            case "angel_wings" -> "Engelsflügel";
            case "ice_wings" -> "Eisflügel";
            case "dragon_wings" -> "Drachenflügel";
            case "red_cape" -> "Roter Umhang";
            case "royal_cape" -> "Königsumhang";
            case "backpack" -> "Abenteurerrucksack";
            case "jetpack" -> "Jetpack";
            case "back_sword" -> "Rückenschwert";
            case "flame" -> "Feuer";
            case "soul" -> "Seelenflammen";
            case "snow" -> "Schneeflocken";
            case "ender" -> "Enderpartikel";
            case "heart" -> "Herzen";
            case "stars" -> "Sternenfunken";
            case "magic" -> "Magie";
            case "cloud" -> "Wolken";
            case "crimson" -> "Drachenatem";
            case "spark" -> "Funken";
            case "totem" -> "Totem-Aura";
            case "mist" -> "Mystischer Nebel";
            case "storm" -> "Elektrosturm";
            default -> "Keines";
        };
    }

    private static int ticks;

    public static void tick(Minecraft mc) {
        PaletteConfig cfg = RandomPaletteClient.config;
        if (cfg == null || !cfg.cosmeticsEnabled || mc.level == null
                || mc.player == null || mc.player.isSpectator() || mc.player.isInvisible()) {
            ticks = 0;
            return;
        }
        String id = cfg.auraCosmetic;
        if (!validAura(id) || "none".equals(id)) return;

        ticks++;
        int interval = switch (cfg.particleIntensity) {
            case 1 -> 5;
            case 2 -> 3;
            default -> 9;
        };
        if (ticks % interval != 0) return;

        ParticleOptions particle = switch (id) {
            case "flame" -> ParticleTypes.FLAME;
            case "soul" -> ParticleTypes.SOUL_FIRE_FLAME;
            case "snow" -> ParticleTypes.SNOWFLAKE;
            case "ender" -> ParticleTypes.PORTAL;
            case "heart" -> ParticleTypes.HEART;
            case "stars" -> ParticleTypes.END_ROD;
            case "magic" -> ParticleTypes.ENCHANT;
            case "cloud", "mist" -> ParticleTypes.CLOUD;
            case "crimson" -> ParticleTypes.DRAGON_BREATH;
            case "spark" -> ParticleTypes.CRIT;
            case "totem" -> ParticleTypes.TOTEM_OF_UNDYING;
            case "storm" -> ParticleTypes.ELECTRIC_SPARK;
            default -> null;
        };
        if (particle == null) return;
        // Maximum four particles per burst even at highest intensity.
        int amount = cfg.particleIntensity == 2 ? 4 : cfg.particleIntensity == 1 ? 3 : 2;
        for (int i = 0; i < amount; i++) {
            double angle = (ticks * 0.25 + i * (2.0 * Math.PI / amount));
            double radius = 0.39 + (i % 2) * 0.14;
            double y = mc.player.getY() + 0.18 + (i % 3) * 0.52;
            if ("heart".equals(id) || "stars".equals(id)) y += 0.35;
            if ("mist".equals(id) || "cloud".equals(id)) y = mc.player.getY() + 0.11;
            mc.level.addParticle(particle,
                    mc.player.getX() + Math.cos(angle) * radius,
                    y,
                    mc.player.getZ() + Math.sin(angle) * radius,
                    0.0, 0.005, 0.0);
        }
    }
}
