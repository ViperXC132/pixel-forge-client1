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

/** Clean HUD editor — no top/bottom bars. Drag boxes, RMB toggle. */
public class HudEditor extends Screen {
    private Module dragging;
    private int dragOffsetX, dragOffsetY;
    private final List<Module> hudModules;

    private static final int BOX_H = 16;

    public HudEditor() {
        super(Text.literal("HUD Editor"));
        hudModules = PixelForgeClient.getInstance().getModuleManager().getModules().stream()
                .filter(m -> m.getCategory() == Category.HUD || m.getCategory() == Category.TRAINER)
                .toList();
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        RenderUtil.fill(c, 0, 0, width, height, 0x55000000);
        RenderUtil.drawCenteredText(c, textRenderer, "Drag to move  ·  Right-click toggle  ·  ESC done", width / 2, 8, RenderUtil.DIM, false);

        for (Module m : hudModules) {
            int x = HudRenderer.getX(m.getName());
            int y = HudRenderer.getY(m.getName());
            int w = boxWidth(m);
            boolean on = m.isEnabled();
            boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + BOX_H;
            boolean isDrag = dragging == m;

            int bg = isDrag ? 0x70FFFFFF : (on ? 0x50000000 : 0x30000000);
            int border = hover || isDrag ? 0xAAFFFFFF : 0x40FFFFFF;

            RenderUtil.fill(c, x, y, x + w, y + BOX_H, bg);
            RenderUtil.drawBorder(c, x, y, w, BOX_H, border);
            RenderUtil.drawText(c, textRenderer, m.getName(), x + 5, y + 4, on ? RenderUtil.TEXT : RenderUtil.DIM, false);
            RenderUtil.fill(c, x + w - 9, y + 5, x + w - 4, y + 11, on ? RenderUtil.GREEN : RenderUtil.RED);
        }

        boolean resetHover = mx >= 8 && mx <= 80 && my >= height - 20 && my <= height - 6;
        RenderUtil.fill(c, 8, height - 20, 80, height - 6, resetHover ? 0x40FFFFFF : 0x20000000);
        RenderUtil.drawBorder(c, 8, height - 20, 72, 14, 0x55FFFFFF);
        RenderUtil.drawText(c, textRenderer, "Reset", 28, height - 17, RenderUtil.TEXT, false);

        super.render(c, mx, my, d);
    }

    private int boxWidth(Module m) {
        return Math.max(64, Math.min(140, textRenderer.getWidth(m.getName()) + 20));
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mx = (int) click.x(), my = (int) click.y(), button = click.button();
        if (button == 0 && mx >= 8 && mx <= 80 && my >= height - 20 && my <= height - 6) {
            HudRenderer.resetLayout();
            HudRenderer.savePositions();
            return true;
        }
        for (Module m : hudModules) {
            int x = HudRenderer.getX(m.getName());
            int y = HudRenderer.getY(m.getName());
            int w = boxWidth(m);
            if (mx >= x && mx <= x + w && my >= y && my <= y + BOX_H) {
                if (button == 1) { m.toggle(); return true; }
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
    public boolean mouseDragged(Click click, double ox, double oy) {
        if (dragging != null && click.button() == 0) {
            int x = Math.max(2, Math.min(width - boxWidth(dragging) - 2, (int) click.x() - dragOffsetX));
            int y = Math.max(16, Math.min(height - BOX_H - 22, (int) click.y() - dragOffsetY));
            HudRenderer.setPosition(dragging.getName(), x, y);
            return true;
        }
        return super.mouseDragged(click, ox, oy);
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
        if (i.key() == 256) { HudRenderer.savePositions(); close(); return true; }
        return super.keyPressed(i);
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        HudRenderer.savePositions();
        if (client != null) client.setScreen(new ClickGui());
    }
}
