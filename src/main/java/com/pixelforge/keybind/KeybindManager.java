package com.pixelforge.keybind;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.lwjgl.glfw.GLFW;

import java.util.IdentityHashMap;
import java.util.Map;

/** Handles edge-triggered module keybinds without conflicts between modules sharing a key. */
public class KeybindManager {

    private final Map<Module, Boolean> previousStates = new IdentityHashMap<>();

    public void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.currentScreen != null) {
                previousStates.clear();
                return;
            }

            long window = client.getWindow().getHandle();
            for (Module module : PixelForgeClient.getInstance().getModuleManager().getModules()) {
                int key = module.getKeybind();
                if (key <= 0) continue;

                boolean down = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
                boolean wasDown = previousStates.getOrDefault(module, false);

                if (down && !wasDown) {
                    try {
                        module.toggle();
                    } catch (Throwable t) {
                        PixelForgeClient.LOGGER.error("Keybind failed for module {}", module.getName(), t);
                    }
                }
                previousStates.put(module, down);
            }
        });
    }

    public static String getKeyName(int key) {
        if (key <= 0) return "None";
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null) return name.toUpperCase();
        return switch (key) {
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS";
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_ESCAPE -> "ESC";
            default -> "KEY" + key;
        };
    }
}
