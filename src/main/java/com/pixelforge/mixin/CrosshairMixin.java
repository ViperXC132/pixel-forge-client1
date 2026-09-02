package com.pixelforge.mixin;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.modules.visual.CustomCrosshairModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class CrosshairMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void pixelforge$customCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (PixelForgeClient.getInstance() == null) return;

        CustomCrosshairModule module = PixelForgeClient.getInstance()
                .getModuleManager()
                .getModule(CustomCrosshairModule.class);

        if (module != null && module.shouldReplaceVanilla()) {
            int cx = context.getScaledWindowWidth() / 2;
            int cy = context.getScaledWindowHeight() / 2;
            module.renderCrosshair(context, cx, cy);
            ci.cancel();
        }
    }
}
