package com.pixelforge.module.modules.hud;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.ColorUtil;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class KeystrokesModule extends Module {

    private int baseX = 4;
    private int baseY = 90;
    private final int size = 18;
    private final int gap = 2;

    public KeystrokesModule() {
        super("Keystrokes", "Shows WASD, space and mouse buttons", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.options == null || mc.textRenderer == null) return;

        drawKey(context, "W", baseX + size + gap, baseY, isPressed(mc.options.forwardKey));
        drawKey(context, "A", baseX, baseY + size + gap, isPressed(mc.options.leftKey));
        drawKey(context, "S", baseX + size + gap, baseY + size + gap, isPressed(mc.options.backKey));
        drawKey(context, "D", baseX + (size + gap) * 2, baseY + size + gap, isPressed(mc.options.rightKey));
        drawKey(context, "SPC", baseX, baseY + (size + gap) * 2, isPressed(mc.options.jumpKey), size * 3 + gap * 2, size);

        boolean lmb = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rmb = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        drawKey(context, "LMB", baseX, baseY + (size + gap) * 3, lmb, size + gap + size / 2, size);
        drawKey(context, "RMB", baseX + size + gap + size / 2 + gap, baseY + (size + gap) * 3, rmb, size + gap + size / 2, size);
    }

    private boolean isPressed(KeyBinding binding) {
        return binding.isPressed();
    }

    private void drawKey(DrawContext context, String label, int x, int y, boolean pressed) {
        drawKey(context, label, x, y, pressed, size, size);
    }

    private void drawKey(DrawContext context, String label, int x, int y, boolean pressed, int w, int h) {
        int bg = pressed ? 0xAAFFFFFF : 0xAA111122;
        int border = pressed ? 0xFFFFFFFF : 0xFF3344AA;
        int text = pressed ? 0xFF000000 : 0xFFFFFFFF;

        RenderUtil.drawRect(context, x, y, w, h, bg, border);
        int tw = mc.textRenderer.getWidth(label);
        RenderUtil.drawText(context, mc.textRenderer, label, x + (w - tw) / 2, y + (h - 8) / 2, text, false);
    }
}
