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

/** Lunar-style title screen — transparent glass, white text. */
public class TitleScreenOverride extends Screen {
    public TitleScreenOverride() {
        super(Text.literal("PixelForge"));
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        RenderUtil.fill(c, 0, 0, width, height, 0xFF0A0A0A);
        RenderUtil.fillGradient(c, 0, 0, width, height, 0xFF0A0A0A, 0xFF151515);

        RenderUtil.drawCenteredText(c, textRenderer, "PIXELFORGE", width / 2, height / 2 - 90, 0xFFFFFFFF, false);
        RenderUtil.drawCenteredText(c, textRenderer, "1.21.11", width / 2, height / 2 - 76, 0xFF808080, false);

        int bw = 200, bh = 28, gap = 8;
        int x = width / 2 - bw / 2;
        int y = height / 2 - 40;
        String[] labels = {"Singleplayer", "Multiplayer", "Accounts", "Mods", "Options", "Quit"};
        for (String label : labels) {
            boolean hover = mx >= x && mx <= x + bw && my >= y && my <= y + bh;
            RenderUtil.fill(c, x, y, x + bw, y + bh, hover ? 0x35FFFFFF : 0x18FFFFFF);
            RenderUtil.drawBorder(c, x, y, bw, bh, hover ? 0xAAFFFFFF : 0x40FFFFFF);
            RenderUtil.drawCenteredText(c, textRenderer, label, x + bw / 2, y + 10, 0xFFFFFFFF, false);
            y += bh + gap;
        }
        super.render(c, mx, my, d);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        int bw = 200, bh = 28, gap = 8;
        int x = width / 2 - bw / 2;
        int y = height / 2 - 40;
        double mx = click.x(), my = click.y();
        if (hit(mx, my, x, y, bw, bh)) { client.setScreen(new SelectWorldScreen(this)); return true; } y += bh + gap;
        if (hit(mx, my, x, y, bw, bh)) { client.setScreen(new MultiplayerScreen(this)); return true; } y += bh + gap;
        if (hit(mx, my, x, y, bw, bh)) { client.setScreen(new AccountsScreen(this)); return true; } y += bh + gap;
        if (hit(mx, my, x, y, bw, bh)) { client.setScreen(new ModsScreen(this)); return true; } y += bh + gap;
        if (hit(mx, my, x, y, bw, bh)) { client.setScreen(new OptionsScreen(this, client.options)); return true; } y += bh + gap;
        if (hit(mx, my, x, y, bw, bh)) { client.scheduleStop(); return true; }
        return super.mouseClicked(click, doubled);
    }

    private boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public boolean shouldPause() { return false; }
}
