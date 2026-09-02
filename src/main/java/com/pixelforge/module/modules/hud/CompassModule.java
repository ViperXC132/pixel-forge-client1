package com.pixelforge.module.modules.hud;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class CompassModule extends Module {

    private int x = 4;
    private int y = 62;

    public CompassModule() {
        super("Compass", "Shows facing direction and time", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.player == null || mc.textRenderer == null || mc.world == null) return;

        float yaw = mc.player.getYaw();
        yaw = (yaw % 360 + 360) % 360;
        String dir;
        if (yaw >= 337.5 || yaw < 22.5) dir = "S";
        else if (yaw < 67.5) dir = "SW";
        else if (yaw < 112.5) dir = "W";
        else if (yaw < 157.5) dir = "NW";
        else if (yaw < 202.5) dir = "N";
        else if (yaw < 247.5) dir = "NE";
        else if (yaw < 292.5) dir = "E";
        else dir = "SE";

        long time = mc.world.getTimeOfDay() % 24000;
        int hours = (int) ((time / 1000 + 6) % 24);
        int minutes = (int) ((time % 1000) * 60 / 1000);

        String text = String.format("%s (%.0f°) | %02d:%02d", dir, yaw, hours, minutes);
        RenderUtil.drawText(context, mc.textRenderer, text, x, y, 0xFFFFAA00, true);
    }
}
