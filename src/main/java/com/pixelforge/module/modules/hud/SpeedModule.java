package com.pixelforge.module.modules.hud;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

public class SpeedModule extends Module {

    private int x = 4;
    private int y = 50;
    private double lastX, lastZ;
    private double speed;

    public SpeedModule() {
        super("Speed", "Shows horizontal movement speed in m/s and km/h", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.player == null) return;
        double dx = mc.player.getX() - lastX;
        double dz = mc.player.getZ() - lastZ;
        speed = Math.sqrt(dx * dx + dz * dz) * 20.0; // blocks per second
        lastX = mc.player.getX();
        lastZ = mc.player.getZ();
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.textRenderer == null) return;
        String text = String.format("Speed: %.2f m/s (%.1f km/h)", speed, speed * 3.6);
        RenderUtil.drawText(context, mc.textRenderer, text, x, y, 0xFF55FFFF, true);
    }
}
