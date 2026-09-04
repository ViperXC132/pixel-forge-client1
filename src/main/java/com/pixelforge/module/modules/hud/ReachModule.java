package com.pixelforge.module.modules.hud;

import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.HitResult;

public class ReachModule extends Module {
    public ReachModule() {
        super("Reach", "Shows distance to crosshair target", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.player == null || mc.textRenderer == null) return;
        double dist = 0;
        HitResult hit = mc.crosshairTarget;
        if (hit != null && hit.getType() != HitResult.Type.MISS) {
            dist = mc.player.getEyePos().distanceTo(hit.getPos());
        }
        String text = String.format("Reach: %.2f", dist);
        RenderUtil.drawHudBox(context, mc.textRenderer, text, HudRenderer.getX(getName()), HudRenderer.getY(getName()), 0xFF55FFFF);
    }
}
