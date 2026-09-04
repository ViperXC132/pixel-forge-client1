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

/** Clean two-column pause menu with the client tools grouped logically. */
public final class PauseScreen extends Screen {
    private final Screen parent;
    private int left, top;

    public PauseScreen(Screen parent) { super(Text.literal("Game Menu")); this.parent = parent; }

    private void layout() { left = (width - 520) / 2; top = (height - 340) / 2; }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        layout();
        RenderUtil.fill(c, 0, 0, width, height, 0x78000000);
        RenderUtil.drawRoundedPanel(c, left, top, 520, 340, 0xE0101010, 0x60FFFFFF);
        RenderUtil.drawText(c, textRenderer, "GAME MENU", left + 22, top + 20, 0xFFFFFFFF, false);
        RenderUtil.drawText(c, textRenderer, "PixelForge", left + 22, top + 35, 0xFF777777, false);

        String[] labels = {"Back to Game", "Host World", "Mods", "Accounts", "Crosshair", "HUD Editor", "Options", "Resource Packs", "ClickGUI", "Disconnect"};
        int bw = 232, bh = 30, gap = 8;
        for (int i = 0; i < labels.length; i++) {
            int col = i % 2, row = i / 2;
            int x = left + 22 + col * (bw + 12), y = top + 60 + row * (bh + gap);
            button(c, x, y, bw, bh, labels[i], mx, my);
        }
        RenderUtil.drawText(c, textRenderer, "ESC  •  Back", left + 22, top + 320, 0xFF666666, false);
        super.render(c, mx, my, d);
    }

    private void button(DrawContext c, int x, int y, int w, int h, String label, int mx, int my) {
        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + h;
        RenderUtil.fill(c, x, y, x + w, y + h, hover ? 0x45FFFFFF : 0x20FFFFFF);
        RenderUtil.drawBorder(c, x, y, w, h, hover ? 0xAAFFFFFF : 0x45FFFFFF);
        RenderUtil.drawCenteredText(c, textRenderer, label, x + w / 2, y + 10, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        layout();
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        String[] labels = {"Back to Game", "Host World", "Mods", "Accounts", "Crosshair", "HUD Editor", "Options", "Resource Packs", "ClickGUI", "Disconnect"};
        int bw = 232, bh = 30, gap = 8;
        for (int i = 0; i < labels.length; i++) {
            int col = i % 2, row = i / 2;
            int x = left + 22 + col * (bw + 12), y = top + 60 + row * (bh + gap);
            if (!hit(click, x, y, bw, bh)) continue;
            switch (i) {
                case 0 -> client.setScreen(parent);
                case 1 -> client.setScreen(new HostWorldScreen(this));
                case 2 -> client.setScreen(new ModsScreen(this));
                case 3 -> client.setScreen(new AccountsScreen(this));
                case 4 -> client.setScreen(new CrosshairScreen(this));
                case 5 -> client.setScreen(new HudEditor());
                case 6 -> client.setScreen(new OptionsScreen(this, client.options));
                case 7 -> openResourcePacks();
                case 8 -> client.setScreen(new ClickGui());
                case 9 -> client.disconnect(Text.literal("Disconnected"));
            }
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    private boolean hit(net.minecraft.client.gui.Click c, int x, int y, int w, int h) { return c.x() >= x && c.x() <= x + w && c.y() >= y && c.y() <= y + h; }

    private void openResourcePacks() {
        try {
            Path dir = client.getResourcePackDir();
            client.setScreen(new PackScreen(client.getResourcePackManager(), manager -> { client.options.refreshResourcePacks(manager); client.setScreen(PauseScreen.this); }, dir, Text.literal("Resource Packs")));
        } catch (Throwable t) { PixelForgeClient.LOGGER.warn("PackScreen open failed", t); }
    }

    @Override public boolean keyPressed(net.minecraft.client.input.KeyInput i) { if (i.key() == 256) { client.setScreen(parent); return true; } return super.keyPressed(i); }
    @Override public boolean shouldPause() { return true; }
}
