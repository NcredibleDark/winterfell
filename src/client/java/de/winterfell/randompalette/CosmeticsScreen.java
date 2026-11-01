package de.winterfell.randompalette;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Separate wardrobe page, preserves the established palette screen layout. */
public final class CosmeticsScreen extends Screen {
    private static final int W = 336, H = 270;
    private int x() { return (width - W) / 2; }
    private int y() { return (height - H) / 2; }

    public CosmeticsScreen() {
        super(Component.literal("Winterfell-Cosmetics"));
    }

    @Override
    protected void init() {
        int x = x(), y = y();
        option(x + 14, y + 52, "none");
        option(x + 176, y + 52, "halo");
        option(x + 14, y + 82, "red_wings");
        option(x + 176, y + 82, "blue_wings");
        option(x + 14, y + 112, "flame_aura");
        addRenderableWidget(Button.builder(
                Component.literal(RandomPaletteClient.config.showOtherCosmetics
                        ? "§aAndere Cosmetics anzeigen: AN" : "§cAndere Cosmetics anzeigen: AUS"),
                b -> {
                    RandomPaletteClient.config.showOtherCosmetics =
                            !RandomPaletteClient.config.showOtherCosmetics;
                    RandomPaletteClient.config.save();
                    rebuildWidgets();
                }).bounds(x + 14, y + 152, 308, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Zurück zum Multimenü"),
                b -> minecraft.gui.setScreen(new PaletteScreen()))
                .bounds(x + 80, y + 237, 176, 20).build());
    }

    private void option(int x, int y, String id) {
        boolean selected = id.equals(RandomPaletteClient.config.cosmetic);
        addRenderableWidget(Button.builder(
                Component.literal((selected ? "§a✓ " : "") + CosmeticEffects.displayName(id)),
                b -> {
                    RandomPaletteClient.config.cosmetic = id;
                    RandomPaletteClient.config.save();
                    CosmeticsSync.refreshSoon();
                    rebuildWidgets();
                }).bounds(x, y, 146, 23).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float tick) {}

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float tick) {
        g.fill(0, 0, width, height, 0x99000000);
        int x = x(), y = y();
        g.fill(x, y, x + W, y + H, 0xEC182636);
        g.outline(x, y, W, H, 0xFF60B5E7);
        g.text(font, "Winterfell-Cosmetics", x + 14, y + 12, 0xFFFFFFFF);
        g.text(font, "Partikel-Effekte | Auswahl wird gespeichert", x + 14, y + 32, 0xFFB7D9ED);
        g.text(font, "Multiplayer: " + CosmeticsSync.status(), x + 14, y + 187, 0xFFB7D9ED);
        g.text(font, "Mitspieler sehen Cosmetics nur mit unserer Mod", x + 14, y + 201, 0xFFCBD9E9);
        g.text(font, "Sync-URL und Token: config/winterfell-random-palette.json", x + 14, y + 215, 0xFFB7C9D9);
        super.extractRenderState(g, mx, my, tick);
    }

    @Override
    public void onClose() { minecraft.gui.setScreen(new PaletteScreen()); }

    @Override
    public boolean isPauseScreen() { return false; }
}
