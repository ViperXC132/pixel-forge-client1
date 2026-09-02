package com.pixelforge.module.modules.hud;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class ReachModule extends Module {

    private int x = 4;
    private int y = 122;

    public ReachModule() {
        super("Reach", "Shows distance to the block or entity you are looking at", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.player == null || mc.textRenderer == null) return;

        HitResult hit = mc.crosshairTarget;
        String text = "Reach: -";

        if (hit != null) {
            if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult bhr) {
                double dist = mc.player.getEyePos().distanceTo(bhr.getPos());
                text = String.format("Reach: %.2f (block)", dist);
            } else if (hit.getType() == HitResult.Type.ENTITY && hit instanceof EntityHitResult ehr) {
                double dist = mc.player.getEyePos().distanceTo(ehr.getPos());
                text = String.format("Reach: %.2f (entity)", dist);
            }
        }

        RenderUtil.drawText(context, mc.textRenderer, text, x, y, 0xFFFFCC88, true);
    }
}
