package com.pixelforge.module.modules.hud;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.stream.Collectors;

public class ModListModule extends Module {

    private int x = -1; // right aligned
    private int y = 4;

    public ModListModule() {
        super("ModList", "Lists enabled PixelForge modules", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.textRenderer == null) return;

        List<Module> enabled = PixelForgeClient.getInstance().getModuleManager().getModules().stream()
                .filter(Module::isEnabled)
                .filter(m -> m.getCategory() != Category.HUD && m.getCategory() != Category.SYSTEM)
                .collect(Collectors.toList());

        int screenWidth = mc.getWindow().getScaledWidth();
        int offset = 0;

        for (Module m : enabled) {
            String name = m.getName();
            int tw = mc.textRenderer.getWidth(name);
            int drawX = (x < 0) ? screenWidth - tw - 4 : x;
            RenderUtil.drawText(context, mc.textRenderer, name, drawX, y + offset, 0xFFAA88FF, true);
            offset += 10;
        }
    }
}
