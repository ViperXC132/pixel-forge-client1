package com.pixelforge.module.modules.hud;

import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class MemoryEntityModule extends Module {
    public MemoryEntityModule() {
        super("Memory Entity", "Shows memory usage and entity count", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.textRenderer == null) return;
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long max = rt.maxMemory() / (1024 * 1024);
        int entities = 0;
        if (mc.world != null) {
            for (var ignored : mc.world.getEntities()) entities++;
        }
        String text = String.format("Mem: %d/%d MB | Ent: %d", used, max, entities);
        RenderUtil.drawHudBox(context, mc.textRenderer, text, HudRenderer.getX(getName()), HudRenderer.getY(getName()), 0xFFAAAAFF);
    }
}
