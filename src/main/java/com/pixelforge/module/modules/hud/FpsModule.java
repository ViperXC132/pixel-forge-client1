package com.pixelforge.module.modules.hud;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class FpsModule extends Module {

    private final HudElement pos = new HudElement(4, 4);

    public FpsModule() {
        super("FPS", "Displays current frames per second", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.textRenderer == null) return;
        int fps = mc.getCurrentFps();
        String text = "FPS: " + fps;
        int color = fps >= 100 ? 0xFF55FF55 : (fps >= 60 ? 0xFFFFFF55 : 0xFFFF5555);
        RenderUtil.drawText(context, mc.textRenderer, text, pos.getX(), pos.getY(), color, true);
    }

    public static class HudElement {
        private int x, y;
        public HudElement(int x, int y) { this.x = x; this.y = y; }
        public int getX() { return x; }
        public int getY() { return y; }
        public void setX(int x) { this.x = x; }
        public void setY(int y) { this.y = y; }
    }
}
