package com.pixelforge.module.modules.hud;

import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CpsModule extends Module {
    private final List<Long> leftClicks = new ArrayList<>();
    private final List<Long> rightClicks = new ArrayList<>();
    private boolean leftWasDown, rightWasDown;
    private final Setting<Boolean> showRight = addSetting(new Setting<>("Show Right CPS", true));

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
        prune(leftClicks, now);
        prune(rightClicks, now);
    }

    private void prune(List<Long> list, long now) {
        Iterator<Long> it = list.iterator();
        while (it.hasNext()) if (now - it.next() > 1000) it.remove();
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.textRenderer == null) return;
        String text = showRight.get()
                ? ("CPS: " + leftClicks.size() + " | " + rightClicks.size())
                : ("CPS: " + leftClicks.size());
        RenderUtil.drawHudBox(context, mc.textRenderer, text, HudRenderer.getX(getName()), HudRenderer.getY(getName()), 0xFFFFAA55);
    }
}
