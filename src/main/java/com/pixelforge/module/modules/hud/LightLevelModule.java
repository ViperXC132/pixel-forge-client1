package com.pixelforge.module.modules.hud;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

public class LightLevelModule extends Module {

    private int x = 4;
    private int y = 98;

    public LightLevelModule() {
        super("Light Level", "Shows block and sky light at player position", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.player == null || mc.world == null || mc.textRenderer == null) return;

        BlockPos pos = mc.player.getBlockPos();
        int block = mc.world.getLightLevel(LightType.BLOCK, pos);
        int sky = mc.world.getLightLevel(LightType.SKY, pos);

        String text = String.format("Light: Block %d | Sky %d", block, sky);
        int color = block < 8 ? 0xFFFF5555 : 0xFF55FF55;
        RenderUtil.drawText(context, mc.textRenderer, text, x, y, color, true);
    }
}
