package com.pixelforge.module.modules.hud;

import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

public class CoordsModule extends Module {

    private final Setting<Boolean> showDimension = addSetting(new Setting<>("Show Dimension", true));
    private final Setting<Boolean> shadow = addSetting(new Setting<>("Shadow", true));

    public CoordsModule() {
        super("Coords", "Shows player coordinates and dimension", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.player == null || mc.textRenderer == null) return;
        int x = HudRenderer.getX(getName());
        int y = HudRenderer.getY(getName());
        BlockPos pos = mc.player.getBlockPos();
        String line1 = String.format("XYZ: %d %d %d", pos.getX(), pos.getY(), pos.getZ());
        RenderUtil.drawText(context, mc.textRenderer, line1, x, y, 0xFF55FFFF, shadow.get());
        if (showDimension.get()) {
            String dim = mc.world != null ? mc.world.getRegistryKey().getValue().getPath() : "?";
            RenderUtil.drawText(context, mc.textRenderer, "Dim: " + dim, x, y + 10, 0xFFAAAAAA, shadow.get());
        }
    }
}
