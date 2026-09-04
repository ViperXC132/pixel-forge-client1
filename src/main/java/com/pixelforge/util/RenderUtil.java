package com.pixelforge.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;

/**
 * Shared draw helpers + simple Lunar-style HUD containers.
 */
public final class RenderUtil {

    private RenderUtil() {}

    public static final int HUD_BG = 0xC0101420;
    public static final int HUD_BORDER = 0xFF2A3350;
    public static final int HUD_PAD_X = 5;
    public static final int HUD_PAD_Y = 3;
    public static final int HUD_LINE = 10;

    public static void fill(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1, y1, x2, y2, color);
    }

    public static void fillGradient(DrawContext context, int x1, int y1, int x2, int y2, int colorTop, int colorBottom) {
        context.fillGradient(x1, y1, x2, y2, colorTop, colorBottom);
    }

    public static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        fill(context, x, y, x + width, y + 1, color);
        fill(context, x, y + height - 1, x + width, y + height, color);
        fill(context, x, y, x + 1, y + height, color);
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

    /** Single-line HUD container box (Lunar-style). Returns height used. */
    public static int drawHudBox(DrawContext c, TextRenderer tr, String text, int x, int y, int textColor) {
        if (text == null) text = "";
        int tw = tr.getWidth(text);
        int w = tw + HUD_PAD_X * 2;
        int h = 9 + HUD_PAD_Y * 2;
        fill(c, x, y, x + w, y + h, HUD_BG);
        drawBorder(c, x, y, w, h, HUD_BORDER);
        drawText(c, tr, text, x + HUD_PAD_X, y + HUD_PAD_Y, textColor, false);
        return h;
    }

    /** Multi-line HUD container. */
    public static int drawHudBox(DrawContext c, TextRenderer tr, String[] lines, int[] colors, int x, int y) {
        if (lines == null || lines.length == 0) return 0;
        int maxW = 0;
        for (String line : lines) {
            if (line == null) continue;
            maxW = Math.max(maxW, tr.getWidth(line));
        }
        int w = maxW + HUD_PAD_X * 2;
        int h = lines.length * HUD_LINE + HUD_PAD_Y * 2;
        fill(c, x, y, x + w, y + h, HUD_BG);
        drawBorder(c, x, y, w, h, HUD_BORDER);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i] == null ? "" : lines[i];
            int col = (colors != null && i < colors.length && colors[i] != 0) ? colors[i] : 0xFFEAF0FF;
            drawText(c, tr, line, x + HUD_PAD_X, y + HUD_PAD_Y + i * HUD_LINE, col, false);
        }
        return h;
    }

    /** Empty box of given size (for custom content like armor icons). */
    public static void drawHudBoxFrame(DrawContext c, int x, int y, int w, int h) {
        fill(c, x, y, x + w, y + h, HUD_BG);
        drawBorder(c, x, y, w, h, HUD_BORDER);
    }
}
