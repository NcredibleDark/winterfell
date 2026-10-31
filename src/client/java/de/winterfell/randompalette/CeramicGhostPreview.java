package de.winterfell.randompalette;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders a single temporary, translucent vanilla block model at the place
 * that the real block would occupy. Does not touch client world blocks, send
 * packets, or alter placement/rotation logic on the server.
 */
public final class CeramicGhostPreview {
    private static final int GHOST_TINT = 0x8CFFFFFF;
    private static final int LIGHT = 0x00F000F0; // fullbright for legibility

    private CeramicGhostPreview() {}

    public static void register() {
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            Minecraft mc = Minecraft.getInstance();
            if (RandomPaletteClient.config == null
                    || !RandomPaletteClient.config.ceramicGhostPreview
                    || mc.player == null || mc.level == null
                    || mc.gui.screen() != null
                    || !(mc.hitResult instanceof BlockHitResult hit)) return;

            // Random palette may switch the block immediately before placing:
            // showing the held tile in that mode would be misleading.
            if (RandomPaletteClient.config.enabled) return;

            ItemStack inHand = mc.player.getMainHandItem();
            if (!(inHand.getItem() instanceof BlockItem item)
                    || !(item.getBlock() instanceof GlazedTerracottaBlock glazed)) return;

            BlockPos clicked = hit.getBlockPos();
            BlockPos placement = mc.level.getBlockState(clicked).canBeReplaced()
                    ? clicked : clicked.relative(hit.getDirection());
            if (!mc.level.getBlockState(placement).canBeReplaced()) return;

            // Vanilla glazed terracotta faces opposite the player's
            // horizontal facing direction when placed.
            BlockState predicted = glazed.defaultBlockState().setValue(
                    HorizontalDirectionalBlock.FACING, mc.player.getDirection().getOpposite());

            List<BlockStateModelPart> parts = new ArrayList<>();
            mc.getModelManager().getBlockStateModelSet().get(predicted)
                    .collectParts(RandomSource.create(42L), parts);
            if (parts.isEmpty()) return;

            PoseStack pose = context.poseStack();
            var camera = context.levelState().cameraRenderState.pos;
            pose.pushPose();
            pose.translate(placement.getX() - camera.x,
                    placement.getY() - camera.y,
                    placement.getZ() - camera.z);

            // The custom geometry render type uses alpha blending. By submitting
            // the actual baked block model, the glazed pattern and its orientation
            // are identical to the world model, including active resource packs.
            context.submitNodeCollector().submitCustomGeometry(
                    pose, RenderTypes.entityTranslucent(InventoryMenu.BLOCK_ATLAS),
                    (poseState, output) -> {
                        QuadInstance tint = new QuadInstance();
                        tint.setColor(GHOST_TINT);
                        tint.setLightCoords(LIGHT);
                        tint.setOverlayCoords(0);
                        for (BlockStateModelPart part : parts) {
                            for (Direction face : Direction.values()) {
                                for (BakedQuad quad : part.getQuads(face)) {
                                    output.putBakedQuad(poseState, quad, tint);
                                }
                            }
                            for (BakedQuad quad : part.getQuads(null)) {
                                output.putBakedQuad(poseState, quad, tint);
                            }
                        }
                    });
            pose.popPose();
        });
    }
}
