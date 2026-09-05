package com.pixelforge.module.modules.visual;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.ColorUtil;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

/** Pixel-perfect 15x15 crosshair renderer. The editor owns the pixels. */
public class CustomCrosshairModule extends Module {
    public static final int GRID = 15;

    public enum Style { CROSS, DOT, CIRCLE, CROSS_DOT, GAP, CUSTOM }

    private final Setting<String> pixels = addSetting(new Setting<>("Pixel grid", defaultGrid()));
    private final Setting<Integer> color = addSetting(new Setting<>("Color", 0xFFFFFF, 0, 0xFFFFFF));
    private final Setting<Integer> opacity = addSetting(new Setting<>("Opacity", 230, 0, 255));
    private final Setting<Integer> scale = addSetting(new Setting<>("Pixel scale", 2, 1, 8));
    private final Setting<Boolean> outline = addSetting(new Setting<>("Outline", true));
    private final Setting<Boolean> replaceVanilla = addSetting(new Setting<>("Replace vanilla", true));

    // Compatibility state for the existing CrosshairScreen API.
    private Style style = Style.CUSTOM;
    private int size = 5;
    private int thickness = 1;
    private int gap = 2;
    private int customTop = 5;
    private int customBottom = 5;
    private int customLeft = 5;
    private int customRight = 5;
    private boolean customDot = true;

    public CustomCrosshairModule() {
        super("Custom Crosshair", "Draw a crosshair pixel-by-pixel on a 15x15 canvas", Category.VISUAL);
    }

    private static String defaultGrid() {
        StringBuilder s = new StringBuilder(GRID * GRID);
        int c = GRID / 2;
        for (int y = 0; y < GRID; y++) for (int x = 0; x < GRID; x++) s.append(x == c || y == c ? '1' : '0');
        return s.toString();
    }

    public boolean isPixelSet(int x, int y) {
        if (x < 0 || y < 0 || x >= GRID || y >= GRID) return false;
        String g = pixels.get();
        int i = y * GRID + x;
        return i < g.length() && g.charAt(i) == '1';
    }

    public void setPixel(int x, int y, boolean value) {
        if (x < 0 || y < 0 || x >= GRID || y >= GRID) return;
        StringBuilder g = new StringBuilder(normalizeGrid());
        g.setCharAt(y * GRID + x, value ? '1' : '0');
        pixels.set(g.toString());
    }

    public void clearGrid() { pixels.set("0".repeat(GRID * GRID)); }
    public void fillGrid() { pixels.set("1".repeat(GRID * GRID)); }

    public void applyPreset(String name) {
        clearGrid();
        int c = GRID / 2;
        switch (name) {
            case "Dot" -> setPixel(c, c, true);
            case "Plus" -> { for (int i = 2; i < 13; i++) { setPixel(c, i, true); setPixel(i, c, true); } }
            case "X" -> { for (int i = 2; i < 13; i++) { setPixel(i, i, true); setPixel(14 - i, i, true); } }
            case "Square" -> { for (int i = 3; i < 12; i++) { setPixel(i, 3, true); setPixel(i, 11, true); setPixel(3, i, true); setPixel(11, i, true); } }
            case "T" -> { for (int i = 2; i < 13; i++) setPixel(i, 3, true); for (int i = 3; i < 12; i++) setPixel(c, i, true); }
            default -> { for (int i = 0; i < GRID; i++) { setPixel(c, i, true); setPixel(i, c, true); } }
        }
    }

    public String normalizeGrid() {
        String g = pixels.get();
        if (g == null) g = "";
        StringBuilder out = new StringBuilder(GRID * GRID);
        for (int i = 0; i < GRID * GRID; i++) out.append(i < g.length() && g.charAt(i) == '1' ? '1' : '0');
        return out.toString();
    }

    public void setPixels(String grid) {
        if (grid == null) grid = "";
        StringBuilder normalized = new StringBuilder(GRID * GRID);
        for (int i = 0; i < GRID * GRID; i++) normalized.append(i < grid.length() && grid.charAt(i) == '1' ? '1' : '0');
        pixels.set(normalized.toString());
    }

    public void mirrorHorizontal() {
        String g = normalizeGrid();
        StringBuilder out = new StringBuilder(GRID * GRID);
        for (int y = 0; y < GRID; y++) for (int x = 0; x < GRID; x++) out.append(g.charAt(y * GRID + (GRID - 1 - x)));
        pixels.set(out.toString());
    }

    public void mirrorVertical() {
        String g = normalizeGrid();
        StringBuilder out = new StringBuilder(GRID * GRID);
        for (int y = 0; y < GRID; y++) for (int x = 0; x < GRID; x++) out.append(g.charAt((GRID - 1 - y) * GRID + x));
        pixels.set(out.toString());
    }

    public void renderCrosshair(DrawContext context, int centerX, int centerY) {
        if (!isEnabled()) return;
        int p = Math.max(1, scale.get());
        int half = GRID / 2;
        int alpha = Math.max(0, Math.min(255, opacity.get()));
        int col = ColorUtil.setAlpha(color.get(), alpha);
        int out = 0xFF000000 | (alpha << 24);
        String g = normalizeGrid();
        for (int y = 0; y < GRID; y++) for (int x = 0; x < GRID; x++) {
            if (g.charAt(y * GRID + x) != '1') continue;
            int px = centerX + (x - half) * p;
            int py = centerY + (y - half) * p;
            if (outline.get()) RenderUtil.fill(context, px - 1, py - 1, px + p + 1, py + p + 1, out);
            RenderUtil.fill(context, px, py, px + p, py + p, col);
        }
    }

    public boolean shouldReplaceVanilla() { return isEnabled() && replaceVanilla.get(); }
    public int getColor() { return color.get(); }
    public void setColor(int v) { color.set(v & 0xFFFFFF); }
    public int getOpacity() { return opacity.get(); }
    public void setOpacity(int v) { opacity.set(Math.max(0, Math.min(255, v))); }
    public int getScale() { return scale.get(); }
    public void setScale(int v) { scale.set(Math.max(1, Math.min(8, v))); }
    public boolean isOutline() { return outline.get(); }
    public void setOutline(boolean v) { outline.set(v); }
    public boolean isReplaceVanilla() { return replaceVanilla.get(); }
    public void setReplaceVanilla(boolean v) { replaceVanilla.set(v); }

    // Existing CrosshairScreen compatibility API.
    public Style getStyle() { return style; }
    public void setStyle(int value) {
        Style[] values = Style.values();
        style = values[Math.max(0, Math.min(values.length - 1, value))];
    }
    public void setStyle(Style value) { if (value != null) style = value; }
    public int getSize() { return size; }
    public void setSize(int value) { size = Math.max(1, Math.min(32, value)); }
    public int getThickness() { return thickness; }
    public void setThickness(int value) { thickness = Math.max(1, Math.min(8, value)); }
    public int getGap() { return gap; }
    public void setGap(int value) { gap = Math.max(0, Math.min(16, value)); }
    public int getCustomTop() { return customTop; }
    public void setCustomTop(int value) { customTop = Math.max(0, Math.min(24, value)); }
    public int getCustomBottom() { return customBottom; }
    public void setCustomBottom(int value) { customBottom = Math.max(0, Math.min(24, value)); }
    public int getCustomLeft() { return customLeft; }
    public void setCustomLeft(int value) { customLeft = Math.max(0, Math.min(24, value)); }
    public int getCustomRight() { return customRight; }
    public void setCustomRight(int value) { customRight = Math.max(0, Math.min(24, value)); }
    public boolean isCustomDot() { return customDot; }
    public void setCustomDot() { customDot = !customDot; }
    public void setCustomDot(boolean value) { customDot = value; }
}
