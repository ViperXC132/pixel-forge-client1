package com.pixelforge.mixin;

import com.pixelforge.gui.PauseScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Routes the vanilla ESC pause screen into the PixelForge pause UI. */
@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin {
    @Inject(method="init", at=@At("HEAD"), cancellable=true)
    private void pixelforge$replacePause(CallbackInfo ci){
        MinecraftClient client=MinecraftClient.getInstance();
        if(client!=null && client.currentScreen instanceof GameMenuScreen){
            client.setScreen(new PauseScreen(null));
            ci.cancel();
        }
    }
}
