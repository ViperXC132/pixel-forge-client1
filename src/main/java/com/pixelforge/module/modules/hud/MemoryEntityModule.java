package com.pixelforge.module.modules.hud;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class MemoryEntityModule extends Module {

    private int x = 4;
    private int y = 110;

    public MemoryEntityModule() {
        super("Memory Entities", "Shows used memory and entity count", Category.HUD);
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
        RenderUtil.drawText(context, mc.textRenderer, text, x, y, 0xFFAAAAFF, true);
    }
}
