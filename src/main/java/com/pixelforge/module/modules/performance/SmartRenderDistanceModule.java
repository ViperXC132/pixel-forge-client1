package com.pixelforge.module.modules.performance;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

/** Keeps render distance responsive by adapting it to recent client FPS. */
public class SmartRenderDistanceModule extends Module {
    private int minimumDistance = 6;
    private int maximumDistance = 20;
    private int lastApplied = -1;
    private long lastUpdate;

    public SmartRenderDistanceModule() {
        super("Smart Render Distance", "Dynamically adjusts render distance based on FPS", Category.PERFORMANCE);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.options == null || mc.getWindow() == null) return;
        long now = System.currentTimeMillis();
        if (now - lastUpdate < 1000) return;
        lastUpdate = now;

        int fps = mc.getCurrentFps();
        int target;
        if (fps < 30) target = minimumDistance;
        else if (fps < 45) target = Math.max(minimumDistance, maximumDistance - 6);
        else if (fps < 60) target = Math.max(minimumDistance, maximumDistance - 3);
        else target = maximumDistance;

        int current = mc.options.getViewDistance().getValue();
        if (lastApplied < 0) lastApplied = current;
        if (current != target) mc.options.getViewDistance().setValue(target);
    }

    @Override
    public void onDisable() {
        if (mc != null && mc.options != null && lastApplied >= 0) {
            mc.options.getViewDistance().setValue(lastApplied);
        }
        lastApplied = -1;
    }

    public int getMinimumDistance() { return minimumDistance; }
    public void setMinimumDistance(int value) { minimumDistance = Math.max(2, Math.min(32, value)); }
    public int getMaximumDistance() { return maximumDistance; }
    public void setMaximumDistance(int value) { maximumDistance = Math.max(minimumDistance, Math.min(32, value)); }
}
