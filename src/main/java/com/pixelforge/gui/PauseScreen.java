package com.pixelforge.gui;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.gui.screens.AccountsScreen;
import com.pixelforge.gui.screens.CrosshairScreen;
import com.pixelforge.gui.screens.ModsScreen;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.text.Text;

import java.nio.file.Path;

/** Lunar pause menu — all buttons inside the panel. */
public class PauseScreen extends Screen {
    private final Screen parent;
    private int left, top, w, h;

    public PauseScreen(Screen parent) {
        super(Text.literal("Game Menu"));
        this.parent = parent;
    }

    private void layout() {
        w = 300;
        h = 340;
        left = (width - w) / 2;
        top = (height - h) / 2;
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        layout();
        RenderUtil.fill(c, 0, 0, width, height, 0x66000000);
        RenderUtil.drawRoundedPanel(c, left, top, w, h, 0xD0101010, 0x55FFFFFF);
        RenderUtil.drawCenteredText(c, textRenderer, "Game Menu", left + w / 2, top + 14, 0xFFFFFFFF, false);

        int bw = 240, bh = 24, gap = 6;
        int x = left + (w - bw) / 2;
        int y = top + 36;
        String[] labels = {
                "Back to Game", "Mods", "Accounts", "Crosshair",
                "HUD Editor", "Options", "Resource Packs", "ClickGUI", "Disconnect", "Host World"
        };
        for (String label : labels) {
            button(c, x, y, bw, bh, label, mx, my);
            y += bh + gap;
        }
        super.render(c, mx, my, d);
    }

    private void button(DrawContext c, int x, int y, int bw, int bh, String label, int mx, int my) {
        boolean hover = mx >= x && mx <= x + bw && my >= y && my <= y + bh;
        RenderUtil.fill(c, x, y, x + bw, y + bh, hover ? 0x40FFFFFF : 0x20FFFFFF);
        RenderUtil.drawBorder(c, x, y, bw, bh, hover ? 0xAAFFFFFF : 0x40FFFFFF);
        RenderUtil.drawCenteredText(c, textRenderer, label, x + bw / 2, y + 8, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        layout();
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        int bw = 240, bh = 24, gap = 6;
        int x = left + (w - bw) / 2;
        int y = top + 36;
        if (hit(click, x, y, bw, bh)) { client.setScreen(parent); return true; } y += bh + gap;
        if (hit(click, x, y, bw, bh)) { client.setScreen(new ModsScreen(this)); return true; } y += bh + gap;
        if (hit(click, x, y, bw, bh)) { client.setScreen(new AccountsScreen(this)); return true; } y += bh + gap;
        if (hit(click, x, y, bw, bh)) { client.setScreen(new CrosshairScreen(this)); return true; } y += bh + gap;
        if (hit(click, x, y, bw, bh)) { client.setScreen(new HudEditor()); return true; } y += bh + gap;
        if (hit(click, x, y, bw, bh)) { client.setScreen(new OptionsScreen(this, client.options)); return true; } y += bh + gap;
        if (hit(click, x, y, bw, bh)) { openResourcePacks(); return true; } y += bh + gap;
        if (hit(click, x, y, bw, bh)) { client.setScreen(new ClickGui()); return true; } y += bh + gap;
        if (hit(click, x, y, bw, bh)) { client.disconnect(Text.literal("Disconnected")); return true; } y += bh + gap;
        if (hit(click, x, y, bw, bh)) { client.setScreen(new HostWorldScreen(this)); return true; }
        return super.mouseClicked(click, doubled);
    }

    private boolean hit(net.minecraft.client.gui.Click c, int x, int y, int w, int h) {
        return c.x() >= x && c.x() <= x + w && c.y() >= y && c.y() <= y + h;
    }

    private void openResourcePacks() {
        try {
            Path dir = client.getResourcePackDir();
            client.setScreen(new PackScreen(
                    client.getResourcePackManager(),
                    manager -> {
                        client.options.refreshResourcePacks(manager);
                        client.setScreen(PauseScreen.this);
                    },
                    dir,
                    Text.literal("Resource Packs")
            ));
        } catch (Throwable t) {
            PixelForgeClient.LOGGER.warn("PackScreen open failed", t);
        }
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput i) {
        if (i.key() == 256) { client.setScreen(parent); return true; }
        return super.keyPressed(i);
    }

    @Override
    public boolean shouldPause() { return true; }
}
