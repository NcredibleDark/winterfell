package de.winterfell.randompalette;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Compact, blur-free overlay integrated with Fabric's HUD layers. */
public final class PaletteHud {
    private PaletteHud() {}

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(RandomPaletteClient.MOD_ID, "status"),
                PaletteHud::extract);
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker tick) {
        Minecraft mc = Minecraft.getInstance();
        PaletteConfig config = RandomPaletteClient.config;
        if (config == null || !config.showHud || mc.player == null || mc.gui.screen() != null)
            return;

        int types = 0;
        int available = 0;
        if (config.enabled) {
            for (String id : config.weights.keySet()) {
                boolean inHotbar = false;
                int count = 0;
                for (int slot = 0; slot < 36; slot++) {
                    ItemStack stack = mc.player.getInventory().getItem(slot);
                    if (RandomPaletteClient.isSelectable(stack)
                            && RandomPaletteClient.itemId(stack).equals(id)) {
                        count += stack.getCount();
                        if (slot < 9) inHotbar = true;
                    }
                }
                if (inHotbar) {
                    types++;
                    available += count;
                }
            }
        }

        String mode = !config.enabled ? "AUS" : config.smartRandom ? "SMART" : "ZUFALL";
        String line1 = "WINTERFELL  |  " + mode;
        String line2 = "Palette " + types + " Typen | " + available + " Bloecke";
        String line3 = "P:" + on(config.enabled && config.autoRefill)
                + "  H:" + on(config.generalRefill)
                + "  Tool:" + on(config.autoToolSwitch);

        int width = Math.max(mc.font.width(line1), Math.max(mc.font.width(line2), mc.font.width(line3))) + 14;
        int x = Math.max(4, Math.min(graphics.guiWidth() - width - 4, graphics.guiWidth() / 2 + 103));
        int y = graphics.guiHeight() - 79;

        graphics.fill(x, y, x + width, y + 42, 0xBB101925);
        graphics.fill(x, y, x + 2, y + 42, config.enabled ? 0xFF69C4EE : 0xFF718392);
        graphics.text(mc.font, line1, x + 7, y + 4, config.enabled ? 0xFFB9EFFF : 0xFFCFD3DB);
        graphics.text(mc.font, line2, x + 7, y + 16, 0xFFE9EDF2);
        graphics.text(mc.font, line3, x + 7, y + 28, 0xFFA9B9C8);
    }

    private static String on(boolean enabled) {
        return enabled ? "AN" : "AUS";
    }
}
