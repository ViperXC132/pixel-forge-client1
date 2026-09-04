package com.pixelforge.module.modules.hud;

import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class SpeedModule extends Module {
    private double lastX, lastZ, speed;
    private final Setting<Boolean> showKmh = addSetting(new Setting<>("Show km/h", true));

    public SpeedModule() {
        super("Speed", "Shows horizontal movement speed", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.player == null) return;
        double dx = mc.player.getX() - lastX;
        double dz = mc.player.getZ() - lastZ;
        speed = Math.sqrt(dx * dx + dz * dz) * 20.0;
        lastX = mc.player.getX();
        lastZ = mc.player.getZ();
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.textRenderer == null) return;
        String text = showKmh.get()
                ? String.format("Speed: %.2f m/s (%.1f km/h)", speed, speed * 3.6)
                : String.format("Speed: %.2f m/s", speed);
        RenderUtil.drawHudBox(context, mc.textRenderer, text, HudRenderer.getX(getName()), HudRenderer.getY(getName()), 0xFF55FFFF);
    }
}
