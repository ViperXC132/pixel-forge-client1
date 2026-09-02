package com.pixelforge.module.modules.hud;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

public class CoordsModule extends Module {

    private int x = 4;
    private int y = 28;

    public CoordsModule() {
        super("Coords", "Shows player coordinates and dimension", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.player == null || mc.textRenderer == null) return;

        BlockPos pos = mc.player.getBlockPos();
        String dim = mc.world != null ? mc.world.getRegistryKey().getValue().getPath() : "?";

        String line1 = String.format("XYZ: %d %d %d", pos.getX(), pos.getY(), pos.getZ());
        String line2 = "Dim: " + dim;

        RenderUtil.drawText(context, mc.textRenderer, line1, x, y, 0xFF55FFFF, true);
        RenderUtil.drawText(context, mc.textRenderer, line2, x, y + 10, 0xFFAAAAAA, true);
    }
}
