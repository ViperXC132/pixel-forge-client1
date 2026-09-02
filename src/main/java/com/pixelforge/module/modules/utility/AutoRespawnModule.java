package com.pixelforge.module.modules.utility;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import net.minecraft.client.gui.screen.DeathScreen;

public class AutoRespawnModule extends Module {

    public AutoRespawnModule() {
        super("AutoRespawn", "Automatically respawns after death", Category.UTILITY);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.player == null) return;
        if (mc.currentScreen instanceof DeathScreen) {
            mc.player.requestRespawn();
            mc.setScreen(null);
        }
    }
}
