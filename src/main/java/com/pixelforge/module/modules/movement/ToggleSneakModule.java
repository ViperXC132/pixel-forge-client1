package com.pixelforge.module.modules.movement;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class ToggleSneakModule extends Module {

    public ToggleSneakModule() {
        super("ToggleSneak", "Toggle sneak on/off instead of holding", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.player == null || mc.options == null) return;
        if (isEnabled()) {
            mc.player.setSneaking(true);
        }
    }

    @Override
    public void onDisable() {
        if (mc != null && mc.player != null) {
            mc.player.setSneaking(false);
        }
    }
}
