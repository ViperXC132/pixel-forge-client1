package com.pixelforge.module.modules.hud;

import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class FpsModule extends Module {

    private final Setting<Boolean> shadow = addSetting(new Setting<>("Shadow", true));
    private final Setting<Boolean> showLabel = addSetting(new Setting<>("Show Label", true));

    public FpsModule() {
        super("FPS", "Displays current frames per second", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.textRenderer == null) return;
        int fps = mc.getCurrentFps();
        String text = showLabel.get() ? ("FPS: " + fps) : String.valueOf(fps);
        int color = fps >= 100 ? 0xFF55FF55 : (fps >= 60 ? 0xFFFFFF55 : 0xFFFF5555);
        int x = HudRenderer.getX(getName());
        int y = HudRenderer.getY(getName());
        RenderUtil.drawText(context, mc.textRenderer, text, x, y, color, shadow.get());
    }
}
