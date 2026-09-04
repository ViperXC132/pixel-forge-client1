package com.pixelforge.module.modules.hud;

import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

public class CoordsModule extends Module {
    private final Setting<Boolean> showDimension = addSetting(new Setting<>("Show Dimension", true));

    public CoordsModule() {
        super("Coords", "Shows player coordinates and dimension", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.player == null || mc.textRenderer == null) return;
        BlockPos pos = mc.player.getBlockPos();
        String line1 = String.format("XYZ: %d %d %d", pos.getX(), pos.getY(), pos.getZ());
        if (showDimension.get()) {
            String dim = mc.world != null ? mc.world.getRegistryKey().getValue().getPath() : "?";
            RenderUtil.drawHudBox(context, mc.textRenderer,
                    new String[]{line1, "Dim: " + dim},
                    new int[]{0xFF55FFFF, 0xFFAAAAAA},
                    HudRenderer.getX(getName()), HudRenderer.getY(getName()));
        } else {
            RenderUtil.drawHudBox(context, mc.textRenderer, line1, HudRenderer.getX(getName()), HudRenderer.getY(getName()), 0xFF55FFFF);
        }
    }
}
