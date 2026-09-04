package com.pixelforge.gui;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Lunar-style HUD editor.
 * Compact moveable boxes — drag to reposition, right-click to toggle.
 */
public class HudEditor extends Screen {
    private Module dragging;
    private int dragOffsetX, dragOffsetY;
    private final List<Module> hudModules;

    private static final int BOX_H = 18;
    private static final int ACCENT = 0xFF5B6CFF;
    private static final int TEXT = 0xFFEAF0FF;
    private static final int DIM = 0xFF8B95B0;
    private static final int GREEN = 0xFF4ADE80;
    private static final int RED = 0xFFF87171;

    public HudEditor() {
        super(Text.literal("HUD Editor"));
        hudModules = PixelForgeClient.getInstance().getModuleManager().getModules().stream()
                .filter(m -> m.getCategory() == Category.HUD || m.getCategory() == Category.TRAINER)
                .toList();
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        RenderUtil.fill(c, 0, 0, width, height, 0xA0080B12);

        RenderUtil.fill(c, 0, 0, width, 28, 0xF00A0E18);
        RenderUtil.drawText(c, textRenderer, "HUD EDITOR", 12, 10, TEXT, false);
        RenderUtil.drawText(c, textRenderer, "Drag boxes · Right-click toggle · ESC done", 100, 10, DIM, false);

        for (Module m : hudModules) {
            int x = HudRenderer.getX(m.getName());
            int y = HudRenderer.getY(m.getName());
            int w = boxWidth(m);
            boolean on = m.isEnabled();
            boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + BOX_H;
            boolean isDrag = dragging == m;

            int bg = isDrag ? 0xA03B5BDB : (on ? 0x900E1424 : 0x70080C14);
            int border = isDrag ? 0xFF8B9CFF : (hover ? ACCENT : (on ? 0xFF2A3350 : 0xFF1A2030));

            RenderUtil.fill(c, x, y, x + w, y + BOX_H, bg);
            RenderUtil.drawBorder(c, x, y, w, BOX_H, border);

            if (on) {
                RenderUtil.fill(c, x, y, x + 2, y + BOX_H, ACCENT);
            }

            String label = m.getName();
            int maxText = w - 28;
            while (textRenderer.getWidth(label) > maxText && label.length() > 3) {
                label = label.substring(0, label.length() - 1);
            }
            if (!label.equals(m.getName()) && label.length() > 3) {
                label = label.substring(0, label.length() - 1) + "\u2026";
            }

            RenderUtil.drawText(c, textRenderer, label, x + 6, y + 5, on ? TEXT : DIM, false);

            int dot = on ? GREEN : RED;
            RenderUtil.fill(c, x + w - 10, y + 6, x + w - 4, y + 12, dot);
        }

        RenderUtil.fill(c, 0, height - 28, width, height, 0xF00A0E18);
        boolean resetHover = mx >= 12 && mx <= 100 && my >= height - 22 && my <= height - 8;
        RenderUtil.fill(c, 12, height - 22, 100, height - 8, resetHover ? 0x403B5BDB : 0x20101824);
        RenderUtil.drawBorder(c, 12, height - 22, 88, 14, ACCENT);
        RenderUtil.drawText(c, textRenderer, "Reset layout", 22, height - 19, ACCENT, false);
        RenderUtil.drawText(c, textRenderer, "Positions auto-save", 120, height - 19, DIM, false);

        super.render(c, mx, my, d);
    }

    private int boxWidth(Module m) {
        int tw = textRenderer.getWidth(m.getName()) + 24;
        return Math.max(72, Math.min(160, tw));
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mx = (int) click.x();
        int my = (int) click.y();
        int button = click.button();

        if (button == 0 && mx >= 12 && mx <= 100 && my >= height - 22 && my <= height - 8) {
            HudRenderer.resetLayout();
            HudRenderer.savePositions();
            return true;
        }

        for (Module m : hudModules) {
            int x = HudRenderer.getX(m.getName());
            int y = HudRenderer.getY(m.getName());
            int w = boxWidth(m);
            if (mx >= x && mx <= x + w && my >= y && my <= y + BOX_H) {
                if (button == 1) {
                    m.toggle();
                    return true;
                }
                if (button == 0) {
                    dragging = m;
                    dragOffsetX = mx - x;
                    dragOffsetY = my - y;
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging != null && click.button() == 0) {
            int x = (int) click.x() - dragOffsetX;
            int y = (int) click.y() - dragOffsetY;
            x = Math.max(2, Math.min(width - boxWidth(dragging) - 2, x));
            y = Math.max(30, Math.min(height - BOX_H - 30, y));
            HudRenderer.setPosition(dragging.getName(), x, y);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0 && dragging != null) {
            HudRenderer.savePositions();
            dragging = null;
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput i) {
        if (i.key() == 256) {
            HudRenderer.savePositions();
            close();
            return true;
        }
        return super.keyPressed(i);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        HudRenderer.savePositions();
        if (client != null) client.setScreen(new ClickGui());
    }
}
