package com.pixelforge.gui.screens;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.modules.visual.CustomCrosshairModule;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayDeque;
import java.util.Deque;

/** Clean 15x15 pixel-by-pixel crosshair editor. */
public class CrosshairScreen extends Screen {
    private static final int ACCENT = 0xFF3B5BDB;
    private static final int TEXT = 0xFFE8ECFF;
    private static final int DIM = 0xFF8993AC;
    private static final int PANEL = 0xE8141928;
    private static final int[] COLORS = {
            0xFFFFFFFF, 0xFFFF5555, 0xFF55FF55, 0xFF5555FF,
            0xFFFFFF55, 0xFFFF55FF, 0xFF55FFFF, 0xFFFFAA00
    };

    private final Screen parent;
    private final Deque<String> undo = new ArrayDeque<>();
    private final Deque<String> redo = new ArrayDeque<>();
    private int tool = 0; // 0 pencil, 1 eraser
    private int selectedColor = COLORS[0];
    private int gridX, gridY, cell;
    private boolean drawing;

    public CrosshairScreen(Screen parent) {
        super(Text.literal("PixelForge Crosshair Editor"));
        this.parent = parent;
    }

    private CustomCrosshairModule mod() {
        if (PixelForgeClient.getInstance() == null) return null;
        return PixelForgeClient.getInstance().getModuleManager().getModule(CustomCrosshairModule.class);
    }

    private void snapshot(CustomCrosshairModule m) {
        undo.push(m.normalizeGrid());
        while (undo.size() > 30) undo.removeLast();
        redo.clear();
    }

    private void paint(CustomCrosshairModule m, int x, int y, boolean value) {
        if (x < 0 || y < 0 || x >= CustomCrosshairModule.GRID || y >= CustomCrosshairModule.GRID) return;
        if (m.isPixelSet(x, y) == value) return;
        m.setPixel(x, y, value);
    }

    private void editPixel(CustomCrosshairModule m, int mouseX, int mouseY, boolean forceErase) {
        if (cell <= 0) return;
        int x = (mouseX - gridX) / cell;
        int y = (mouseY - gridY) / cell;
        if (x < 0 || y < 0 || x >= CustomCrosshairModule.GRID || y >= CustomCrosshairModule.GRID) return;
        paint(m, x, y, forceErase || tool == 1 ? false : true);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        RenderUtil.fill(c, 0, 0, width, height, 0xFF080B12);
        RenderUtil.fill(c, 0, 0, width, 42, 0xF00A0E18);
        RenderUtil.drawText(c, textRenderer, "CROSSHAIR", 18, 10, TEXT, false);
        RenderUtil.drawText(c, textRenderer, "PIXEL EDITOR", 18, 25, DIM, false);

        CustomCrosshairModule m = mod();
        if (m == null) {
            RenderUtil.drawText(c, textRenderer, "Custom Crosshair module is unavailable", 20, 62, 0xFFFF6666, false);
            super.render(c, mx, my, delta);
            return;
        }

        int side = Math.min(440, Math.max(260, Math.min(height - 150, width - 430)));
        cell = Math.max(14, side / CustomCrosshairModule.GRID);
        side = cell * CustomCrosshairModule.GRID;
        gridX = Math.max(24, (width - 360 - side) / 2);
        gridY = Math.max(72, (height - side) / 2 + 8);

        // Canvas panel.
        RenderUtil.fill(c, gridX - 18, gridY - 18, gridX + side + 18, gridY + side + 18, PANEL);
        RenderUtil.drawBorder(c, gridX - 18, gridY - 18, side + 36, side + 36, 0xFF1E2540);
        for (int y = 0; y < CustomCrosshairModule.GRID; y++) {
            for (int x = 0; x < CustomCrosshairModule.GRID; x++) {
                int left = gridX + x * cell;
                int top = gridY + y * cell;
                int bg = ((x + y) & 1) == 0 ? 0xFF111726 : 0xFF0E1421;
                RenderUtil.fill(c, left, top, left + cell - 1, top + cell - 1, bg);
                if (m.isPixelSet(x, y)) {
                    RenderUtil.fill(c, left + 2, top + 2, left + cell - 2, top + cell - 2,
                            0xFF000000 | (selectedColor & 0xFFFFFF));
                    if (m.isOutline()) RenderUtil.drawBorder(c, left + 1, top + 1, cell - 2, cell - 2, 0xFF000000);
                }
            }
        }

        // Center marker.
        int center = CustomCrosshairModule.GRID / 2;
        RenderUtil.drawBorder(c, gridX + center * cell, gridY + center * cell, cell, cell, 0x88748FFF);

        // Hover highlight.
        if (mx >= gridX && mx < gridX + side && my >= gridY && my < gridY + side) {
            int hx = (mx - gridX) / cell, hy = (my - gridY) / cell;
            RenderUtil.drawBorder(c, gridX + hx * cell, gridY + hy * cell, cell, cell, 0xFFFFFFFF);
        }

        // Right control panel.
        int px = gridX + side + 34;
        int pw = width - px - 22;
        if (pw < 260) { px = width - 300; pw = 278; }
        RenderUtil.fill(c, px, 58, px + pw, height - 22, PANEL);
        RenderUtil.drawBorder(c, px, 58, pw, height - 80, 0xFF1E2540);
        RenderUtil.drawText(c, textRenderer, "TOOLS", px + 16, 72, ACCENT, false);

        int bx = px + 16, by = 92;
        button(c, bx, by, 92, 26, "PENCIL", tool == 0, mx, my);
        button(c, bx + 100, by, 92, 26, "ERASER", tool == 1, mx, my);
        by += 38;
        button(c, bx, by, 92, 26, "UNDO", false, mx, my);
        button(c, bx + 100, by, 92, 26, "REDO", false, mx, my);
        by += 38;
        button(c, bx, by, 92, 26, "CLEAR", false, mx, my);
        button(c, bx + 100, by, 92, 26, "FILL", false, mx, my);
        by += 38;
        button(c, bx, by, 92, 26, "MIRROR H", false, mx, my);
        button(c, bx + 100, by, 92, 26, "MIRROR V", false, mx, my);

        by += 48;
        RenderUtil.drawText(c, textRenderer, "COLOR", bx, by, ACCENT, false);
        by += 18;
        int cx = bx;
        for (int i = 0; i < COLORS.length; i++) {
            int col = COLORS[i];
            RenderUtil.fill(c, cx, by, cx + 22, by + 22, col);
            if ((selectedColor & 0xFFFFFF) == (col & 0xFFFFFF))
                RenderUtil.drawBorder(c, cx - 2, by - 2, 26, 26, 0xFFFFFFFF);
            cx += 30;
            if ((i & 3) == 3) { cx = bx; by += 30; }
        }

        by += 14;
        RenderUtil.drawText(c, textRenderer, "LIVE PREVIEW", bx, by, ACCENT, false);
        by += 20;
        int previewSize = 120;
        int previewX = bx + 54, previewY = by + 52;
        RenderUtil.fill(c, bx, by, bx + 220, by + previewSize, 0xFF0D1220);
        RenderUtil.drawBorder(c, bx, by, 220, previewSize, 0xFF1E2540);
        RenderUtil.fill(c, previewX - 45, previewY, previewX + 45, previewY + 1, 0x16FFFFFF);
        RenderUtil.fill(c, previewX, previewY - 45, previewX + 1, previewY + 45, 0x16FFFFFF);
        m.renderCrosshair(c, previewX, previewY);

        by += previewSize + 18;
        RenderUtil.drawText(c, textRenderer, "Pixel scale: " + m.getScale(), bx, by, DIM, false);
        RenderUtil.drawText(c, textRenderer, "Opacity: " + m.getOpacity(), bx, by + 18, DIM, false);
        RenderUtil.drawText(c, textRenderer, "Outline: " + (m.isOutline() ? "ON" : "OFF"), bx, by + 36, DIM, false);
        RenderUtil.drawText(c, textRenderer, "Replace vanilla: " + (m.isReplaceVanilla() ? "ON" : "OFF"), bx, by + 54, DIM, false);

        RenderUtil.drawText(c, textRenderer, "LMB paint · RMB erase · drag to draw · ESC back", 18, height - 12, DIM, false);
        super.render(c, mx, my, delta);
    }

    private void button(DrawContext c, int x, int y, int w, int h, String label, boolean active, int mx, int my) {
        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + h;
        RenderUtil.fill(c, x, y, x + w, y + h, active ? 0x403B5BDB : (hover ? 0x201E2540 : 0x10101828));
        RenderUtil.drawBorder(c, x, y, w, h, active ? ACCENT : 0xFF1E2540);
        RenderUtil.drawText(c, textRenderer, label, x + 10, y + 9, active ? 0xFF748FFF : DIM, false);
    }

    private boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        CustomCrosshairModule m = mod();
        if (m == null) return super.mouseClicked(click, doubled);
        int mx = (int) click.x(), my = (int) click.y(), b = click.button();

        if (mx >= gridX && mx < gridX + cell * CustomCrosshairModule.GRID && my >= gridY && my < gridY + cell * CustomCrosshairModule.GRID) {
            snapshot(m);
            drawing = true;
            editPixel(m, mx, my, b == 1);
            return true;
        }

        int px = gridX + cell * CustomCrosshairModule.GRID + 34;
        int bx = px + 16, by = 92;
        if (inside(mx, my, bx, by, 92, 26) && b == 0) { tool = 0; return true; }
        if (inside(mx, my, bx + 100, by, 92, 26) && b == 0) { tool = 1; return true; }
        by += 38;
        if (inside(mx, my, bx, by, 92, 26) && b == 0) {
            if (!undo.isEmpty()) { redo.push(m.normalizeGrid()); m.setPixels(undo.pop()); }
            return true;
        }
        if (inside(mx, my, bx + 100, by, 92, 26) && b == 0) {
            if (!redo.isEmpty()) { undo.push(m.normalizeGrid()); m.setPixels(redo.pop()); }
            return true;
        }
        by += 38;
        if (inside(mx, my, bx, by, 92, 26) && b == 0) { snapshot(m); m.clearGrid(); return true; }
        if (inside(mx, my, bx + 100, by, 92, 26) && b == 0) { snapshot(m); m.fillGrid(); return true; }
        by += 38;
        if (inside(mx, my, bx, by, 92, 26) && b == 0) { snapshot(m); m.mirrorHorizontal(); return true; }
        if (inside(mx, my, bx + 100, by, 92, 26) && b == 0) { snapshot(m); m.mirrorVertical(); return true; }

        by += 48 + 18;
        int cx = bx;
        for (int i = 0; i < COLORS.length; i++) {
            if (inside(mx, my, cx, by, 22, 22) && b == 0) { selectedColor = COLORS[i]; m.setColor(selectedColor); return true; }
            cx += 30;
            if ((i & 3) == 3) { cx = bx; by += 30; }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double oldMouseX, double oldMouseY) {
        if (drawing) {
            CustomCrosshairModule m = mod();
            if (m != null) { editPixel(m, (int) click.x(), (int) click.y(), click.button() == 1); return true; }
        }
        return super.mouseDragged(click, oldMouseX, oldMouseY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0 || click.button() == 1) drawing = false;
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        if (input.key() == 256) { client.setScreen(parent); return true; }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() { return false; }
}
