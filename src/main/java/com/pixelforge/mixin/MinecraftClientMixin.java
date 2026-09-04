package com.pixelforge.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "updateWindowTitle", at = @At("HEAD"), cancellable = true)
    private void pixelforge$title(CallbackInfo ci) {
        MinecraftClient self = (MinecraftClient) (Object) this;
        if (self.getWindow() != null) {
            self.getWindow().setTitle("PixelForge 1.21.11");
        }
        ci.cancel();
    }
}
