package de.winterfell.randompalette;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The wardrobe is separate from the existing building tools and keeps all
 * cosmetics purely local: no URLs, permissions, tokens, or networking.
 */
public final class CosmeticsScreen extends Screen {
    private static final int W = 430, H = 336;
    private static final String[] CATEGORIES = {"Kopf", "Rücken", "Aura"};
    private static final int PAGE_SIZE = 8;
    private int category = 0;
    private int page = 0;

    private int x() { return (width - W) / 2; }
    private int y() { return (height - H) / 2; }

    public CosmeticsScreen() {
        super(Component.literal("Winterfell-Cosmetics"));
    }

    @Override
    protected void init() {
        int x = x(), y = y();
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(
                    Component.literal((category == i ? "§b▶ " : "") + CATEGORIES[i]),
                    b -> {
                        category = idx;
                        page = 0;
                        rebuildWidgets();
                    }).bounds(x + 14 + i * 136, y + 44, 128, 21).build());
        }

        String[] options = options();
        int pages = Math.max(1, (options.length + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(pages - 1, page));
        for (int i = 0; i < PAGE_SIZE && i + page * PAGE_SIZE < options.length; i++) {
            String id = options[i + page * PAGE_SIZE];
            int bx = x + 14 + (i % 2) * 204;
            int by = y + 77 + (i / 2) * 31;
            boolean active = id.equals(selected());
            addRenderableWidget(Button.builder(
                    Component.literal((active ? "§a✓ " : "") + name(id)),
                    b -> {
                        switch (category) {
                            case 0 -> RandomPaletteClient.config.headCosmetic = id;
                            case 1 -> RandomPaletteClient.config.backCosmetic = id;
                            case 2 -> RandomPaletteClient.config.auraCosmetic = id;
                            default -> {}
                        }
                        RandomPaletteClient.config.save();
                        rebuildWidgets();
                    }).bounds(bx, by, 195, 25).build());
        }

        if (pages > 1) {
            addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
                page = Math.max(0, page - 1);
                rebuildWidgets();
            }).bounds(x + 150, y + 209, 32, 20).build());
            addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
                page = Math.min(pages - 1, page + 1);
                rebuildWidgets();
            }).bounds(x + 248, y + 209, 32, 20).build());
        }

        addRenderableWidget(Button.builder(
                Component.literal(RandomPaletteClient.config.cosmeticsEnabled
                        ? "§aCosmetics: AN" : "§cCosmetics: AUS"),
                b -> {
                    RandomPaletteClient.config.cosmeticsEnabled =
                            !RandomPaletteClient.config.cosmeticsEnabled;
                    RandomPaletteClient.config.save();
                    rebuildWidgets();
                }).bounds(x + 14, y + 285, 120, 22).build());
        addRenderableWidget(Button.builder(
                Component.literal("Partikel: " + density()),
                b -> {
                    RandomPaletteClient.config.particleIntensity =
                            (RandomPaletteClient.config.particleIntensity + 1) % 3;
                    RandomPaletteClient.config.save();
                    rebuildWidgets();
                }).bounds(x + 147, y + 285, 134, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Zurück"), b ->
                minecraft.gui.setScreen(new PaletteScreen()))
                .bounds(x + 294, y + 285, 122, 22).build());
    }

    private String[] options() {
        return switch (category) {
            case 0 -> CosmeticEffects.HEAD_OPTIONS;
            case 1 -> CosmeticEffects.BACK_OPTIONS;
            default -> CosmeticEffects.AURA_OPTIONS;
        };
    }

    private String selected() {
        return switch (category) {
            case 0 -> RandomPaletteClient.config.headCosmetic;
            case 1 -> RandomPaletteClient.config.backCosmetic;
            default -> RandomPaletteClient.config.auraCosmetic;
        };
    }

    private String name(String id) {
        return CosmeticEffects.displayName(id);
    }

    private String density() {
        return switch (RandomPaletteClient.config.particleIntensity) {
            case 0 -> "Sparsam";
            case 1 -> "Normal";
            default -> "Intensiv";
        };
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mx, int my, float partialTick) {}

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mx, int my, float partialTick) {
        graphics.fill(0, 0, width, height, 0x99000000);
        int x = x(), y = y();
        graphics.fill(x, y, x + W, y + H, 0xEC182636);
        graphics.outline(x, y, W, H, 0xFF60B5E7);
        graphics.text(font, "Winterfell-Cosmetics | Nur für dich sichtbar",
                x + 14, y + 12, 0xFFFFFFFF);
        graphics.text(font, "Drei kombinierbare Slots: Kopf + Rücken + Aura",
                x + 14, y + 29, 0xFFB7D9ED);

        int pages = Math.max(1, (options().length + PAGE_SIZE - 1) / PAGE_SIZE);
        if (pages > 1)
            graphics.text(font, "Seite " + (page + 1) + "/" + pages,
                    x + 193, y + 214, 0xFFEAF4FF);

        graphics.text(font, "Kopf: " + CosmeticEffects.displayName(RandomPaletteClient.config.headCosmetic)
                        + " | Rücken: " + CosmeticEffects.displayName(RandomPaletteClient.config.backCosmetic),
                x + 14, y + 242, 0xFFE9EDF2);
        graphics.text(font, "Aura: " + CosmeticEffects.displayName(RandomPaletteClient.config.auraCosmetic),
                x + 14, y + 256, 0xFFE9EDF2);
        graphics.text(font, "3D im Drittpersonenmodus (F5) sichtbar; keine Serververbindung.",
                x + 14, y + 316, 0xFFB7C9D9);
        super.extractRenderState(graphics, mx, my, partialTick);
    }

    @Override
    public void onClose() { minecraft.gui.setScreen(new PaletteScreen()); }

    @Override
    public boolean isPauseScreen() { return false; }
}
