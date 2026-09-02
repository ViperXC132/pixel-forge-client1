package com.pixelforge.mixin;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.modules.utility.NoHurtCamModule;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void pixelforge$noHurtCam(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (PixelForgeClient.getInstance() == null) return;

        NoHurtCamModule module = PixelForgeClient.getInstance()
                .getModuleManager()
                .getModule(NoHurtCamModule.class);

        if (module != null && module.isEnabled()) {
            ci.cancel();
        }
    }
}
