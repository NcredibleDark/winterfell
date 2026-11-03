package de.winterfell.randompalette;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Client-only personal HuskHomes shortcut GUI. It never tries to read private
 * HuskHomes server data or alter the server database. Users bookmark their
 * existing home/warp names locally; all teleports use normal server commands.
 */
public final class HomesScreen extends Screen {
    private static final int WIDTH = 416;
    private static final int HEIGHT = 342;
    private static final int PAGE_SIZE = 6;
    private static final int MAX_BOOKMARKS = 60;

    private boolean warps = false;
    private int page = 0;
    private EditBox nameInput;
    private String feedback = "";

    public HomesScreen() {
        super(Component.literal("Winterfell Homes"));
    }

    private int left() { return (width - WIDTH) / 2; }
    private int top() { return (height - HEIGHT) / 2; }

    /** Scope bookmarks by server and player; never mix other accounts. */
    private String scope() {
        if (minecraft == null || minecraft.player == null
                || minecraft.getCurrentServer() == null) return null;
        String host = minecraft.getCurrentServer().ip;
        return host + "|" + minecraft.player.getUUID();
    }

    private LinkedHashMap<String, ArrayList<String>> store() {
        return warps ? RandomPaletteClient.config.warpBookmarks
                     : RandomPaletteClient.config.homeBookmarks;
    }

    private List<String> saved() {
        String key = scope();
        if (key == null) return List.of();
        return store().computeIfAbsent(key, ignored -> new ArrayList<>());
    }

    /** HuskHomes uses names as a single command argument; no command injection. */
    private static boolean validName(String name) {
        return name != null && name.matches("[A-Za-z0-9_-]{1,40}");
    }

    private void sendServerCommand(String command) {
        if (minecraft == null || minecraft.player == null
                || minecraft.player.connection == null
                || minecraft.getCurrentServer() == null) {
            feedback = "Nur auf einem Multiplayer-Server verwendbar.";
            return;
        }
        minecraft.gui.setScreen(null);
        minecraft.player.connection.sendCommand(command);
    }

    private void addBookmark() {
        String name = nameInput.getValue().trim();
        if (scope() == null) {
            feedback = "Verbinde dich zuerst mit Winterfell.";
        } else if (!validName(name)) {
            feedback = "Erlaubt: Buchstaben, Zahlen, _ und - (max. 40).";
        } else {
            List<String> list = saved();
            if (list.contains(name)) {
                feedback = "Dieser Eintrag ist bereits gespeichert.";
            } else if (list.size() >= MAX_BOOKMARKS) {
                feedback = "Maximum von 60 Einträgen erreicht.";
            } else {
                list.add(name);
                RandomPaletteClient.config.save();
                feedback = "Gespeichert: " + name;
                page = Math.max(0, (list.size() - 1) / PAGE_SIZE);
            }
        }
        rebuildWidgets();
    }

    private void removeBookmark(String name) {
        if (saved().remove(name)) {
            RandomPaletteClient.config.save();
            feedback = "Nur lokalen Eintrag entfernt: " + name;
            int pages = Math.max(1, (saved().size() + PAGE_SIZE - 1) / PAGE_SIZE);
            page = Math.min(page, pages - 1);
            rebuildWidgets();
        }
    }

    @Override
    protected void init() {
        int x = left(), y = top();

        addRenderableWidget(Button.builder(Component.literal(warps ? "Eigene Homes" : "§b▶ Eigene Homes"), b -> {
            warps = false; page = 0; feedback = ""; rebuildWidgets();
        }).bounds(x + 14, y + 49, 189, 22).build());
        addRenderableWidget(Button.builder(Component.literal(warps ? "§b▶ Öffentliche Warps" : "Öffentliche Warps"), b -> {
            warps = true; page = 0; feedback = ""; rebuildWidgets();
        }).bounds(x + 213, y + 49, 189, 22).build());

        List<String> entries = saved();
        int pageCount = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(page, pageCount - 1));
        for (int i = 0; i < PAGE_SIZE && page * PAGE_SIZE + i < entries.size(); i++) {
            String name = entries.get(page * PAGE_SIZE + i);
            int yRow = y + 81 + i * 25;
            addRenderableWidget(Button.builder(
                    Component.literal(name.length() > 30 ? name.substring(0, 28) + "…" : name),
                    b -> {
                        if (validName(name))
                            sendServerCommand((warps ? "warp " : "home ") + name);
                    }).bounds(x + 14, yRow, 342, 21).build());
            addRenderableWidget(Button.builder(Component.literal("✕"),
                    b -> removeBookmark(name))
                    .bounds(x + 365, yRow, 37, 21).build());
        }

        nameInput = new EditBox(font, x + 14, y + 238, 240, 21,
                Component.literal("Home- oder Warpname"));
        nameInput.setMaxLength(40);
        nameInput.setHint(Component.literal("Namen hier eingeben …"));
        addRenderableWidget(nameInput);
        addRenderableWidget(Button.builder(Component.literal("Hinzufügen"),
                b -> addBookmark()).bounds(x + 264, y + 238, 138, 21).build());

        addRenderableWidget(Button.builder(Component.literal("Serverliste anzeigen"),
                b -> sendServerCommand(warps ? "warplist" : "homelist"))
                .bounds(x + 14, y + 272, 162, 22).build());

        // Create a named home through HuskHomes using the same server-side
        // permissions, warmups and limits as the player's usual chat command.
        if (!warps) {
            addRenderableWidget(Button.builder(Component.literal("Home hier setzen"),
                    b -> {
                        String name = nameInput.getValue().trim();
                        if (validName(name)) sendServerCommand("sethome " + name);
                        else { feedback = "Erst gültigen Namen eingeben."; rebuildWidgets(); }
                    }).bounds(x + 183, y + 272, 132, 22).build());
        } else {
            addRenderableWidget(Button.builder(Component.literal("Öffentl. Homes"),
                    b -> sendServerCommand("phomelist"))
                    .bounds(x + 183, y + 272, 132, 22).build());
        }

        addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
            page = Math.max(0, page - 1); rebuildWidgets();
        }).bounds(x + 323, y + 272, 37, 22).build());
        addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
            page = Math.min(pageCount - 1, page + 1); rebuildWidgets();
        }).bounds(x + 365, y + 272, 37, 22).build());

        addRenderableWidget(Button.builder(Component.literal("Zurück zum Multimenü"),
                b -> minecraft.gui.setScreen(new PaletteScreen()))
                .bounds(x + 111, y + 310, 194, 22).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mx, int my, float delta) {
        // Keep Minecraft 26.3 blur-free, matching our other menus.
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0x99000000);
        int x = left(), y = top();
        g.fill(x, y, x + WIDTH, y + HEIGHT, 0xEC182636);
        g.outline(x, y, WIDTH, HEIGHT, 0xFF60B5E7);
        g.text(font, "Winterfell | Homes & Warps", x + 14, y + 12, 0xFFFFFFFF);
        g.text(font, "Lokal gespeicherte Ziele: Anklicken = teleportieren", x + 14, y + 30, 0xFFB7D9ED);

        List<String> entries = saved();
        if (entries.isEmpty()) {
            g.text(font, "Noch keine Ziele gespeichert.", x + 20, y + 116, 0xFFFFC7A1);
            g.text(font, "Namen unten einmalig eintragen.", x + 20, y + 134, 0xFFB7C9D9);
        }
        int pages = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        g.text(font, (page + 1) + "/" + pages, x + 333, y + 256, 0xFFB7D9ED);
        if (!feedback.isEmpty())
            g.text(font, feedback.length() > 60 ? feedback.substring(0, 57) + "…" : feedback,
                    x + 14, y + 297, 0xFFE9C18E);
        else
            g.text(font, "✕ entfernt nur den Favoriten, niemals das Server-Home.",
                    x + 14, y + 297, 0xFFB7C9D9);
        super.extractRenderState(g, mx, my, delta);
    }

    @Override
    public void onClose() { minecraft.gui.setScreen(new PaletteScreen()); }

    @Override
    public boolean isPauseScreen() { return false; }
}
