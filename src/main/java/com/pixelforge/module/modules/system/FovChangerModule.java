package com.pixelforge.module.modules.system;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class FovChangerModule extends Module {

    private double customFov = 90.0;

    public FovChangerModule() {
        super("FOV Changer", "Custom field of view", Category.SYSTEM);
    }

    @Override
    public void onEnable() {
        if (mc != null && mc.options != null) {
            mc.options.getFov().setValue(customFov);
        }
    }

    public double getCustomFov() { return customFov; }
    public void setCustomFov(double customFov) { this.customFov = customFov; }
}
