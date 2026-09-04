package com.pixelforge.module.modules.hud;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ModListModule extends Module {
    private final Setting<Boolean> rightAlign = addSetting(new Setting<>("Right Align", true));
    private final Setting<Integer> color = addSetting(new Setting<>("Color", 0xAA88FF));

    public ModListModule() {
        super("ModList", "Lists enabled modules", Category.HUD);
        setEnabled(false);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.textRenderer == null) return;
        List<Module> enabled = PixelForgeClient.getInstance().getModuleManager().getModules().stream()
                .filter(Module::isEnabled)
                .filter(m -> m != this)
                .filter(m -> m.getCategory() != Category.HUD && m.getCategory() != Category.SYSTEM)
                .sorted(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        if (enabled.isEmpty()) return;

        String[] lines = enabled.stream().map(Module::getName).toArray(String[]::new);
        int col = 0xFF000000 | (color.get() & 0x00FFFFFF);
        int[] colors = new int[lines.length];
        for (int i = 0; i < colors.length; i++) colors[i] = col;

        int baseX = HudRenderer.getX(getName());
        int baseY = HudRenderer.getY(getName());
        int screenWidth = mc.getWindow().getScaledWidth();

        int maxW = 0;
        for (String line : lines) maxW = Math.max(maxW, mc.textRenderer.getWidth(line));
        int boxW = maxW + RenderUtil.HUD_PAD_X * 2;
        if (rightAlign.get()) {
            baseX = Math.min(baseX, screenWidth - boxW - 4);
            if (baseX < 40) baseX = screenWidth - boxW - 4;
        }
        RenderUtil.drawHudBox(context, mc.textRenderer, lines, colors, baseX, baseY);
    }
}
