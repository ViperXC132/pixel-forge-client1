package com.pixelforge.module.modules.visual;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

/**
 * Gamma-Utils-style fullbright.
 *
 * The vanilla gamma option is never modified. Instead, the client lightmap
 * brightness calculation is overridden while this module is enabled.
 */
public class FullbrightModule extends Module {
    private static volatile boolean renderOverrideActive;
    private final Setting<Boolean> forceGamma = addSetting(new Setting<>("Force gamma", true));

    public FullbrightModule() {
        super("Fullbright", "True client-side fullbright using a lightmap override — no Night Vision", Category.VISUAL);
    }

    @Override public void onEnable() {
        renderOverrideActive = forceGamma.get();
    }

    @Override public void onDisable() {
        renderOverrideActive = false;
    }

    @Override public void onTick() {
        if (!isEnabled()) renderOverrideActive = false;
        else renderOverrideActive = forceGamma.get();
    }

    public static boolean isRenderOverrideActive() {
        return renderOverrideActive;
    }
}
