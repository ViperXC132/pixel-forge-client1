package com.pixelforge.module.modules.utility;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import org.lwjgl.glfw.GLFW;

/** Hold key to zoom — FOV applied via GameRendererMixin (reliable on 1.21.x). */
public class ZoomModule extends Module {
    private boolean zooming;
    private final Setting<Integer> zoomFov = addSetting(new Setting<>("Zoom FOV", 20, 5, 50));

    public ZoomModule() {
        super("Zoom", "Hold key to zoom (OptiFine-style)", Category.UTILITY);
        setKeybind(GLFW.GLFW_KEY_C);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.getWindow() == null) return;
        int key = getKeybind() >= 0 ? getKeybind() : GLFW.GLFW_KEY_C;
        boolean down = GLFW.glfwGetKey(mc.getWindow().getHandle(), key) == GLFW.GLFW_PRESS;
        zooming = isEnabled() && down;
    }

    @Override
    public void onDisable() {
        zooming = false;
    }

    public boolean isZooming() {
        return zooming && isEnabled();
    }

    public double getZoomFov() {
        return Math.max(5, Math.min(50, zoomFov.get()));
    }
}
