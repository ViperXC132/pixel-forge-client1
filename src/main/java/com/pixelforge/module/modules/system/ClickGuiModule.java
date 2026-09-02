package com.pixelforge.module.modules.system;

import com.pixelforge.gui.ClickGui;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import org.lwjgl.glfw.GLFW;

public class ClickGuiModule extends Module {

    public ClickGuiModule() {
        super("ClickGUI", "Opens the module configuration GUI", Category.SYSTEM);
        setKeybind(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void onEnable() {
        if (mc != null) {
            mc.setScreen(new ClickGui());
        }
        // Keep the module "enabled" only while the screen is open conceptually,
        // but we immediately turn the toggle off so the keybind can re-open it.
        setEnabled(false);
    }
}
