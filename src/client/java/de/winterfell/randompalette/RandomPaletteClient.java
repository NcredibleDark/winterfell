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
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
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
                    client.player.displayClientMessage(Component.literal(
                            "§bWinterfell Palette: " + (config.enabled ? "§aAKTIV" : "§cAUS")), true);
                }
            }
        });
    }

    public static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    public static boolean isSelectable(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem;
    }

    public static boolean selectBeforePlacement(Minecraft mc) {
        if (config == null || !config.enabled || mc.player == null || mc.level == null
                || mc.gui.screen() != null || !(mc.hitResult instanceof BlockHitResult)) {
            return true;
        }

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
