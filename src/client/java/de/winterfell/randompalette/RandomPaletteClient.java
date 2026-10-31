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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
    private static KeyMapping toggleHudKey;

    // Slot ownership is remembered after the final block is placed.
    // We never refill a slot if the user has put a different item into it.
    private static final String[] rememberedBlock = new String[9];
    private static final ItemStack[] rememberedStack = new ItemStack[9];
    private static int nextInventoryClick = 0;
    private static final int[] retryDelay = new int[9];
    private static Object previousPlayer;
    private static final Deque<String> smartRecent = new ArrayDeque<>();

    @Override
    public void onInitializeClient() {
        config = PaletteConfig.load();
        PaletteHud.register();
        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.winterfell_random_palette.open", InputConstants.Type.KEYBOARD,
                InputConstants.KEY_K, CATEGORY));
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.winterfell_random_palette.toggle", InputConstants.Type.KEYBOARD,
                InputConstants.KEY_J, CATEGORY));
        toggleHudKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.winterfell_random_palette.toggle_hud", InputConstants.Type.KEYBOARD,
                InputConstants.KEY_U, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    client.gui.setScreen(new PaletteScreen());
                }
            }
            while (toggleKey.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    config.enabled = !config.enabled;
                    smartRecent.clear();
                    config.save();
                }
            }
            while (toggleHudKey.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    config.showHud = !config.showHud;
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
            Arrays.fill(rememberedStack, null);
            Arrays.fill(retryDelay, 0);
            nextInventoryClick = 0;
            smartRecent.clear();
            previousPlayer = mc.player;
        }
        if (mc.player == null || config == null) return;

        boolean inventoryScreenOpen = mc.gui.screen() != null;
        if (nextInventoryClick > 0) nextInventoryClick--;
        // While a screen is open, treat manual inventory rearrangement as intentional.
        for (int slot = 0; slot < 9; slot++) {
            if (retryDelay[slot] > 0) retryDelay[slot]--;
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
                rememberedStack[slot] = stack.isStackable() ? stack.copyWithCount(1) : null;
                rememberedBlock[slot] = isSelectable(stack) && config.weights.containsKey(itemId(stack))
                        ? itemId(stack) : null;
            } else if (inventoryScreenOpen) {
                rememberedBlock[slot] = null;
                rememberedStack[slot] = null;
            }
        }
        if (inventoryScreenOpen) return;
        refillOneSlot(mc);
    }

    /**
     * The two refill modes are independent:
     * - generalRefill: any stackable item, but only the same item and components.
     * - autoRefill: selected palette blocks while random palette is active.
     * Never touch non-empty hotbar slots or non-stackable tools.
     * Only vanilla inventory SWAP clicks, validated by the server.
     */
    private static void refillOneSlot(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null || mc.level == null || config == null
                || (!config.generalRefill && !(config.enabled && config.autoRefill))
                || mc.gui.screen() != null || nextInventoryClick > 0
                || mc.player.containerMenu != mc.player.inventoryMenu) return;

        for (int hotbar = 0; hotbar < 9; hotbar++) {
            if (!mc.player.getInventory().getItem(hotbar).isEmpty() || retryDelay[hotbar] > 0) continue;
            ItemStack wantedStack = config.generalRefill ? rememberedStack[hotbar] : null;
            String wantedBlock = config.enabled && config.autoRefill ? rememberedBlock[hotbar] : null;
            boolean hasGeneral = wantedStack != null && !wantedStack.isEmpty()
                    && wantedStack.isStackable();
            if (!hasGeneral && wantedBlock == null) continue;

            for (int invSlot = 9; invSlot < 36; invSlot++) {
                ItemStack candidate = mc.player.getInventory().getItem(invSlot);
                if (candidate.isEmpty()) continue;
                boolean exactMatch = hasGeneral && ItemStack.isSameItemSameComponents(wantedStack, candidate);
                boolean paletteMatch = wantedBlock != null && isSelectable(candidate)
                        && itemId(candidate).equals(wantedBlock);
                if (exactMatch || paletteMatch) {
                    retryDelay[hotbar] = 10;
                    nextInventoryClick = 2;
                    mc.gameMode.handleContainerInput(
                            mc.player.containerMenu.containerId,
                            invSlot, hotbar, ContainerInput.SWAP, mc.player);
                    return; // at most one synchronized inventory click every two ticks
                }
            }
        }
    }

    /**
     * Choose the fastest effective mining tool already present in the hotbar.
     * Selection happens only while attacking a block, never when targeting an entity.
     * The tool itself is never moved from the inventory.
     */
    public static void selectToolBeforeMining(Minecraft mc) {
        if (config == null || !config.autoToolSwitch || mc.player == null || mc.level == null
                || mc.gui.screen() != null || !(mc.hitResult instanceof BlockHitResult hit)) return;

        BlockState state = mc.level.getBlockState(hit.getBlockPos());
        if (state.isAir()) return;

        int selected = mc.player.getInventory().getSelectedSlot();
        ItemStack held = mc.player.getInventory().getItem(selected);
        float heldSpeed = held.isEmpty() ? 1.0f : held.getDestroySpeed(state);
        boolean heldCorrect = !held.isEmpty() && held.isCorrectToolForDrops(state);
        int bestSlot = -1;
        float bestSpeed = Math.max(1.0f, heldSpeed);
        boolean bestCorrect = heldCorrect;

        for (int slot = 0; slot < 9; slot++) {
            if (slot == selected) continue;
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (stack.isEmpty() || stack.getItem() instanceof BlockItem || stack.getMaxStackSize() > 1)
                continue;
            if (stack.isDamageableItem() && stack.nextDamageWillBreak()) continue;

            float speed = stack.getDestroySpeed(state);
            if (speed <= 1.0f || !Float.isFinite(speed)) continue;
            boolean correct = stack.isCorrectToolForDrops(state);

            // A harvest-capable tool beats a faster tool that would lose drops.
            if ((!bestCorrect && correct) ||
                    (bestCorrect == correct && speed > bestSpeed + 0.001f)) {
                bestSlot = slot;
                bestSpeed = speed;
                bestCorrect = correct;
            }
        }
        if (bestSlot >= 0) mc.player.getInventory().setSelectedSlot(bestSlot);
    }

    public static boolean selectBeforePlacement(Minecraft mc) {
        if (config == null || !config.enabled || mc.player == null || mc.level == null
                || mc.gui.screen() != null || !(mc.hitResult instanceof BlockHitResult)) {
            return true;
        }

        // If a slot just ran out, recover it before selecting the next block.
        refillOneSlot(mc);

        // Exact same hotbar-only candidates and refill behavior as v1.2.1.
        List<WeightedSlot> choices = new ArrayList<>();
        double totalWeight = 0.0;
        for (Map.Entry<String, Integer> entry : config.weights.entrySet()) {
            int weight = entry.getValue();
            if (weight < 1) continue;
            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = mc.player.getInventory().getItem(slot);
                if (isSelectable(stack) && itemId(stack).equals(entry.getKey())) {
                    double adjustedWeight = weight;
                    if (config.smartRandom && config.weights.size() > 1) {
                        // Prefer variation but never prohibit repetition.
                        int distance = 0;
                        for (String recent : smartRecent) {
                            if (recent.equals(entry.getKey())) {
                                adjustedWeight *= distance == 0 ? 0.22 : distance == 1 ? 0.58 : 0.78;
                                break;
                            }
                            distance++;
                        }
                    }
                    totalWeight += adjustedWeight;
                    choices.add(new WeightedSlot(slot, entry.getKey(), totalWeight));
                    break;
                }
            }
        }
        // Never select tools or arbitrary blocks from the inventory.
        if (choices.isEmpty()) return false;
        double roll = ThreadLocalRandom.current().nextDouble(totalWeight);
        for (WeightedSlot choice : choices) {
            if (roll < choice.cumulativeWeight()) {
                mc.player.getInventory().setSelectedSlot(choice.slot());
                if (config.smartRandom) rememberSmartChoice(choice.id());
                else smartRecent.clear();
                return true;
            }
        }
        return false;
    }

    private static void rememberSmartChoice(String id) {
        smartRecent.addFirst(id);
        while (smartRecent.size() > 3) smartRecent.removeLast();
    }

    private record WeightedSlot(int slot, String id, double cumulativeWeight) {}
}
