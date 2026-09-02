package com.pixelforge.module.modules.utility;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import org.lwjgl.glfw.GLFW;

public class ZoomModule extends Module {

    private boolean zooming;
    private double previousFov = 70.0;
    private final double zoomFov = 20.0;

    public ZoomModule() {
        super("Zoom", "OptiFine-style zoom (hold key)", Category.UTILITY);
        setKeybind(GLFW.GLFW_KEY_C);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.options == null) return;

        long window = mc.getWindow().getHandle();
        boolean keyDown = GLFW.glfwGetKey(window, getKeybind()) == GLFW.GLFW_PRESS;

        if (keyDown && !zooming) {
            previousFov = mc.options.getFov().getValue();
            mc.options.getFov().setValue(zoomFov);
            zooming = true;
        } else if (!keyDown && zooming) {
            mc.options.getFov().setValue(previousFov);
            zooming = false;
        }
    }

    @Override
    public void onDisable() {
        if (zooming && mc != null && mc.options != null) {
            mc.options.getFov().setValue(previousFov);
            zooming = false;
        }
    }
}
