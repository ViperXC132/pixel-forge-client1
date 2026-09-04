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

/**
 * Optional array-list of enabled modules (Lunar-style).
 * Disabled by default so it does not clutter the top-right until the user turns it on.
 */
public class ModListModule extends Module {

    private final Setting<Boolean> rightAlign = addSetting(new Setting<>("Right Align", true));
    private final Setting<Boolean> shadow = addSetting(new Setting<>("Shadow", true));
    private final Setting<Boolean> background = addSetting(new Setting<>("Background", false));
    private final Setting<Integer> color = addSetting(new Setting<>("Color", 0xAA88FF));
    private final Setting<Integer> spacing = addSetting(new Setting<>("Spacing", 10));

    public ModListModule() {
        super("ModList", "Lists enabled modules (array list style)", Category.HUD);
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

        int screenWidth = mc.getWindow().getScaledWidth();
        int baseX = HudRenderer.getX(getName());
        int baseY = HudRenderer.getY(getName());
        if (rightAlign.get() && baseX < 40) {
            baseX = screenWidth - 8;
        }

        int y = baseY;
        int space = Math.max(8, Math.min(20, spacing.get()));
        int col = 0xFF000000 | (color.get() & 0x00FFFFFF);

        for (Module m : enabled) {
            String name = m.getName();
            int tw = mc.textRenderer.getWidth(name);
            int drawX = rightAlign.get() ? (baseX - tw) : baseX;

            if (background.get()) {
                RenderUtil.fill(context, drawX - 3, y - 1, drawX + tw + 3, y + 9, 0x80081018);
            }
            RenderUtil.drawText(context, mc.textRenderer, name, drawX, y, col, shadow.get());
            y += space;
        }
    }
}
