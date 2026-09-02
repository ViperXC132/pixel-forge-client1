package com.pixelforge.module.modules.visual;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.ColorUtil;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

/**
 * Fully configurable custom crosshair. No placeholders — every setting is used when drawing.
 */
public class CustomCrosshairModule extends Module {

    public enum Style { CROSS, DOT, CIRCLE, CROSS_DOT, GAP, CUSTOM }

    private Style style = Style.CROSS;
    private int color = 0xFFFFFFFF;
    private int opacity = 230;      // 0-255
    private int size = 6;
    private int thickness = 2;
    private int gap = 3;
    private boolean replaceVanilla = true;
    private boolean outline = true;
    private int outlineColor = 0xFF000000;

    // CUSTOM style: independent arm lengths / offsets
    private int customTop = 6;
    private int customBottom = 6;
    private int customLeft = 6;
    private int customRight = 6;
    private boolean customDot = true;

    public CustomCrosshairModule() {
        super("Custom Crosshair", "Fully customizable crosshair — replaces vanilla", Category.VISUAL);
        setEnabled(true);
    }

    public void renderCrosshair(DrawContext context, int centerX, int centerY) {
        if (!isEnabled()) return;

        int col = ColorUtil.setAlpha(color, opacity);
        int out = ColorUtil.setAlpha(outlineColor, opacity);

        switch (style) {
            case DOT -> {
                int r = Math.max(1, thickness);
                if (outline) fillBox(context, centerX - r - 1, centerY - r - 1, centerX + r + 1, centerY + r + 1, out);
                fillBox(context, centerX - r, centerY - r, centerX + r, centerY + r, col);
            }
            case CIRCLE -> drawCircle(context, centerX, centerY, size, thickness, col, outline ? out : 0);
            case CROSS_DOT -> {
                drawCross(context, centerX, centerY, size, thickness, gap, col, outline ? out : 0);
                int r = Math.max(1, thickness);
                fillBox(context, centerX - r, centerY - r, centerX + r, centerY + r, col);
            }
            case GAP -> drawCross(context, centerX, centerY, size, thickness, gap, col, outline ? out : 0);
            case CUSTOM -> drawCustom(context, centerX, centerY, col, outline ? out : 0);
            default -> drawCross(context, centerX, centerY, size, thickness, 0, col, outline ? out : 0); // CROSS
        }
    }

    private void drawCross(DrawContext context, int cx, int cy, int len, int th, int g, int col, int out) {
        int half = th / 2;
        // horizontal left
        if (out != 0) {
            fillBox(context, cx - len - g - 1, cy - half - 1, cx - g + 1, cy + half + 1, out);
            fillBox(context, cx + g - 1, cy - half - 1, cx + len + g + 1, cy + half + 1, out);
            fillBox(context, cx - half - 1, cy - len - g - 1, cx + half + 1, cy - g + 1, out);
            fillBox(context, cx - half - 1, cy + g - 1, cx + half + 1, cy + len + g + 1, out);
        }
        fillBox(context, cx - len - g, cy - half, cx - g, cy + half + (th % 2 == 0 ? 0 : 1), col);
        fillBox(context, cx + g + 1, cy - half, cx + len + g + 1, cy + half + (th % 2 == 0 ? 0 : 1), col);
        fillBox(context, cx - half, cy - len - g, cx + half + (th % 2 == 0 ? 0 : 1), cy - g, col);
        fillBox(context, cx - half, cy + g + 1, cx + half + (th % 2 == 0 ? 0 : 1), cy + len + g + 1, col);
    }

    private void drawCustom(DrawContext context, int cx, int cy, int col, int out) {
        int th = thickness;
        int half = th / 2;
        int g = gap;

        if (out != 0) {
            if (customLeft > 0) fillBox(context, cx - customLeft - g - 1, cy - half - 1, cx - g + 1, cy + half + 1, out);
            if (customRight > 0) fillBox(context, cx + g - 1, cy - half - 1, cx + customRight + g + 1, cy + half + 1, out);
            if (customTop > 0) fillBox(context, cx - half - 1, cy - customTop - g - 1, cx + half + 1, cy - g + 1, out);
            if (customBottom > 0) fillBox(context, cx - half - 1, cy + g - 1, cx + half + 1, cy + customBottom + g + 1, out);
        }

        if (customLeft > 0) fillBox(context, cx - customLeft - g, cy - half, cx - g, cy + half + 1, col);
        if (customRight > 0) fillBox(context, cx + g + 1, cy - half, cx + customRight + g + 1, cy + half + 1, col);
        if (customTop > 0) fillBox(context, cx - half, cy - customTop - g, cx + half + 1, cy - g, col);
        if (customBottom > 0) fillBox(context, cx - half, cy + g + 1, cx + half + 1, cy + customBottom + g + 1, col);

        if (customDot) {
            int r = Math.max(1, th);
            fillBox(context, cx - r, cy - r, cx + r, cy + r, col);
        }
    }

    private void drawCircle(DrawContext context, int cx, int cy, int radius, int th, int col, int out) {
        for (int i = -radius - 1; i <= radius + 1; i++) {
            for (int j = -radius - 1; j <= radius + 1; j++) {
                int d2 = i * i + j * j;
                int outer = radius * radius;
                int inner = Math.max(0, radius - th) * Math.max(0, radius - th);
                if (d2 <= outer && d2 >= inner) {
                    boolean edge = d2 > outer - radius || d2 < inner + radius;
                    RenderUtil.fill(context, cx + i, cy + j, cx + i + 1, cy + j + 1, edge && out != 0 ? out : col);
                }
            }
        }
    }

    private void fillBox(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        if (x2 <= x1 || y2 <= y1) return;
        RenderUtil.fill(context, x1, y1, x2, y2, color);
    }

    public boolean shouldReplaceVanilla() {
        return isEnabled() && replaceVanilla;
    }

    // --- getters / setters used by CrosshairScreen ---
    public Style getStyle() { return style; }
    public void setStyle(Style style) { this.style = style; }
    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
    public int getOpacity() { return opacity; }
    public void setOpacity(int opacity) { this.opacity = Math.max(0, Math.min(255, opacity)); }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = Math.max(1, Math.min(32, size)); }
    public int getThickness() { return thickness; }
    public void setThickness(int thickness) { this.thickness = Math.max(1, Math.min(8, thickness)); }
    public int getGap() { return gap; }
    public void setGap(int gap) { this.gap = Math.max(0, Math.min(16, gap)); }
    public boolean isOutline() { return outline; }
    public void setOutline(boolean outline) { this.outline = outline; }
    public boolean isReplaceVanilla() { return replaceVanilla; }
    public void setReplaceVanilla(boolean replaceVanilla) { this.replaceVanilla = replaceVanilla; }

    public int getCustomTop() { return customTop; }
    public void setCustomTop(int v) { this.customTop = Math.max(0, Math.min(24, v)); }
    public int getCustomBottom() { return customBottom; }
    public void setCustomBottom(int v) { this.customBottom = Math.max(0, Math.min(24, v)); }
    public int getCustomLeft() { return customLeft; }
    public void setCustomLeft(int v) { this.customLeft = Math.max(0, Math.min(24, v)); }
    public int getCustomRight() { return customRight; }
    public void setCustomRight(int v) { this.customRight = Math.max(0, Math.min(24, v)); }
    public boolean isCustomDot() { return customDot; }
    public void setCustomDot(boolean customDot) { this.customDot = customDot; }
}
