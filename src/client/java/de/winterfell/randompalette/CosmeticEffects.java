package de.winterfell.randompalette;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;

/**
 * Lightweight purely client-side cosmetics. Particle effects are intentional
 * for this first release: no custom server packets or shader-specific models.
 */
public final class CosmeticEffects {
    private CosmeticEffects() {}

    public static boolean isValid(String id) {
        return switch (id == null ? "" : id) {
            case "none", "halo", "red_wings", "blue_wings", "flame_aura" -> true;
            default -> false;
        };
    }

    public static String displayName(String id) {
        return switch (id == null ? "" : id) {
            case "halo" -> "Heiligenschein";
            case "red_wings" -> "Dämonenflügel (Partikel)";
            case "blue_wings" -> "Eisflügel (Partikel)";
            case "flame_aura" -> "Flammenaura";
            default -> "Keine Cosmetics";
        };
    }

    private static int ticks = 0;

    public static void tick(Minecraft mc) {
        if (mc.level == null || mc.player == null || RandomPaletteClient.config == null) {
            ticks = 0;
            return;
        }
        // Four updates per second; also avoid generating particles with GUIs open.
        if (++ticks % 5 != 0 || mc.gui.screen() != null) return;
        int drawn = 0;
        for (Player player : mc.level.players()) {
            if (drawn >= 20) break;
            if (player.isInvisible() || player.isSpectator()
                    || mc.player.distanceToSqr(player) > 32.0 * 32.0) continue;
            boolean self = player.getUUID().equals(mc.player.getUUID());
            if (!self && !RandomPaletteClient.config.showOtherCosmetics) continue;
            String cosmetic = self ? RandomPaletteClient.config.cosmetic
                    : CosmeticsSync.choiceFor(player.getUUID());
            if (!isValid(cosmetic) || "none".equals(cosmetic)) continue;
            drawn++;
            switch (cosmetic) {
                case "halo" -> halo(mc, player);
                case "red_wings" -> wings(mc, player, ParticleTypes.WITCH);
                case "blue_wings" -> wings(mc, player, ParticleTypes.SOUL_FIRE_FLAME);
                case "flame_aura" -> aura(mc, player);
                default -> {}
            }
        }
    }

    private static void halo(Minecraft mc, Player player) {
        double t = (ticks % 1000) * 0.19;
        double y = player.getY() + player.getBbHeight() + 0.19;
        for (int i = 0; i < 4; i++) {
            double angle = t + i * Math.PI / 2.0;
            spawn(mc, ParticleTypes.END_ROD,
                    player.getX() + 0.34 * Math.cos(angle), y,
                    player.getZ() + 0.34 * Math.sin(angle));
        }
    }

    private static void wings(Minecraft mc, Player player, ParticleOptions particle) {
        double rad = Math.toRadians(player.getYRot());
        double backX = Math.sin(rad), backZ = -Math.cos(rad);
        double rightX = Math.cos(rad), rightZ = Math.sin(rad);
        double flap = Math.sin(ticks * 0.13) * 0.08;
        double[] widths = {0.32, 0.55, 0.78, 0.98, 0.76, 0.58, 0.43};
        double[] heights = {1.42, 1.66, 1.77, 1.66, 1.38, 1.16, 0.98};
        double x = player.getX() + backX * 0.30;
        double z = player.getZ() + backZ * 0.30;
        for (int side : new int[]{-1, 1}) {
            for (int i = 0; i < widths.length; i++) {
                double w = side * (widths[i] + flap);
                spawn(mc, particle, x + rightX * w,
                        player.getY() + heights[i], z + rightZ * w);
            }
        }
    }

    private static void aura(Minecraft mc, Player player) {
        for (int i = 0; i < 5; i++) {
            double angle = ticks * 0.15 + i * Math.PI * 0.4;
            spawn(mc, ParticleTypes.FLAME, player.getX() + Math.cos(angle) * 0.48,
                    player.getY() + 0.09 + (i % 3) * 0.29,
                    player.getZ() + Math.sin(angle) * 0.48);
        }
    }

    private static void spawn(Minecraft mc, ParticleOptions particle, double x, double y, double z) {
        mc.level.addParticle(particle, x, y, z, 0.0, 0.003, 0.0);
    }
}
