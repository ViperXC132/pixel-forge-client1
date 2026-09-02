package com.pixelforge.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;

/**
 * Helper methods that only use GuiGraphics / DrawContext (Yarn name in 1.21.11 is still DrawContext in some mappings,
 * but the official / common name used by Fabric docs is GuiGraphics. We accept both via the parameter type).
 */
public final class RenderUtil {

    private RenderUtil() {}

    public static void fill(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1, y1, x2, y2, color);
    }

    public static void fillGradient(DrawContext context, int x1, int y1, int x2, int y2, int colorTop, int colorBottom) {
        context.fillGradient(x1, y1, x2, y2, colorTop, colorBottom);
    }

    public static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        // top
        fill(context, x, y, x + width, y + 1, color);
        // bottom
        fill(context, x, y + height - 1, x + width, y + height, color);
        // left
        fill(context, x, y, x + 1, y + height, color);
        // right
        fill(context, x + width - 1, y, x + width, y + height, color);
    }

    public static void drawRect(DrawContext context, int x, int y, int width, int height, int fillColor, int borderColor) {
        fill(context, x, y, x + width, y + height, fillColor);
        if (borderColor != 0) {
            drawBorder(context, x, y, width, height, borderColor);
        }
    }

    public static void drawText(DrawContext context, TextRenderer textRenderer, String text, int x, int y, int color, boolean shadow) {
        context.drawText(textRenderer, text, x, y, color, shadow);
    }

    public static void drawCenteredText(DrawContext context, TextRenderer textRenderer, String text, int centerX, int y, int color, boolean shadow) {
        int width = textRenderer.getWidth(text);
        context.drawText(textRenderer, text, centerX - width / 2, y, color, shadow);
    }

    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}
