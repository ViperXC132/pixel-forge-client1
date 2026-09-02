package com.pixelforge.mixin;

import com.pixelforge.gui.TitleScreenOverride;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    /**
     * Replace vanilla title screen after it finishes constructing.
     * Using RETURN avoids recursion and init-order problems.
     */
    @Inject(method = "init", at = @At("RETURN"))
    private void pixelforge$replaceTitleScreen(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.currentScreen instanceof TitleScreen) {
            client.setScreen(new TitleScreenOverride());
        }
    }
}
