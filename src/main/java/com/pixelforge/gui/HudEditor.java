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

/** Clean HUD editor. Only real HUD modules are editable here; trainer modules stay out. */
public final class HudEditor extends Screen {
    private Module dragging;
    private int dragOffsetX, dragOffsetY;
    private final List<Module> hudModules;
    private static final int BOX_H = 18;

    public HudEditor() {
        super(Text.literal("HUD Editor"));
        hudModules = PixelForgeClient.getInstance().getModuleManager().getModules().stream()
                .filter(m -> m.getCategory() == Category.HUD)
                .toList();
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        RenderUtil.fill(c, 0, 0, width, height, 0x72000000);
        RenderUtil.drawRoundedPanel(c, 8, 8, width - 16, height - 16, 0xC8101010, 0x45FFFFFF);
        RenderUtil.drawText(c, textRenderer, "HUD EDITOR", 18, 17, 0xFFFFFFFF, false);
        RenderUtil.drawText(c, textRenderer, "Drag to position  •  Right click to toggle  •  ESC to save", 18, 31, 0xFFB8B8B8, false);

        for (Module m : hudModules) {
            int x = HudRenderer.getX(m.getName());
            int y = HudRenderer.getY(m.getName());
            int w = boxWidth(m);
            boolean on = m.isEnabled();
            boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + BOX_H;
            boolean selected = dragging == m;
            int bg = selected ? 0x55FFFFFF : (hover ? 0x35FFFFFF : 0x22000000);
            RenderUtil.fill(c, x, y, x + w, y + BOX_H, bg);
            RenderUtil.drawBorder(c, x, y, w, BOX_H, hover || selected ? 0xCCFFFFFF : 0x55FFFFFF);
            RenderUtil.drawText(c, textRenderer, m.getName(), x + 6, y + 5, 0xFFFFFFFF, false);
            RenderUtil.fill(c, x + w - 9, y + 6, x + w - 4, y + 12, on ? 0xFFFFFFFF : 0xFF777777);
        }

        int resetY = height - 28;
        boolean resetHover = mx >= 18 && mx <= 84 && my >= resetY && my <= resetY + 18;
        RenderUtil.fill(c, 18, resetY, 84, resetY + 18, resetHover ? 0x45FFFFFF : 0x22FFFFFF);
        RenderUtil.drawBorder(c, 18, resetY, 66, 18, 0x55FFFFFF);
        RenderUtil.drawText(c, textRenderer, "Reset", 36, resetY + 5, 0xFFFFFFFF, false);
        super.render(c, mx, my, d);
    }

    private int boxWidth(Module m) { return Math.max(72, Math.min(160, textRenderer.getWidth(m.getName()) + 24)); }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mx = (int) click.x(), my = (int) click.y(), button = click.button();
        if (button == 0 && mx >= 18 && mx <= 84 && my >= height - 28 && my <= height - 10) {
            HudRenderer.resetLayout();
            HudRenderer.savePositions();
            return true;
        }
        for (Module m : hudModules) {
            int x = HudRenderer.getX(m.getName()), y = HudRenderer.getY(m.getName()), w = boxWidth(m);
            if (mx >= x && mx <= x + w && my >= y && my <= y + BOX_H) {
                if (button == 1) { m.toggle(); return true; }
                if (button == 0) { dragging = m; dragOffsetX = mx - x; dragOffsetY = my - y; return true; }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double ox, double oy) {
        if (dragging != null && click.button() == 0) {
            int x = Math.max(2, Math.min(width - boxWidth(dragging) - 2, (int) click.x() - dragOffsetX));
            int y = Math.max(46, Math.min(height - BOX_H - 2, (int) click.y() - dragOffsetY));
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

    @Override public boolean shouldPause() { return false; }

    @Override
    public void close() {
        HudRenderer.savePositions();
        if (client != null) client.setScreen(new ClickGui());
    }
}
