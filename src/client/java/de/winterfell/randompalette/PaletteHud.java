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
        String title = "Winterfell-Multimenü | " + mode;
        String blocks = "Palette: " + types + " Typen | " + available + " Blöcke";
        String hotbarRefill = "Hotbar-Nachfüllen:";
        String autoTool = "Werkzeugwechsel:";

        int labelWidth = Math.max(mc.font.width(hotbarRefill), mc.font.width(autoTool));
        int width = Math.max(Math.max(mc.font.width(title), mc.font.width(blocks)),
                labelWidth + 8 + mc.font.width("AUS")) + 16;
        int x = Math.max(4, Math.min(graphics.guiWidth() - width - 4, graphics.guiWidth() / 2 + 103));
        int y = Math.max(3, graphics.guiHeight() - 93);
        int height = 54;

        graphics.fill(x, y, x + width, y + height, 0xBB101925);
        graphics.fill(x, y, x + 2, y + height, config.enabled ? 0xFF69C4EE : 0xFF718392);
        graphics.text(mc.font, title, x + 8, y + 4, config.enabled ? 0xFFB9EFFF : 0xFFCFD3DB);
        graphics.text(mc.font, blocks, x + 8, y + 16, 0xFFE9EDF2);
        drawStatus(graphics, mc, x + 8, y + 29, hotbarRefill, config.generalRefill);
        drawStatus(graphics, mc, x + 8, y + 41, autoTool, config.autoToolSwitch);
    }

    private static void drawStatus(GuiGraphicsExtractor graphics, Minecraft mc,
                                   int x, int y, String label, boolean enabled) {
        graphics.text(mc.font, label, x, y, 0xFFB9C7D5);
        graphics.text(mc.font, enabled ? "AN" : "AUS",
                x + mc.font.width(label) + 7, y, enabled ? 0xFF75E2A7 : 0xFFFF938C);
    }
}
