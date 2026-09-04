package com.pixelforge.gui;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.config.ConfigManager;
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
 * Drag-and-drop HUD editor.
 * Left-click + drag moves. Right-click toggles. Positions persist via HudRenderer.
 */
public class HudEditor extends Screen {
    private Module dragging;
    private int dragOffsetX, dragOffsetY;
    private final List<Module> hudModules;
    private static final int ACCENT = 0xFF3B5BDB, TEXT = 0xFFE8ECFF, DIM = 0xFF7F8AA4, GREEN = 0xFF55E58A, RED = 0xFFFF6666;

    public HudEditor() {
        super(Text.literal("PixelForge HUD Editor"));
        hudModules = PixelForgeClient.getInstance().getModuleManager().getModules().stream()
                .filter(m -> m.getCategory() == Category.HUD || m.getCategory() == Category.TRAINER)
                .toList();
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        RenderUtil.fill(c, 0, 0, width, height, 0xB0080B12);
        RenderUtil.fill(c, 0, 0, width, 34, 0xF00A0E18);
        RenderUtil.drawText(c, textRenderer, "HUD EDITOR", 14, 11, TEXT, false);
        RenderUtil.drawText(c, textRenderer, "LMB drag to move · RMB toggle · ESC close", 130, 11, DIM, false);

        for (Module m : hudModules) {
            int x = HudRenderer.getX(m.getName());
            int y = HudRenderer.getY(m.getName());
            int w = boxWidth(m, x);
            boolean active = m.isEnabled();
            boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + 22;

            RenderUtil.fill(c, x, y, x + w, y + 22, active ? 0x503B5BDB : 0x30101828);
            RenderUtil.drawBorder(c, x, y, w, 22, hover ? 0xFF748FFF : 0xFF293454);
            RenderUtil.drawText(c, textRenderer, m.getName(), x + 7, y + 7, active ? TEXT : DIM, false);
            RenderUtil.drawText(c, textRenderer, active ? "ON" : "OFF", x + w - 30, y + 7, active ? GREEN : RED, false);
        }

        RenderUtil.fill(c, 12, height - 34, 112, height - 14, 0x203B5BDB);
        RenderUtil.drawBorder(c, 12, height - 34, 100, 20, ACCENT);
        RenderUtil.drawText(c, textRenderer, "Reset layout", 28, height - 28, 0xFF748FFF, false);
        RenderUtil.drawText(c, textRenderer, "Positions save automatically", 12, height - 12, DIM, false);
        super.render(c, mx, my, d);
    }

    private int boxWidth(Module m, int x) {
        int w = Math.min(260, width - x - 8);
        return Math.max(120, w);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mx = (int) click.x();
        int my = (int) click.y();
        int button = click.button();

        if (button == 0 && mx >= 12 && mx <= 112 && my >= height - 34 && my <= height - 14) {
            HudRenderer.resetLayout();
            HudRenderer.savePositions();
            return true;
        }

        for (Module m : hudModules) {
            int x = HudRenderer.getX(m.getName());
            int y = HudRenderer.getY(m.getName());
            int w = boxWidth(m, x);
            if (mx >= x && mx <= x + w && my >= y && my <= y + 22) {
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
            x = Math.max(2, Math.min(width - 122, x));
            y = Math.max(38, Math.min(height - 30, y));
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
