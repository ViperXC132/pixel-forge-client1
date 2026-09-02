package com.pixelforge.module.modules.hud;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.module.modules.movement.ToggleSprintModule;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class ToggleSprintHudModule extends Module {

    private int x = 4;
    private int y = 200;

    public ToggleSprintHudModule() {
        super("ToggleSprint Indicator", "Shows when ToggleSprint is active", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.textRenderer == null) return;
        ToggleSprintModule sprint = PixelForgeClient.getInstance().getModuleManager().getModule(ToggleSprintModule.class);
        if (sprint != null && sprint.isEnabled()) {
            RenderUtil.drawText(context, mc.textRenderer, "[Sprinting]", x, y, 0xFF55FF55, true);
        }
    }
}
