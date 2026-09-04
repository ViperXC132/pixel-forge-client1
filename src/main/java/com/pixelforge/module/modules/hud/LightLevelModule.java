package com.pixelforge.module.modules.hud;

import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

public class LightLevelModule extends Module {
    public LightLevelModule() {
        super("Light Level", "Shows block and sky light at player", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.player == null || mc.world == null || mc.textRenderer == null) return;
        BlockPos pos = mc.player.getBlockPos();
        int block = mc.world.getLightLevel(net.minecraft.world.LightType.BLOCK, pos);
        int sky = mc.world.getLightLevel(net.minecraft.world.LightType.SKY, pos);
        String text = String.format("Light: %d block | %d sky", block, sky);
        RenderUtil.drawHudBox(context, mc.textRenderer, text, HudRenderer.getX(getName()), HudRenderer.getY(getName()), 0xFFFFFF55);
    }
}
