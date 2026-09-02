package com.pixelforge.module.modules.movement;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class ToggleSprintModule extends Module {

    public ToggleSprintModule() {
        super("ToggleSprint", "Toggle sprint on/off instead of holding", Category.MOVEMENT);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.player == null || mc.options == null) return;
        if (isEnabled()) {
            mc.player.setSprinting(true);
        }
    }

    @Override
    public void onDisable() {
        if (mc != null && mc.player != null) {
            mc.player.setSprinting(false);
        }
    }
}
