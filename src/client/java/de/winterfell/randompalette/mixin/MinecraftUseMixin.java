package de.winterfell.randompalette.mixin;

import de.winterfell.randompalette.RandomPaletteClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftUseMixin {
    @Inject(method = "startAttack", at = @At("HEAD"))
    private void winterfell_random_palette$beforeAttack(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        RandomPaletteClient.selectToolBeforeMining((Minecraft) (Object) this);
    }

    @Inject(method = "continueAttack", at = @At("HEAD"))
    private void winterfell_random_palette$continueAttack(boolean down, CallbackInfo ci) {
        if (down) RandomPaletteClient.selectToolBeforeMining((Minecraft) (Object) this);
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void winterfell_random_palette$chooseBeforePlace(CallbackInfo ci) {
        if (!RandomPaletteClient.selectBeforePlacement((Minecraft) (Object) this)) {
            ci.cancel();
        }
    }
}
