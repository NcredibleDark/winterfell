package de.winterfell.randompalette;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomPaletteClient implements ClientModInitializer {
    public static final String MOD_ID = "winterfell_random_palette";
    public static PaletteConfig config;

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, "main"));
    private static KeyMapping openKey;
    private static KeyMapping toggleKey;

    // Slot ownership is remembered after the final block is placed.
    // We never refill a slot if the user has put a different item into it.
    private static final String[] rememberedBlock = new String[9];
    private static final int[] retryDelay = new int[9];
    private static Object previousPlayer;

    @Override
    public void onInitializeClient() {
        config = PaletteConfig.load();
        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.winterfell_random_palette.open", InputConstants.Type.KEYBOARD,
                InputConstants.KEY_K, CATEGORY));
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.winterfell_random_palette.toggle", InputConstants.Type.KEYBOARD,
                InputConstants.KEY_J, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    client.gui.setScreen(new PaletteScreen());
                }
            }
            while (toggleKey.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    config.enabled = !config.enabled;
                    config.save();
                }
            }
            tickAutoRefill(client);
        });
    }

    public static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    public static boolean isSelectable(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem;
    }

    /** Keep track of selected block types while playing and refill depleted slots. */
    private static void tickAutoRefill(Minecraft mc) {
        if (mc.player != previousPlayer) {
            Arrays.fill(rememberedBlock, null);
            Arrays.fill(retryDelay, 0);
            previousPlayer = mc.player;
        }
        if (mc.player == null || config == null) return;

        boolean inventoryScreenOpen = mc.gui.screen() != null;
        // While a screen is open, treat manual inventory rearrangement as intentional.
        for (int slot = 0; slot < 9; slot++) {
            if (retryDelay[slot] > 0) retryDelay[slot]--;
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
                rememberedBlock[slot] = isSelectable(stack) && config.weights.containsKey(itemId(stack))
                        ? itemId(stack) : null;
            } else if (inventoryScreenOpen || !config.enabled) {
                rememberedBlock[slot] = null;
            }
        }
        if (inventoryScreenOpen) return;
        refillOneSlot(mc);
    }

    /**
     * A server-validated vanilla inventory SWAP, never raw local stack edits.
     * Inventory menu indexes 9..35 represent the 27 main-inventory stacks;
     * the SWAP button 0..8 targets exactly one hotbar slot.
     */
    private static void refillOneSlot(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null || mc.level == null || config == null
                || !config.enabled || !config.autoRefill || mc.gui.screen() != null
                || mc.player.containerMenu != mc.player.inventoryMenu) return;

        for (int hotbar = 0; hotbar < 9; hotbar++) {
            String wanted = rememberedBlock[hotbar];
            if (wanted == null || !config.weights.containsKey(wanted)
                    || !mc.player.getInventory().getItem(hotbar).isEmpty()
                    || retryDelay[hotbar] > 0) continue;

            for (int invSlot = 9; invSlot < 36; invSlot++) {
                ItemStack stack = mc.player.getInventory().getItem(invSlot);
                if (isSelectable(stack) && itemId(stack).equals(wanted)) {
                    // Client predicts the same click that the server subsequently validates.
                    retryDelay[hotbar] = 10;
                    mc.gameMode.handleContainerInput(
                            mc.player.containerMenu.containerId,
                            invSlot, hotbar, ContainerInput.SWAP, mc.player);
                    return; // no more than one inventory click per invocation
                }
            }
        }
    }

    public static boolean selectBeforePlacement(Minecraft mc) {
        if (config == null || !config.enabled || mc.player == null || mc.level == null
                || mc.gui.screen() != null || !(mc.hitResult instanceof BlockHitResult)) {
            return true;
        }

        // If a slot just ran out, recover it before selecting the next block.
        refillOneSlot(mc);

        List<WeightedSlot> choices = new ArrayList<>();
        long sum = 0;
        // Ein Blocktyp zaehlt einmal - unabhaengig von mehreren Hotbar-Stacks.
        for (Map.Entry<String, Integer> entry : config.weights.entrySet()) {
            int weight = entry.getValue();
            if (weight < 1) continue;
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getItem(i);
                if (isSelectable(stack) && itemId(stack).equals(entry.getKey())) {
                    sum += weight;
                    choices.add(new WeightedSlot(i, sum));
                    break;
                }
            }
        }
        // Kein zulässiger Block: Vanilla-Rechtsklick verhindern.
        if (choices.isEmpty()) return false;

        long roll = ThreadLocalRandom.current().nextLong(sum);
        for (WeightedSlot choice : choices) {
            if (roll < choice.cumulativeWeight()) {
                mc.player.getInventory().setSelectedSlot(choice.slot());
                return true;
            }
        }
        return false;
    }

    private record WeightedSlot(int slot, long cumulativeWeight) {}
}
