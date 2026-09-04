package com.pixelforge.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;

/** Lunar palette: white text + transparent glass panels. No purple. */
public final class RenderUtil {

    private RenderUtil() {}

    public static final int HUD_BG = 0x90000000;
    public static final int HUD_BORDER = 0x40FFFFFF;
    public static final int HUD_PAD_X = 5;
    public static final int HUD_PAD_Y = 3;
    public static final int HUD_LINE = 10;

    public static final int PANEL = 0xC0101010;
    public static final int PANEL_SOFT = 0xA0121212;
    public static final int BORDER = 0x55FFFFFF;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int DIM = 0xFFB0B0B0;
    public static final int MUTED = 0xFF707070;
    public static final int ACCENT = 0xFFFFFFFF;
    public static final int GREEN = 0xFF90EE90;
    public static final int RED = 0xFFFF6B6B;

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
        if (borderColor != 0) drawBorder(context, x, y, width, height, borderColor);
    }

    public static void drawRoundedPanel(DrawContext c, int x, int y, int w, int h, int fill, int border) {
        fill(c, x + 2, y, x + w - 2, y + h, fill);
        fill(c, x, y + 2, x + w, y + h - 2, fill);
        fill(c, x + 1, y + 1, x + 2, y + 2, fill);
        fill(c, x + w - 2, y + 1, x + w - 1, y + 2, fill);
        fill(c, x + 1, y + h - 2, x + 2, y + h - 1, fill);
        fill(c, x + w - 2, y + h - 2, x + w - 1, y + h - 1, fill);
        if (border != 0) {
            fill(c, x + 2, y, x + w - 2, y + 1, border);
            fill(c, x + 2, y + h - 1, x + w - 2, y + h, border);
            fill(c, x, y + 2, x + 1, y + h - 2, border);
            fill(c, x + w - 1, y + 2, x + w, y + h - 2, border);
            fill(c, x + 1, y + 1, x + 2, y + 2, border);
            fill(c, x + w - 2, y + 1, x + w - 1, y + 2, border);
            fill(c, x + 1, y + h - 2, x + 2, y + h - 1, border);
            fill(c, x + w - 2, y + h - 2, x + w - 1, y + h - 1, border);
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
            int col = (colors != null && i < colors.length && colors[i] != 0) ? colors[i] : TEXT;
            drawText(c, tr, line, x + HUD_PAD_X, y + HUD_PAD_Y + i * HUD_LINE, col, false);
        }
        return h;
    }

    public static void drawHudBoxFrame(DrawContext c, int x, int y, int w, int h) {
        fill(c, x, y, x + w, y + h, HUD_BG);
        drawBorder(c, x, y, w, h, HUD_BORDER);
    }
}
