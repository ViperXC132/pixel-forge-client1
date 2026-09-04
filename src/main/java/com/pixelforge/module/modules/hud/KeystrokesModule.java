package com.pixelforge.module.modules.hud;

import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class KeystrokesModule extends Module {
    private final Setting<Integer> size = addSetting(new Setting<>("Size", 20, 14, 32));
    private final Setting<Integer> gap = addSetting(new Setting<>("Gap", 2, 1, 6));
    private final Setting<Boolean> showMouse = addSetting(new Setting<>("Show Mouse", true));
    private final Setting<Boolean> showSpace = addSetting(new Setting<>("Show Space", true));

    public KeystrokesModule() {
        super("Keystrokes", "WASD / mouse key display", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.options == null || mc.textRenderer == null) return;
        int s = Math.max(14, Math.min(32, size.get()));
        int g = Math.max(1, Math.min(6, gap.get()));
        int baseX = HudRenderer.getX(getName());
        int baseY = HudRenderer.getY(getName());

        drawKey(context, "W", baseX + s + g, baseY, s, s, isPressed(mc.options.forwardKey));
        drawKey(context, "A", baseX, baseY + s + g, s, s, isPressed(mc.options.leftKey));
        drawKey(context, "S", baseX + s + g, baseY + s + g, s, s, isPressed(mc.options.backKey));
        drawKey(context, "D", baseX + (s + g) * 2, baseY + s + g, s, s, isPressed(mc.options.rightKey));

        int row = 2;
        if (showSpace.get()) {
            drawKey(context, "SPACE", baseX, baseY + (s + g) * row, s * 3 + g * 2, s, isPressed(mc.options.jumpKey));
            row++;
        }
        if (showMouse.get()) {
            long handle = mc.getWindow().getHandle();
            boolean lmb = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            boolean rmb = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
            int half = (s * 3 + g * 2 - g) / 2;
            drawKey(context, "LMB", baseX, baseY + (s + g) * row, half, s, lmb);
            drawKey(context, "RMB", baseX + half + g, baseY + (s + g) * row, half, s, rmb);
        }
    }

    private boolean isPressed(KeyBinding binding) {
        return binding != null && binding.isPressed();
    }

    private void drawKey(DrawContext context, String label, int x, int y, int w, int h, boolean pressed) {
        int bg = pressed ? 0xB0FFFFFF : 0x90000000;
        int border = pressed ? 0xFFFFFFFF : 0x40FFFFFF;
        int textCol = pressed ? 0xFF000000 : 0xFFFFFFFF;
        RenderUtil.fill(context, x, y, x + w, y + h, bg);
        RenderUtil.drawBorder(context, x, y, w, h, border);
        int tw = mc.textRenderer.getWidth(label);
        RenderUtil.drawText(context, mc.textRenderer, label, x + Math.max(1, (w - tw) / 2), y + Math.max(1, (h - 8) / 2), textCol, false);
    }
}
