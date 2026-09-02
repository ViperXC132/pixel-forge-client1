package com.pixelforge.module.modules.system;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class FovChangerModule extends Module {
    private int customFov = 90;

    public FovChangerModule() {
        super("FOV Changer", "Custom field of view", Category.SYSTEM);
    }

    @Override
    public void onEnable() {
        if (mc != null && mc.options != null) mc.options.getFov().setValue(customFov);
    }

    public int getCustomFov() { return customFov; }
    public void setCustomFov(int customFov) { this.customFov = customFov; }
}
