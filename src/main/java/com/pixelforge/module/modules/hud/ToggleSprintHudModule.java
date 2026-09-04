package com.pixelforge.module.modules.hud;

import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class ToggleSprintHudModule extends Module {
    public ToggleSprintHudModule() {
        super("Sprint HUD", "Shows sprint status", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.player == null || mc.textRenderer == null) return;
        boolean sprinting = mc.player.isSprinting();
        String text = sprinting ? "Sprinting" : "Walking";
        int color = sprinting ? 0xFF55FF55 : 0xFFAAAAAA;
        RenderUtil.drawHudBox(context, mc.textRenderer, text, HudRenderer.getX(getName()), HudRenderer.getY(getName()), color);
    }
}
