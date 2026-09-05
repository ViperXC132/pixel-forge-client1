package com.pixelforge.module.modules.visual;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

/** True client-side fullbright: forces gamma without applying Night Vision. */
public class FullbrightModule extends Module {
    private double previousGamma = 1.0;
    private final Setting<Boolean> forceGamma = addSetting(new Setting<>("Force gamma", true));

    public FullbrightModule() {
        super("Fullbright", "True fullbright using client gamma — no Night Vision", Category.VISUAL);
    }

    @Override public void onEnable() {
        if (mc == null || mc.options == null) return;
        previousGamma = mc.options.getGamma().getValue();
        if (forceGamma.get()) mc.options.getGamma().setValue(16.0);
    }

    @Override public void onTick() {
        if (!isEnabled() || mc == null || mc.options == null) return;
        if (forceGamma.get() && mc.options.getGamma().getValue() < 16.0) mc.options.getGamma().setValue(16.0);
    }

    @Override public void onDisable() {
        if (mc == null || mc.options == null) return;
        mc.options.getGamma().setValue(previousGamma);
    }
}
