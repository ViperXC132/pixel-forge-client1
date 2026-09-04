package com.pixelforge.module.modules.utility;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import org.lwjgl.glfw.GLFW;

/** Toggle zoom on key press (default C). FOV via GameRendererMixin. */
public class ZoomModule extends Module {
    private boolean zooming;
    private boolean wasKeyDown;
    private final Setting<Integer> zoomFov = addSetting(new Setting<>("Zoom FOV", 20, 5, 50));

    public ZoomModule() {
        super("Zoom", "Toggle zoom (press key once)", Category.UTILITY);
        setKeybind(GLFW.GLFW_KEY_C);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.getWindow() == null) return;
        // Don't toggle while typing in a screen/chat
        if (mc.currentScreen != null) {
            wasKeyDown = false;
            return;
        }
        int key = getKeybind() >= 0 ? getKeybind() : GLFW.GLFW_KEY_C;
        boolean down = GLFW.glfwGetKey(mc.getWindow().getHandle(), key) == GLFW.GLFW_PRESS;
        if (isEnabled() && down && !wasKeyDown) {
            zooming = !zooming;
        }
        wasKeyDown = down;
        if (!isEnabled()) zooming = false;
    }

    @Override
    public void onDisable() {
        zooming = false;
        wasKeyDown = false;
    }

    public boolean isZooming() {
        return zooming && isEnabled();
    }

    public float getZoomFov() {
        return (float) Math.max(5, Math.min(50, zoomFov.get()));
    }
}
