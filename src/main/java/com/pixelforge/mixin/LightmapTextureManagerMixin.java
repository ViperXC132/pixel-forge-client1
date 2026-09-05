package com.pixelforge.mixin;

import com.pixelforge.module.modules.visual.FullbrightModule;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies Fullbright at the render/lightmap stage instead of writing an illegal
 * value into Minecraft's gamma option. This is the same general approach used
 * by modern gamma/fullbright utilities.
 */
@Mixin(LightmapTextureManager.class)
public final class LightmapTextureManagerMixin {
    @Inject(method = "getBrightness(FI)F", at = @At("RETURN"), cancellable = true, require = 0)
    private static void pixelforge$fullbright(float ambientLight, int lightLevel, CallbackInfoReturnable<Float> cir) {
        if (FullbrightModule.isRenderOverrideActive()) {
            cir.setReturnValue(1.0F);
        }
    }

    @Inject(method = "getBrightness(Lnet/minecraft/world/dimension/DimensionType;I)F", at = @At("RETURN"), cancellable = true, require = 0)
    private static void pixelforge$fullbrightDimension(DimensionType type, int lightLevel, CallbackInfoReturnable<Float> cir) {
        if (FullbrightModule.isRenderOverrideActive()) {
            cir.setReturnValue(1.0F);
        }
    }
}
