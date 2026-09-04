package com.pixelforge.module.modules.utility;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import org.lwjgl.glfw.GLFW;

public class ZoomModule extends Module {
    private boolean zooming;
    private int previousFov = 70;
    private final Setting<Integer> zoomFov = addSetting(new Setting<>("Zoom FOV", 20, 5, 50));
    private final Setting<Boolean> smooth = addSetting(new Setting<>("Smooth", false));

    public ZoomModule() {
        super("Zoom", "OptiFine-style zoom (hold key)", Category.UTILITY);
        setKeybind(GLFW.GLFW_KEY_C);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.options == null) return;
        long window = mc.getWindow().getHandle();
        int key = getKeybind() >= 0 ? getKeybind() : GLFW.GLFW_KEY_C;
        boolean keyDown = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
        int target = Math.max(5, Math.min(50, zoomFov.get()));
        if (keyDown && !zooming) {
            previousFov = mc.options.getFov().getValue();
            mc.options.getFov().setValue(target);
            zooming = true;
        } else if (!keyDown && zooming) {
            mc.options.getFov().setValue(previousFov);
            zooming = false;
        } else if (keyDown && zooming && mc.options.getFov().getValue() != target) {
            mc.options.getFov().setValue(target);
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
