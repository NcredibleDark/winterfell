package de.winterfell.randompalette;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Client-only HuskHomes browser. Names are discovered through the command
 * completion protocol that the server already provides when a player presses
 * TAB after '/home ' or '/warp '. No manual bookmarks, new plugin or database.
 */
public final class HomesScreen extends Screen {
    private static final int PANEL_W = 360;
    private static final int PANEL_H = 314;
    private static final int PAGE_SIZE = 6;
    private static final int MAX_RESULTS = 256;

    private boolean warps;
    private int page;
    private int requestId;
    private boolean started;
    private boolean loading;
    private String status = "Noch nicht abgefragt";
    private List<String> homes = List.of();
    private List<String> warpNames = List.of();

    public HomesScreen() {
        super(Component.literal("Winterfell – Homes & Warps"));
    }

    private int left() { return (width - PANEL_W) / 2; }
    private int top() { return (height - PANEL_H) / 2; }
    private List<String> current() { return warps ? warpNames : homes; }

    @Override
    protected void init() {
        int x = left(), y = top();

        // Initial request is performed exactly once, not on every GUI rebuild.
        if (!started) {
            started = true;
            refresh();
        }

        addRenderableWidget(Button.builder(
                Component.literal(warps ? "Eigene Homes" : "§b▶ Eigene Homes"),
                b -> switchTab(false)).bounds(x + 12, y + 37, 163, 21).build());
        addRenderableWidget(Button.builder(
                Component.literal(warps ? "§b▶ Öffentliche Warps" : "Öffentliche Warps"),
                b -> switchTab(true)).bounds(x + 185, y + 37, 163, 21).build());

        List<String> entries = current();
        int totalPages = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(page, totalPages - 1));
        for (int row = 0; row < PAGE_SIZE && page * PAGE_SIZE + row < entries.size(); row++) {
            String name = entries.get(page * PAGE_SIZE + row);
            addRenderableWidget(Button.builder(Component.literal(name), b -> teleport(name))
                    .bounds(x + 12, y + 84 + row * 27, 336, 23).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Aktualisieren"),
                b -> refresh()).bounds(x + 12, y + 254, 111, 21).build());
        addRenderableWidget(Button.builder(Component.literal("Serverliste"),
                b -> command(warps ? "warplist" : "homelist"))
                .bounds(x + 133, y + 254, 109, 21).build());
        addRenderableWidget(Button.builder(Component.literal("Zurück"),
                b -> minecraft.gui.setScreen(new PaletteScreen()))
                .bounds(x + 252, y + 254, 96, 21).build());

        if (totalPages > 1) {
            addRenderableWidget(Button.builder(Component.literal("<"), b -> {
                page = Math.max(0, page - 1);
                rebuildWidgets();
            }).bounds(x + 12, y + 282, 26, 18).build());
            addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                page = Math.min(totalPages - 1, page + 1);
                rebuildWidgets();
            }).bounds(x + 322, y + 282, 26, 18).build());
        }
    }

    private void switchTab(boolean showWarps) {
        if (warps == showWarps) return;
        warps = showWarps;
        page = 0;
        refresh();
    }

    /**
     * Brigadier asks the server for the same completions vanilla chat does.
     * This does NOT execute /home without arguments and cannot teleport.
     */
    private void refresh() {
        requestId++;
        int id = requestId;
        loading = true;
        status = warps ? "Frage /warp-Vorschläge ab …" : "Frage /home-Vorschläge ab …";
        List<String> previous = current();
        if (warps) warpNames = List.of();
        else homes = List.of();
        page = 0;
        if (minecraft == null || minecraft.player == null
                || minecraft.player.connection == null) {
            loading = false;
            status = "Nur auf einem Server verfügbar.";
            rebuildWidgets();
            return;
        }

        final Minecraft client = minecraft;
        final boolean requestedWarps = warps;
        final String prefix = requestedWarps ? "warp " : "home ";
        try {
            var connection = client.player.connection;
            var dispatcher = connection.getCommands();
            var source = connection.getSuggestionsProvider();
            // If the server does not register this command, show a real error.
            if (dispatcher.getRoot().getChild(requestedWarps ? "warp" : "home") == null) {
                loading = false;
                status = "Befehl nicht im Server-Commandtree. Serverliste testen.";
                rebuildWidgets();
                return;
            }
            CompletableFuture<Suggestions> future = dispatcher.getCompletionSuggestions(
                    dispatcher.parse(prefix, source));

            future.whenComplete((result, error) -> client.execute(() -> {
                // Discard stale replies when the player changed tabs/closed the GUI.
                if (id != requestId || client.gui.screen() != this) return;
                loading = false;
                if (error != null || result == null) {
                    status = "Abfrage fehlgeschlagen – Serverliste verwenden.";
                } else {
                    LinkedHashSet<String> names = new LinkedHashSet<>();
                    for (Suggestion suggestion : result.getList()) {
                        // Brigadier suggestions can replace only part of the command.
                        String complete = suggestion.apply(prefix);
                        if (!complete.startsWith(prefix)) continue;
                        String name = complete.substring(prefix.length()).trim();
                        if (isSafeName(name)) names.add(name);
                        if (names.size() >= MAX_RESULTS) break;
                    }
                    List<String> found = new ArrayList<>(names);
                    found.sort(String.CASE_INSENSITIVE_ORDER);
                    if (requestedWarps) warpNames = List.copyOf(found);
                    else homes = List.copyOf(found);
                    status = found.isEmpty()
                            ? "Keine Vorschläge erhalten (oder keine Berechtigung)."
                            : found.size() + " " + (requestedWarps ? "Warps" : "Homes")
                                  + " vom Server gefunden.";
                }
                rebuildWidgets();
            }));
        } catch (RuntimeException ex) {
            loading = false;
            status = "TAB-Abfrage nicht verfügbar – Serverliste nutzen.";
            rebuildWidgets();
        }
    }

    /** No whitespace, quotes or control codes can enter executable commands. */
    private static boolean isSafeName(String name) {
        return name != null && name.length() <= 64
                && name.matches("[\\p{L}\\p{N}_.-]+");
    }

    private void teleport(String name) {
        if (isSafeName(name))
            command((warps ? "warp " : "home ") + name);
    }

    private void command(String value) {
        if (minecraft == null || minecraft.player == null
                || minecraft.player.connection == null) return;
        minecraft.gui.setScreen(null);
        minecraft.player.connection.sendCommand(value);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float tick) {
        // Avoid a second blur pass in Minecraft 26.3.
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float tick) {
        g.fill(0, 0, width, height, 0x99000000);
        int x = left(), y = top();
        g.fill(x, y, x + PANEL_W, y + PANEL_H, 0xE9182636);
        g.outline(x, y, PANEL_W, PANEL_H, 0xFF60B5E7);
        g.text(font, "Winterfell – Homes & Warps", x + 12, y + 12, 0xFFFFFFFF);
        g.text(font, "Automatische Server-Abfrage über TAB-Vorschläge",
                x + 12, y + 25, 0xFFB7D9ED);
        g.text(font, status.length() > 53 ? status.substring(0, 50) + "…" : status,
                x + 12, y + 67, loading ? 0xFFFFDEA4 : 0xFFB7D9ED);

        if (!loading && current().isEmpty())
            g.text(font, "Keine Ziele angezeigt. Mit Serverliste prüfen.",
                    x + 16, y + 111, 0xFFEEC8A2);

        int pages = Math.max(1, (current().size() + PAGE_SIZE - 1) / PAGE_SIZE);
        g.text(font, "Seite " + (page + 1) + "/" + pages,
                x + 145, y + 286, 0xFFE9F5FF);
        super.extractRenderState(g, mx, my, tick);
    }

    @Override
    public void onClose() {
        requestId++;
        minecraft.gui.setScreen(new PaletteScreen());
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
