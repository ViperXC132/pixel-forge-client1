package com.pixelforge.gui;

import com.pixelforge.gui.screens.AccountsScreen;
import com.pixelforge.gui.screens.ModsScreen;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;

/**
 * Home screen — clean 2-column layout (Singleplayer | Multiplayer),
 * plus Accounts / Mods / Options / Quit. The Host World entry is additive.
 */
public class TitleScreenOverride extends Screen {
    public TitleScreenOverride() {
        super(Text.literal("PixelForge"));
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        RenderUtil.fill(c, 0, 0, width, height, 0xFF0A0A0A);
        RenderUtil.fillGradient(c, 0, 0, width, height, 0xFF0A0A0A, 0xFF141414);

        RenderUtil.drawCenteredText(c, textRenderer, "PIXELFORGE", width / 2, height / 2 - 100, 0xFFFFFFFF, false);
        RenderUtil.drawCenteredText(c, textRenderer, "1.21.11", width / 2, height / 2 - 86, 0xFF808080, false);

        int bw = 140, bh = 28, gap = 8;
        int mid = width / 2;
        int y = height / 2 - 50;

        // Existing rows — positions and behavior preserved.
        btn(c, mid - bw - gap / 2, y, bw, bh, "Singleplayer", mx, my);
        btn(c, mid + gap / 2, y, bw, bh, "Multiplayer", mx, my);
        y += bh + gap;

        int fullW = bw * 2 + gap;
        int x = mid - fullW / 2;
        btn(c, x, y, fullW, bh, "Accounts", mx, my); y += bh + gap;
        btn(c, x, y, fullW, bh, "Mods", mx, my); y += bh + gap;
        btn(c, x, y, fullW, bh, "Options", mx, my); y += bh + gap;
        btn(c, x, y, fullW, bh, "Quit Game", mx, my); y += bh + gap;

        // Additive host entry; nothing above it is moved or repurposed.
        btn(c, x, y, fullW, bh, "Host World", mx, my);

        super.render(c, mx, my, d);
    }

    private void btn(DrawContext c, int x, int y, int bw, int bh, String label, int mx, int my) {
        boolean hover = mx >= x && mx <= x + bw && my >= y && my <= y + bh;
        RenderUtil.fill(c, x, y, x + bw, y + bh, hover ? 0x40FFFFFF : 0x18FFFFFF);
        RenderUtil.drawBorder(c, x, y, bw, bh, hover ? 0xCCFFFFFF : 0x40FFFFFF);
        RenderUtil.drawCenteredText(c, textRenderer, label, x + bw / 2, y + 10, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        int bw = 140, bh = 28, gap = 8;
        int mid = width / 2;
        int y = height / 2 - 50;
        double mx = click.x(), my = click.y();

        // Existing interactions — unchanged.
        if (hit(mx, my, mid - bw - gap / 2, y, bw, bh)) {
            client.setScreen(new SelectWorldScreen(this)); return true;
        }
        if (hit(mx, my, mid + gap / 2, y, bw, bh)) {
            client.setScreen(new MultiplayerScreen(this)); return true;
        }
        y += bh + gap;
        int fullW = bw * 2 + gap;
        int x = mid - fullW / 2;
        if (hit(mx, my, x, y, fullW, bh)) { client.setScreen(new AccountsScreen(this)); return true; } y += bh + gap;
        if (hit(mx, my, x, y, fullW, bh)) { client.setScreen(new ModsScreen(this)); return true; } y += bh + gap;
        if (hit(mx, my, x, y, fullW, bh)) { client.setScreen(new OptionsScreen(this, client.options)); return true; } y += bh + gap;
        if (hit(mx, my, x, y, fullW, bh)) { client.scheduleStop(); return true; } y += bh + gap;

        // Additive host entry.
        if (hit(mx, my, x, y, fullW, bh)) { client.setScreen(new HostWorldScreen(this)); return true; }
        return super.mouseClicked(click, doubled);
    }

    private boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean shouldPause() { return false; }
}
