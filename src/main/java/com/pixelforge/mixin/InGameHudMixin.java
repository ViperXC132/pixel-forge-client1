package com.pixelforge.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    // Placeholder for cancelling/replacing vanilla HUD elements when our modules take over.
    // Expand as individual HUD replacements are finalized.
}
