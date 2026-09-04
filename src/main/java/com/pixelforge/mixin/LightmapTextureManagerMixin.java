package com.pixelforge.mixin;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.modules.visual.FullbrightModule;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Render-side fullbright. It overrides Minecraft's light brightness calculation rather than
 * applying a status effect or modifying the user's gamma option.
 */
@Mixin(LightmapTextureManager.class)
public final class LightmapTextureManagerMixin {
    @Inject(method = "getBrightness(Lnet/minecraft/world/dimension/DimensionType;I)F", at = @At("HEAD"), cancellable = true)
    private static void pixelforge$fullbrightDimension(DimensionType type, int lightLevel, CallbackInfoReturnable<Float> cir) {
        if (enabled()) cir.setReturnValue(1.0F);
    }

    @Inject(method = "getBrightness(FI)F", at = @At("HEAD"), cancellable = true)
    private static void pixelforge$fullbrightAmbient(float ambientLight, int lightLevel, CallbackInfoReturnable<Float> cir) {
        if (enabled()) cir.setReturnValue(1.0F);
    }

    private static boolean enabled() {
        try {
            if (PixelForgeClient.getInstance() == null) return false;
            FullbrightModule module = PixelForgeClient.getInstance().getModuleManager().getModule(FullbrightModule.class);
            return module != null && module.isEnabled();
        } catch (Throwable ignored) {
            return false;
        }
    }
}
