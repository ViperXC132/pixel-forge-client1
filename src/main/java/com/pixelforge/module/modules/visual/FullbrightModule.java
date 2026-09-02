package com.pixelforge.module.modules.visual;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class FullbrightModule extends Module {

    private double previousGamma = 1.0;

    public FullbrightModule() {
        super("Fullbright", "Maximum brightness (client-side)", Category.VISUAL);
    }

    @Override
    public void onEnable() {
        if (mc != null && mc.options != null) {
            previousGamma = mc.options.getGamma().getValue();
            mc.options.getGamma().setValue(16.0);
        }
    }

    @Override
    public void onDisable() {
        if (mc != null && mc.options != null) {
            mc.options.getGamma().setValue(previousGamma);
        }
    }

    @Override
    public void onTick() {
        if (isEnabled() && mc != null && mc.options != null) {
            if (mc.options.getGamma().getValue() < 16.0) {
                mc.options.getGamma().setValue(16.0);
            }
        }
    }
}
