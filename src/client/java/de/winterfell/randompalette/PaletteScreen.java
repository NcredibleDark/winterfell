package de.winterfell.randompalette;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PaletteScreen extends Screen {
    private static final int PANEL_W = 332;
    private static final int PANEL_H = 325;
    private static final int GRID_X = 15;
    private static final int GRID_Y = 57;
    private static final int CELL = 20;
    private static final int LIST_Y = 161;
    private static final int ROW_H = 19;
    private static final int PER_PAGE = 6;
    private int page = 0;

    public PaletteScreen() {
        super(Component.literal("Winterfell Random Palette"));
    }

    private int left() { return (width - PANEL_W) / 2; }
    private int top() { return (height - PANEL_H) / 2; }

    /**
     * The palette contains only block types that are still in the player's
     * 36-slot inventory (hotbar included), not historical selections.
     * Never prune without a loaded player/inventory.
     */
    private boolean removeMissingPaletteEntries() {
        if (minecraft == null || minecraft.player == null || RandomPaletteClient.config == null) return false;

        java.util.Set<String> available = new java.util.HashSet<>();
        for (int i = 0; i < 36; i++) {
            ItemStack stack = minecraft.player.getInventory().getItem(i);
            if (RandomPaletteClient.isSelectable(stack)) {
                available.add(RandomPaletteClient.itemId(stack));
            }
        }

        boolean changed = RandomPaletteClient.config.weights.keySet()
                .removeIf(id -> !available.contains(id));
        if (changed) RandomPaletteClient.config.save();
        return changed;
    }

    @Override
    public void tick() {
        super.tick();
        // Updates buttons and pagination too if the server changes inventory
        // while this GUI remains open.
        if (removeMissingPaletteEntries()) rebuildWidgets();
    }

    @Override
    protected void init() {
        removeMissingPaletteEntries();
        int x = left();
        int y = top();

        addRenderableWidget(Button.builder(
                Component.literal(RandomPaletteClient.config.enabled ? "§aAktiv: AN" : "§cAktiv: AUS"),
                b -> {
                    RandomPaletteClient.config.enabled = !RandomPaletteClient.config.enabled;
                    RandomPaletteClient.config.save();
                    rebuildWidgets();
                }).bounds(x + 204, y + 8, 110, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal(RandomPaletteClient.config.autoRefill ? "§aAuto-Nachfüllen: AN" : "§cAuto-Nachfüllen: AUS"),
                b -> {
                    RandomPaletteClient.config.autoRefill = !RandomPaletteClient.config.autoRefill;
                    RandomPaletteClient.config.save();
                    rebuildWidgets();
                }).bounds(x + 199, y + 57, 115, 20).build());

        List<String> ids = new ArrayList<>(RandomPaletteClient.config.weights.keySet());
        int pages = Math.max(1, (ids.size() + PER_PAGE - 1) / PER_PAGE);
        page = Math.max(0, Math.min(page, pages - 1));

        for (int i = 0; i < PER_PAGE && page * PER_PAGE + i < ids.size(); i++) {
            String id = ids.get(page * PER_PAGE + i);
            int row = i;
            addRenderableWidget(Button.builder(Component.literal("-"), b -> changeWeight(id, -5))
                    .bounds(x + 232, y + LIST_Y + row * ROW_H, 18, 17).build());
            addRenderableWidget(Button.builder(Component.literal("+"), b -> changeWeight(id, 5))
                    .bounds(x + 296, y + LIST_Y + row * ROW_H, 18, 17).build());
        }

        if (pages > 1) {
            addRenderableWidget(Button.builder(Component.literal("<"), b -> {
                page = Math.max(0, page - 1);
                rebuildWidgets();
            }).bounds(x + 178, y + 282, 20, 18).build());
            addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                page = Math.min(pages - 1, page + 1);
                rebuildWidgets();
            }).bounds(x + 289, y + 282, 20, 18).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> onClose())
                .bounds(x + 118, y + 302, 96, 19).build());
    }

    private void changeWeight(String id, int diff) {
        Integer old = RandomPaletteClient.config.weights.get(id);
        if (old == null) return;
        RandomPaletteClient.config.weights.put(id, Math.max(1, Math.min(1000, old + diff)));
        RandomPaletteClient.config.save();
    }

    // We draw a custom opaque screen rather than asking Minecraft to blur the same
    // GUI stratum a second time. In 26.3 a second blur throws an exception.
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // No built-in blur. The panel below provides its own background.
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x99000000);
        int x = left();
        int y = top();
        graphics.fill(x, y, x + PANEL_W, y + PANEL_H, 0xE9182636);
        graphics.outline(x, y, PANEL_W, PANEL_H, 0xFF60B5E7);
        graphics.text(font, "Winterfell Random Palette", x + 13, y + 13, 0xFFFFFFFF);
        graphics.text(font, "Baublöcke anklicken (Werkzeuge sind gesperrt)", x + 13, y + 36, 0xFFB7D9ED);

        if (minecraft.player != null) {
            for (int displaySlot = 0; displaySlot < 36; displaySlot++) {
                int slot = inventorySlot(displaySlot);
                ItemStack stack = minecraft.player.getInventory().getItem(slot);
                int bx = x + GRID_X + (displaySlot % 9) * CELL;
                int by = y + GRID_Y + (displaySlot / 9) * CELL;
                boolean selectable = RandomPaletteClient.isSelectable(stack);
                boolean selected = selectable && RandomPaletteClient.config.weights.containsKey(
                        RandomPaletteClient.itemId(stack));
                int color = selected ? 0xFF20764A : slot < 9 ? 0xFF33516B : 0xFF263B50;
                graphics.fill(bx, by, bx + 19, by + 19, color);
                graphics.outline(bx, by, 19, 19, selected ? 0xFF74FFAB : 0xFF607A91);
                if (!stack.isEmpty()) {
                    graphics.item(stack, bx + 1, by + 1);
                    if (!selectable) graphics.fill(bx + 1, by + 1, bx + 18, by + 18, 0x75000000);
                    if (mouseX >= bx && mouseX < bx + 19 && mouseY >= by && mouseY < by + 19) {
                        graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
                    }
                }
            }
        }

        graphics.text(font, "Inventar-Reserve", x + 200, y + 85, 0xFFE7EDF5);
        graphics.text(font, "fuellt leere Slots", x + 200, y + 101, 0xFFB4D8F2);
        graphics.text(font, "mit gleichem Block.", x + 200, y + 114, 0xFFB4D8F2);
        graphics.text(font, "Gewichtung (relativ):", x + 16, y + 145, 0xFFFFFFFF);

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(RandomPaletteClient.config.weights.entrySet());
        long total = entries.stream().filter(e -> isInHotbar(e.getKey()))
                .mapToLong(Map.Entry::getValue).sum();
        if (entries.isEmpty()) {
            graphics.text(font, "Noch keine Blöcke ausgewählt.", x + 17, y + LIST_Y + 9, 0xFFFFC7A1);
        }

        int from = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE && from + i < entries.size(); i++) {
            Map.Entry<String, Integer> entry = entries.get(from + i);
            int ry = y + LIST_Y + i * ROW_H;
            ItemStack stack = findInventoryItem(entry.getKey());
            boolean inHotbar = isInHotbar(entry.getKey());
            graphics.fill(x + 14, ry, x + 316, ry + 18, inHotbar ? 0x77305263 : 0x775E4941);
            if (!stack.isEmpty()) graphics.item(stack, x + 17, ry + 1);
            String label = stack.isEmpty() ? entry.getKey() : stack.getHoverName().getString();
            if (label.length() > 23) label = label.substring(0, 21) + "..";
            graphics.text(font, label, x + 38, ry + 5, inHotbar ? 0xFFFFFFFF : 0xFFFFB5A1);
            int pct = !inHotbar || total == 0 ? 0 : (int) Math.round(entry.getValue() * 100.0 / total);
            graphics.text(font, pct + "%", x + 254, ry + 5, 0xFFE9F5FF);
        }

        int pages = Math.max(1, (entries.size() + PER_PAGE - 1) / PER_PAGE);
        if (pages > 1) {
            graphics.text(font, "Seite " + (page + 1) + "/" + pages, x + 210, y + 287, 0xFFEAF4FF);
        }
        graphics.text(font, "Rot = nicht in Hotbar | +/- ändert Gewicht", x + 15, y + 285, 0xFFB7C9D9);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private ItemStack findInventoryItem(String id) {
        if (minecraft.player == null) return ItemStack.EMPTY;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = minecraft.player.getInventory().getItem(i);
            if (RandomPaletteClient.isSelectable(stack) && RandomPaletteClient.itemId(stack).equals(id)) return stack;
        }
        return ItemStack.EMPTY;
    }

    private boolean isInHotbar(String id) {
        if (minecraft.player == null) return false;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = minecraft.player.getInventory().getItem(i);
            if (RandomPaletteClient.isSelectable(stack) && RandomPaletteClient.itemId(stack).equals(id)) return true;
        }
        return false;
    }

    private int inventorySlot(int displayed) {
        return displayed < 27 ? displayed + 9 : displayed - 27;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT || minecraft.player == null) return false;
        int x = left() + GRID_X;
        int y = top() + GRID_Y;
        double mx = event.x();
        double my = event.y();
        if (mx < x || mx >= x + CELL * 9 || my < y || my >= y + CELL * 4) return false;
        int col = (int) (mx - x) / CELL;
        int row = (int) (my - y) / CELL;
        ItemStack stack = minecraft.player.getInventory().getItem(inventorySlot(row * 9 + col));
        if (!RandomPaletteClient.isSelectable(stack)) return true;
        String id = RandomPaletteClient.itemId(stack);
        if (RandomPaletteClient.config.weights.containsKey(id)) {
            RandomPaletteClient.config.weights.remove(id);
        } else {
            RandomPaletteClient.config.weights.put(id, 10);
        }
        RandomPaletteClient.config.save();
        page = Math.max(0, (RandomPaletteClient.config.weights.size() - 1) / PER_PAGE);
        rebuildWidgets();
        return true;
    }

    @Override
    public void onClose() {
        RandomPaletteClient.config.save();
        minecraft.gui.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
