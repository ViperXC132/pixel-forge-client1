package com.pixelforge.module.modules.hud;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CpsModule extends Module {

    private final List<Long> leftClicks = new ArrayList<>();
    private final List<Long> rightClicks = new ArrayList<>();
    private boolean leftWasDown;
    private boolean rightWasDown;
    private int x = 4;
    private int y = 16;

    public CpsModule() {
        super("CPS", "Shows left and right clicks per second", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.getWindow() == null) return;
        long window = mc.getWindow().getHandle();
        long now = System.currentTimeMillis();

        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rightDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        if (leftDown && !leftWasDown) leftClicks.add(now);
        if (rightDown && !rightWasDown) rightClicks.add(now);

        leftWasDown = leftDown;
        rightWasDown = rightDown;

        leftClicks.removeIf(t -> now - t > 1000);
        rightClicks.removeIf(t -> now - t > 1000);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.textRenderer == null) return;
        String text = "CPS: " + leftClicks.size() + " | " + rightClicks.size();
        RenderUtil.drawText(context, mc.textRenderer, text, x, y, 0xFFFFFFFF, true);
    }
}
