package com.pixelforge.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationManager {

    private final List<Notification> notifications = new ArrayList<>();
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public void push(String message, int color) {
        notifications.add(new Notification(message, color, System.currentTimeMillis()));
    }

    public void tick() {
        long now = System.currentTimeMillis();
        notifications.removeIf(n -> now - n.startTime > 3500);
    }

    public void render(DrawContext context) {
        if (notifications.isEmpty()) return;

        int y = 12;
        long now = System.currentTimeMillis();

        for (Notification n : notifications) {
            long age = now - n.startTime;
            float alpha = 1.0f;
            if (age > 2800) {
                alpha = 1.0f - (age - 2800) / 700f;
            }

            int bg = ColorUtil.setAlpha(0xCC111122, (int) (alpha * 200));
            int textColor = ColorUtil.setAlpha(n.color, (int) (alpha * 255));

            int width = mc.textRenderer.getWidth(n.message) + 16;
            int x = mc.getWindow().getScaledWidth() - width - 8;

            RenderUtil.drawRect(context, x, y, width, 18, bg, ColorUtil.setAlpha(0xFF3344AA, (int) (alpha * 255)));
            RenderUtil.drawText(context, mc.textRenderer, n.message, x + 8, y + 5, textColor, true);

            y += 22;
        }
    }

    private static class Notification {
        final String message;
        final int color;
        final long startTime;

        Notification(String message, int color, long startTime) {
            this.message = message;
            this.color = color;
            this.startTime = startTime;
        }
    }
}
