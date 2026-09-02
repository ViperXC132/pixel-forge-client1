package com.pixelforge.hud;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.Module;
import com.pixelforge.module.modules.hud.*;
import net.minecraft.client.gui.DrawContext;

public class HudRenderer {

    public void render(DrawContext context, float tickDelta) {
        if (PixelForgeClient.getInstance() == null) return;

        for (Module module : PixelForgeClient.getInstance().getModuleManager().getModules()) {
            if (module.isEnabled() && module.getCategory() == com.pixelforge.module.Category.HUD) {
                module.onRender(context, tickDelta);
            }
        }
    }
}
