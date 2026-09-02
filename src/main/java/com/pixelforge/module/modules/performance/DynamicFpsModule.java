package com.pixelforge.module.modules.performance;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

/** Reduces client workload while the game window is unfocused. */
public class DynamicFpsModule extends Module {
    private int activeFps = 60;
    private int unfocusedFps = 15;
    private int previousFps = -1;

    public DynamicFpsModule() {
        super("Dynamic FPS", "Reduces FPS when the game window is not focused", Category.PERFORMANCE);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.options == null) return;
        int target = mc.isWindowFocused() ? activeFps : unfocusedFps;
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

    public int getActiveFps() { return activeFps; }
    public void setActiveFps(int value) { activeFps = Math.max(30, Math.min(1000, value)); }
    public int getUnfocusedFps() { return unfocusedFps; }
    public void setUnfocusedFps(int value) { unfocusedFps = Math.max(5, Math.min(120, value)); }
}
