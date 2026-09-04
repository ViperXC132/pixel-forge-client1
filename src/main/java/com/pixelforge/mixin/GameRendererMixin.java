package com.pixelforge.mixin;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.modules.utility.NoHurtCamModule;
import com.pixelforge.module.modules.utility.ZoomModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void pixelforge$noHurtCam(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (PixelForgeClient.getInstance() == null) return;
        NoHurtCamModule module = PixelForgeClient.getInstance().getModuleManager().getModule(NoHurtCamModule.class);
        if (module != null && module.isEnabled()) ci.cancel();
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void pixelforge$zoomFov(Camera camera, float tickProgress, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        try {
            if (PixelForgeClient.getInstance() == null) return;
            ZoomModule zoom = PixelForgeClient.getInstance().getModuleManager().getModule(ZoomModule.class);
            if (zoom != null && zoom.isZooming()) {
                cir.setReturnValue(zoom.getZoomFov());
            }
        } catch (Throwable ignored) {}
    }
}
