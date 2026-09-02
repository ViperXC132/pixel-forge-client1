package com.pixelforge.module.modules.system;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class FovChangerModule extends Module {
    private int customFov = 90;
    private Integer previousFov;

    public FovChangerModule() {
        super("FOV Changer", "Custom field of view", Category.SYSTEM);
    }

    @Override
    public void onEnable() {
        if (mc != null && mc.options != null) {
            previousFov = mc.options.getFov().getValue();
            mc.options.getFov().setValue(clamp(customFov));
        }
    }

    @Override
    public void onTick() {
        if (mc != null && mc.options != null) {
            mc.options.getFov().setValue(clamp(customFov));
        }
    }

    @Override
    public void onDisable() {
        if (mc != null && mc.options != null && previousFov != null) {
            mc.options.getFov().setValue(previousFov);
            previousFov = null;
        }
    }

    public int getCustomFov() { return customFov; }

    public void setCustomFov(int customFov) { this.customFov = clamp(customFov); }

    private static int clamp(int fov) {
        return Math.max(30, Math.min(110, fov));
    }
}
