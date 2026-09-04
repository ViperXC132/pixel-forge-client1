package com.pixelforge.module.modules.performance;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

/** Reduces client workload while the game window is unfocused. */
public class DynamicFpsModule extends Module {
    private final Setting<Integer> activeFps = addSetting(new Setting<>("Focused FPS", 60, 30, 260));
    private final Setting<Integer> unfocusedFps = addSetting(new Setting<>("Unfocused FPS", 15, 5, 60));
    private int previousFps = -1;

    public DynamicFpsModule() {
        super("Dynamic FPS", "Reduces FPS when the game window is not focused", Category.PERFORMANCE);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.options == null) return;
        int target = mc.isWindowFocused() ? activeFps.get() : unfocusedFps.get();
        if (target <= 0) return;
        int current = mc.options.getMaxFps().getValue();
        if (current != target) {
            if (previousFps < 0) previousFps = current;
            mc.options.getMaxFps().setValue(target);
        }
    }

    @Override
    public void onDisable() {
        if (mc != null && mc.options != null && previousFps >= 0) {
            mc.options.getMaxFps().setValue(previousFps);
            previousFps = -1;
        }
    }
}
