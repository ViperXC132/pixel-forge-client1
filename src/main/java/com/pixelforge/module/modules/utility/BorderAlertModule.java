package com.pixelforge.module.modules.utility;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import net.minecraft.world.border.WorldBorder;

public class BorderAlertModule extends Module {

    private boolean warned;

    public BorderAlertModule() {
        super("BorderAlert", "Warns when approaching the world border", Category.UTILITY);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.player == null || mc.world == null) return;

        WorldBorder border = mc.world.getWorldBorder();
        double dist = border.getDistanceInsideBorder(mc.player);

        if (dist < 20 && !warned) {
            PixelForgeClient.getInstance().getNotificationManager()
                    .push(String.format("World border in %.1f blocks!", dist), 0xFFFF5555);
            warned = true;
        } else if (dist >= 30) {
            warned = false;
        }
    }
}
