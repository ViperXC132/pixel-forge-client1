package com.pixelforge.gui.screens;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.modules.visual.CustomCrosshairModule;
import com.pixelforge.util.ColorUtil;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Full crosshair builder — every control changes the live preview and in-game crosshair.
 */
public class CrosshairScreen extends Screen {

    private final Screen parent;

    private static final int ACCENT = 0xFF3B5BDB;
    private static final int TEXT = 0xFFC8D0E0;
    private static final int DIM = 0xFF8892A8;
    private static final int MUTED = 0xFF3D4A6A;
    private static final int PANEL = 0xD0101424; // Lunar opaque-transparent

    private static final int[] PRESET_COLORS = {
            0xFFFFFFFF, 0xFFFF5555, 0xFF55FF55, 0xFF5555FF,
            0xFFFFFF55, 0xFFFF55FF, 0xFF55FFFF, 0xFFFFAA00
    };

    public CrosshairScreen(Screen parent) {
        super(Text.literal("Crosshair"));
        this.parent = parent;
    }

    private CustomCrosshairModule mod() {
        if (PixelForgeClient.getInstance() == null) return null;
        return PixelForgeClient.getInstance().getModuleManager().getModule(CustomCrosshairModule.class);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dim world behind — Lunar style
        RenderUtil.fill(context, 0, 0, width, height, 0x99080A12);

        RenderUtil.fill(context, 0, 0, width, 32, 0xE00A0C14);
        RenderUtil.drawText(context, textRenderer, "Crosshair Builder", 14, 11, TEXT, false);

        CustomCrosshairModule m = mod();
        if (m == null) {
            RenderUtil.drawText(context, textRenderer, "Module not loaded", 14, 50, 0xFFFF5555, false);
            return;
        }

        // Style row
        RenderUtil.drawText(context, textRenderer, "STYLE", 16, 44, ACCENT, false);
        CustomCrosshairModule.Style[] styles = CustomCrosshairModule.Style.values();
        String[] labels = {"Cross", "Dot", "Circle", "Cross+Dot", "Gap", "Custom"};
        int sx = 16;
        for (int i = 0; i < styles.length; i++) {
            boolean on = m.getStyle() == styles[i];
            int tw = textRenderer.getWidth(labels[i]) + 14;
            RenderUtil.fill(context, sx, 58, sx + tw, 74, on ? 0x403B5BDB : PANEL);
            RenderUtil.drawBorder(context, sx, 58, tw, 16, on ? ACCENT : 0xFF1E2540);
            RenderUtil.drawText(context, textRenderer, labels[i], sx + 7, 62, on ? 0xFF748FFF : DIM, false);
            sx += tw + 6;
        }

        // Preview box
        int prevX = 16, prevY = 90, prevS = 90;
        RenderUtil.fill(context, prevX, prevY, prevX + prevS, prevY + prevS, 0xFF0D1020);
        RenderUtil.drawBorder(context, prevX, prevY, prevS, prevS, 0xFF1E2540);
        // faint grid
        RenderUtil.fill(context, prevX + prevS / 2, prevY, prevX + prevS / 2 + 1, prevY + prevS, 0x18FFFFFF);
        RenderUtil.fill(context, prevX, prevY + prevS / 2, prevX + prevS, prevY + prevS / 2 + 1, 0x18FFFFFF);
        m.renderCrosshair(context, prevX + prevS / 2, prevY + prevS / 2);

        // Settings panel
        int px = 120, py = 90;
        RenderUtil.fill(context, px, py, width - 16, py + 200, PANEL);
        RenderUtil.drawBorder(context, px, py, width - 16 - px, 200, 0xFF1E2540);

        int row = py + 10;
        row = drawSlider(context, px + 10, row, "Size", m.getSize(), 1, 20, mouseX, mouseY);
        row = drawSlider(context, px + 10, row, "Thickness", m.getThickness(), 1, 6, mouseX, mouseY);
        row = drawSlider(context, px + 10, row, "Gap", m.getGap(), 0, 12, mouseX, mouseY);
        row = drawSlider(context, px + 10, row, "Opacity", m.getOpacity(), 40, 255, mouseX, mouseY);

        // Color presets
        RenderUtil.drawText(context, textRenderer, "Color", px + 10, row, DIM, false);
        int cx = px + 50;
        for (int c : PRESET_COLORS) {
            boolean sel = (m.getColor() & 0x00FFFFFF) == (c & 0x00FFFFFF);
            RenderUtil.fill(context, cx, row - 1, cx + 12, row + 11, c);
            if (sel) RenderUtil.drawBorder(context, cx - 1, row - 2, 14, 14, 0xFFFFFFFF);
            cx += 16;
        }
        row += 18;

        // Toggles
        row = drawToggle(context, px + 10, row, "Outline", m.isOutline());
        row = drawToggle(context, px + 10, row, "Replace vanilla", m.isReplaceVanilla());
        row = drawToggle(context, px + 10, row, "Enabled", m.isEnabled());

        if (m.getStyle() == CustomCrosshairModule.Style.CUSTOM) {
            row += 4;
            RenderUtil.drawText(context, textRenderer, "CUSTOM ARMS", px + 10, row, ACCENT, false);
            row += 12;
            row = drawSlider(context, px + 10, row, "Top", m.getCustomTop(), 0, 16, mouseX, mouseY);
            row = drawSlider(context, px + 10, row, "Bottom", m.getCustomBottom(), 0, 16, mouseX, mouseY);
            row = drawSlider(context, px + 10, row, "Left", m.getCustomLeft(), 0, 16, mouseX, mouseY);
            row = drawSlider(context, px + 10, row, "Right", m.getCustomRight(), 0, 16, mouseX, mouseY);
            drawToggle(context, px + 10, row, "Center dot", m.isCustomDot());
        }

        RenderUtil.drawText(context, textRenderer, "Click values to adjust  ·  ESC back", 14, height - 14, MUTED, false);
        super.render(context, mouseX, mouseY, delta);
    }

    private int drawSlider(DrawContext context, int x, int y, String label, int value, int min, int max, int mx, int my) {
        RenderUtil.drawText(context, textRenderer, label, x, y, DIM, false);
        RenderUtil.drawText(context, textRenderer, String.valueOf(value), x + 70, y, 0xFF748FFF, false);
        RenderUtil.drawText(context, textRenderer, "[-]", x + 100, y, TEXT, false);
        RenderUtil.drawText(context, textRenderer, "[+]", x + 120, y, TEXT, false);
        return y + 14;
    }

    private int drawToggle(DrawContext context, int x, int y, String label, boolean on) {
        RenderUtil.drawText(context, textRenderer, label, x, y, DIM, false);
        RenderUtil.drawText(context, textRenderer, on ? "ON" : "OFF", x + 110, y, on ? 0xFF40C057 : 0xFFFA5252, false);
        return y + 14;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        CustomCrosshairModule m = mod();
        if (m == null) return super.mouseClicked(mouseX, mouseY, button);

        // Styles
        CustomCrosshairModule.Style[] styles = CustomCrosshairModule.Style.values();
        String[] labels = {"Cross", "Dot", "Circle", "Cross+Dot", "Gap", "Custom"};
        int sx = 16;
        for (int i = 0; i < styles.length; i++) {
            int tw = textRenderer.getWidth(labels[i]) + 14;
            if (mouseX >= sx && mouseX <= sx + tw && mouseY >= 58 && mouseY <= 74) {
                m.setStyle(styles[i]);
                return true;
            }
            sx += tw + 6;
        }

        int px = 130;
        int row = 100;

        // Size
        if (hitAdjust(mouseX, mouseY, px, row, m.getSize(), 1, 20, m::setSize)) return true;
        row += 14;
        if (hitAdjust(mouseX, mouseY, px, row, m.getThickness(), 1, 6, m::setThickness)) return true;
        row += 14;
        if (hitAdjust(mouseX, mouseY, px, row, m.getGap(), 0, 12, m::setGap)) return true;
        row += 14;
        if (hitAdjust(mouseX, mouseY, px, row, m.getOpacity(), 40, 255, m::setOpacity)) return true;
        row += 14;

        // Colors
        int cx = 170;
        for (int c : PRESET_COLORS) {
            if (mouseX >= cx && mouseX <= cx + 12 && mouseY >= row - 1 && mouseY <= row + 11) {
                m.setColor(c);
                return true;
            }
            cx += 16;
        }
        row += 18;

        if (hitToggle(mouseX, mouseY, px, row)) { m.setOutline(!m.isOutline()); return true; }
        row += 14;
        if (hitToggle(mouseX, mouseY, px, row)) { m.setReplaceVanilla(!m.isReplaceVanilla()); return true; }
        row += 14;
        if (hitToggle(mouseX, mouseY, px, row)) { m.setEnabled(!m.isEnabled()); return true; }
        row += 18;

        if (m.getStyle() == CustomCrosshairModule.Style.CUSTOM) {
            row += 12;
            if (hitAdjust(mouseX, mouseY, px, row, m.getCustomTop(), 0, 16, m::setCustomTop)) return true;
            row += 14;
            if (hitAdjust(mouseX, mouseY, px, row, m.getCustomBottom(), 0, 16, m::setCustomBottom)) return true;
            row += 14;
            if (hitAdjust(mouseX, mouseY, px, row, m.getCustomLeft(), 0, 16, m::setCustomLeft)) return true;
            row += 14;
            if (hitAdjust(mouseX, mouseY, px, row, m.getCustomRight(), 0, 16, m::setCustomRight)) return true;
            row += 14;
            if (hitToggle(mouseX, mouseY, px, row)) { m.setCustomDot(!m.isCustomDot()); return true; }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean hitAdjust(double mx, double my, int px, int row, int val, int min, int max, java.util.function.IntConsumer set) {
        if (my < row || my > row + 12) return false;
        if (mx >= px + 100 && mx <= px + 115) { set.accept(Math.max(min, val - 1)); return true; }
        if (mx >= px + 120 && mx <= px + 135) { set.accept(Math.min(max, val + 1)); return true; }
        return false;
    }

    private boolean hitToggle(double mx, double my, int px, int row) {
        return my >= row && my <= row + 12 && mx >= px + 100 && mx <= px + 140;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            client.setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
