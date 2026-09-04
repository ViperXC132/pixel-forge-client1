package com.pixelforge.module.modules.hud;

import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class SpeedModule extends Module {

    private double lastX, lastZ;
    private double speed;
    private final Setting<Boolean> showKmh = addSetting(new Setting<>("Show km/h", true));
    private final Setting<Boolean> shadow = addSetting(new Setting<>("Shadow", true));

    public SpeedModule() {
        super("Speed", "Shows horizontal movement speed in m/s and km/h", Category.HUD);
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
        int x = HudRenderer.getX(getName());
        int y = HudRenderer.getY(getName());
        String text = showKmh.get()
                ? String.format("Speed: %.2f m/s (%.1f km/h)", speed, speed * 3.6)
                : String.format("Speed: %.2f m/s", speed);
        RenderUtil.drawText(context, mc.textRenderer, text, x, y, 0xFF55FFFF, shadow.get());
    }
}
