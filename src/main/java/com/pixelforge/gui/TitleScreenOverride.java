package com.pixelforge.gui;

import com.pixelforge.account.AccountManager;
import com.pixelforge.account.AccountManager.Account;
import com.pixelforge.account.SkinHelper;
import com.pixelforge.gui.screens.AccountsScreen;
import com.pixelforge.gui.screens.CrosshairScreen;
import com.pixelforge.gui.screens.ModsScreen;
import com.pixelforge.util.ColorUtil;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Lunar-style main menu: opaque-transparent panels, star field, no center crosshair.
 * VulkanMod safe.
 */
public class TitleScreenOverride extends Screen {

    private final List<Star> stars = new ArrayList<>();
    private final Random random = new Random();

    private static final int BG           = 0xFF0E1117;
    private static final int NAV_BG       = 0xE00A0C14;
    private static final int ACCENT       = 0xFF3B5BDB;
    private static final int TEXT         = 0xFFC8D0E0;
    private static final int TEXT_DIM     = 0xFF8892A8;
    private static final int TEXT_MUTED   = 0xFF3D4A6A;
    private static final int PANEL_BG     = 0xD0101424; // opaque transparent
    private static final int PANEL_BORDER = 0xFF1E2540;

    public TitleScreenOverride() {
        super(Text.literal("PixelForge"));
        for (int i = 0; i < 80; i++) {
            stars.add(new Star(
                    random.nextFloat() * 900,
                    random.nextFloat() * 500,
                    random.nextFloat() * 0.55f + 0.15f,
                    random.nextFloat() * 0.45f + 0.2f
            ));
        }
    }

    @Override
    protected void init() {
        int leftX = 28;
        int startY = height / 2 - 90;
        int bw = 150;
        int bh = 22;

        addDrawableChild(ButtonWidget.builder(Text.literal("Singleplayer"), b ->
                client.setScreen(new SelectWorldScreen(this)))
                .dimensions(leftX, startY, bw, bh).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Multiplayer"), b ->
                client.setScreen(new MultiplayerScreen(this)))
                .dimensions(leftX, startY + 28, bw, bh).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Mod Menu"), b ->
                client.setScreen(new ModsScreen(this)))
                .dimensions(leftX, startY + 64, bw, bh).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Options"), b ->
                client.setScreen(new OptionsScreen(this, client.options)))
                .dimensions(leftX, startY + 92, bw, bh).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Quit"), b ->
                client.scheduleStop())
                .dimensions(leftX, startY + 120, bw, bh).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderUtil.fill(context, 0, 0, width, height, BG);

        for (int gx = 0; gx < width; gx += 40)
            RenderUtil.fill(context, gx, 0, gx + 1, height, 0x05FFFFFF);
        for (int gy = 0; gy < height; gy += 40)
            RenderUtil.fill(context, 0, gy, width, gy + 1, 0x05FFFFFF);

        for (Star s : stars) {
            s.y += s.speed * 0.22f;
            if (s.y > height) {
                s.y = 0;
                s.x = random.nextFloat() * Math.max(1, width);
            }
            int a = (int) (s.alpha * 220);
            RenderUtil.fill(context, (int) s.x, (int) s.y, (int) s.x + 1, (int) s.y + 1,
                    ColorUtil.rgba(200, 210, 255, Math.min(255, a)));
        }

        // Nav
        RenderUtil.fill(context, 0, 0, width, 30, NAV_BG);
        RenderUtil.fill(context, 0, 29, width, 30, 0x12FFFFFF);

        RenderUtil.fill(context, 14, 6, 30, 22, ACCENT);
        RenderUtil.fill(context, 18, 10, 26, 18, 0xFFFFFFFF);
        RenderUtil.drawText(context, textRenderer, "PixelForge", 36, 10, TEXT, false);
        RenderUtil.drawText(context, textRenderer, "1.21.11", 100, 11, ACCENT, false);

        drawNav(context, "Home", 200, true);
        drawNav(context, "Mods", 250, false);
        drawNav(context, "Crosshair", 300, false);
        drawNav(context, "Accounts", 370, false);

        // Center — splash only, no crosshair
        RenderUtil.drawCenteredText(context, textRenderer, "No hacks. Just vibes.",
                width / 2, height / 2 - 8, TEXT_MUTED, false);

        // Right accounts panel
        int px = width - 220;
        int py = 48;
        drawPanel(context, px, py, 200, 140);
        RenderUtil.drawText(context, textRenderer, "ACCOUNTS", px + 10, py + 8, ACCENT, false);
        RenderUtil.drawText(context, textRenderer, "manage", px + 148, py + 8, TEXT_MUTED, false);

        int ay = py + 26;
        int shown = 0;
        for (Account acc : AccountManager.getAccounts()) {
            if (shown >= 3) break;
            SkinHelper.drawHead(context, acc.username, px + 10, ay, 18);
            RenderUtil.drawText(context, textRenderer, acc.username, px + 34, ay + 1, TEXT, false);
            RenderUtil.drawText(context, textRenderer, acc.type.displayName, px + 34, ay + 10, TEXT_MUTED, false);
            if (acc.active) {
                RenderUtil.fill(context, px + 180, ay + 6, px + 186, ay + 12, 0xFF40C057);
            }
            ay += 26;
            shown++;
        }
        RenderUtil.drawText(context, textRenderer, "+ Add account", px + 12, py + 120, TEXT_MUTED, false);

        // Quick connect
        int qy = py + 150;
        drawPanel(context, px, qy, 200, 100);
        RenderUtil.drawText(context, textRenderer, "QUICK CONNECT", px + 10, qy + 8, ACCENT, false);
        drawServer(context, px + 8, qy + 26, "Hypixel", "mc.hypixel.net", "34ms", true);
        drawServer(context, px + 8, qy + 48, "CubeCraft", "play.cubecraft.net", "72ms", true);
        drawServer(context, px + 8, qy + 70, "My SMP", "play.mysmp.net", "off", false);

        // Bottom
        RenderUtil.fill(context, 0, height - 20, width, height, 0xE0080A12);
        RenderUtil.drawText(context, textRenderer, "PixelForge v1.0.0 · Fabric 1.21.11 · Java 21",
                12, height - 14, TEXT_MUTED, false);
        RenderUtil.drawText(context, textRenderer, "Discord  ·  GitHub  ·  Report bug",
                width - 160, height - 14, TEXT_MUTED, false);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawNav(DrawContext context, String label, int x, boolean active) {
        RenderUtil.drawText(context, textRenderer, label, x, 10, active ? 0xFF748FFF : TEXT_DIM, false);
    }

    private void drawPanel(DrawContext context, int x, int y, int w, int h) {
        RenderUtil.fill(context, x, y, x + w, y + h, PANEL_BG);
        RenderUtil.drawBorder(context, x, y, w, h, PANEL_BORDER);
    }

    private void drawServer(DrawContext context, int x, int y, String name, String addr, String ping, boolean online) {
        RenderUtil.fill(context, x, y + 4, x + 6, y + 10, online ? 0xFF40C057 : 0xFFFA5252);
        RenderUtil.drawText(context, textRenderer, name, x + 12, y, TEXT, false);
        RenderUtil.drawText(context, textRenderer, addr, x + 12, y + 9, TEXT_MUTED, false);
        RenderUtil.drawText(context, textRenderer, ping, x + 150, y + 3, online ? 0xFF40C057 : 0xFFFA5252, false);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        if (mouseY < 30) {
            if (mouseX >= 250 && mouseX < 290) { client.setScreen(new ModsScreen(this)); return true; }
            if (mouseX >= 300 && mouseX < 360) { client.setScreen(new CrosshairScreen(this)); return true; }
            if (mouseX >= 370 && mouseX < 430) { client.setScreen(new AccountsScreen(this)); return true; }
        }
        int px = width - 220;
        if (mouseX >= px + 140 && mouseX <= px + 195 && mouseY >= 56 && mouseY <= 68) {
            client.setScreen(new AccountsScreen(this));
            return true;
        }
        if (mouseX >= px + 12 && mouseX <= px + 100 && mouseY >= 168 && mouseY <= 180) {
            client.setScreen(new AccountsScreen(this));
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    private static class Star {
        float x, y, speed, alpha;
        Star(float x, float y, float speed, float alpha) {
            this.x = x; this.y = y; this.speed = speed; this.alpha = alpha;
        }
    }
}
