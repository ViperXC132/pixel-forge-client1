package com.pixelforge.gui;

import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.Locale;

/**
 * One-click local-world hosting helper for Playit.
 * Minecraft's integrated server is opened to LAN first; Playit's agent then exposes that
 * local TCP port without requiring router port forwarding.
 */
public final class HostWorldScreen extends Screen {
    private final Screen parent;
    private boolean started;
    private int port = 25565;
    private String status = "Ready to host this world.";

    public HostWorldScreen(Screen parent) {
        super(Text.literal("Host World"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        int w = 560, h = 360, left = (width - w) / 2, top = (height - h) / 2;
        RenderUtil.fill(c, 0, 0, width, height, 0x78000000);
        RenderUtil.drawRoundedPanel(c, left, top, w, h, 0xE0101010, 0x60FFFFFF);
        RenderUtil.drawText(c, textRenderer, "HOST WORLD", left + 22, top + 20, 0xFFFFFFFF, false);
        RenderUtil.drawText(c, textRenderer, "Share your singleplayer world through Playit.", left + 22, top + 37, 0xFF999999, false);

        RenderUtil.drawText(c, textRenderer, "LOCAL SERVER", left + 22, top + 72, 0xFF777777, false);
        RenderUtil.drawText(c, textRenderer, started ? "ONLINE" : "OFFLINE", left + 22, top + 90, 0xFFFFFFFF, false);
        RenderUtil.drawText(c, textRenderer, started ? "127.0.0.1:" + port : "No LAN port opened yet", left + 22, top + 108, 0xFFAAAAAA, false);

        RenderUtil.drawText(c, textRenderer, "1", left + 22, top + 148, 0xFFFFFFFF, false);
        RenderUtil.drawText(c, textRenderer, "Start the local Minecraft LAN server.", left + 42, top + 148, 0xFFFFFFFF, false);
        RenderUtil.drawText(c, textRenderer, "2", left + 22, top + 172, 0xFFFFFFFF, false);
        RenderUtil.drawText(c, textRenderer, "Run/claim the Playit agent on this PC.", left + 42, top + 172, 0xFFFFFFFF, false);
        RenderUtil.drawText(c, textRenderer, "3", left + 22, top + 196, 0xFFFFFFFF, false);
        RenderUtil.drawText(c, textRenderer, "Create a Minecraft Java tunnel to the port above.", left + 42, top + 196, 0xFFFFFFFF, false);

        RenderUtil.drawText(c, textRenderer, status, left + 22, top + 228, 0xFFAAAAAA, false);
        button(c, left + 22, top + 262, 166, 28, started ? "LAN Started" : "Start Hosting", mx, my);
        button(c, left + 198, top + 262, 166, 28, "Open Playit", mx, my);
        button(c, left + 374, top + 262, 164, 28, "Back", mx, my);

        RenderUtil.drawText(c, textRenderer, "Playit uses a separate agent; this client never stores your Playit credentials.", left + 22, top + 320, 0xFF666666, false);
        super.render(c, mx, my, d);
    }

    private void button(DrawContext c, int x, int y, int w, int h, String label, int mx, int my) {
        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + h;
        RenderUtil.fill(c, x, y, x + w, y + h, hover ? 0x45FFFFFF : 0x20FFFFFF);
        RenderUtil.drawBorder(c, x, y, w, h, hover ? 0xAAFFFFFF : 0x45FFFFFF);
        RenderUtil.drawCenteredText(c, textRenderer, label, x + w / 2, y + 8, 0xFFFFFFFF, false);
    }

    private void startHosting() {
        if (client == null || client.getServer() == null) {
            status = "Open a singleplayer world first.";
            return;
        }
        if (client.getServer().getServerPort() > 0 && client.getServer().getServerPort() != -1) {
            port = client.getServer().getServerPort();
            started = true;
            status = "LAN server is already running.";
            return;
        }
        for (int candidate = 25565; candidate <= 25575; candidate++) {
            try {
                if (client.getServer().openToLan(null, false, candidate)) {
                    port = candidate;
                    started = true;
                    status = "LAN is live. Open Playit and create a Minecraft Java tunnel.";
                    return;
                }
            } catch (Throwable ignored) {}
        }
        status = "Could not open a LAN port. Check firewall/network permissions.";
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        int w = 560, h = 360, left = (width - w) / 2, top = (height - h) / 2;
        int y = top + 262;
        if (click.x() >= left + 22 && click.x() <= left + 188 && click.y() >= y && click.y() <= y + 28) { startHosting(); return true; }
        if (click.x() >= left + 198 && click.x() <= left + 364 && click.y() >= y && click.y() <= y + 28) {
            try { Util.getOperatingSystem().open("https://playit.gg/download"); status = "Opened the official Playit download page."; }
            catch (Throwable t) { status = "Could not open your browser."; }
            return true;
        }
        if (click.x() >= left + 374 && click.x() <= left + 538 && click.y() >= y && click.y() <= y + 28) { close(); return true; }
        return super.mouseClicked(click, doubled);
    }

    @Override public boolean keyPressed(net.minecraft.client.input.KeyInput i) { if (i.key() == 256) { close(); return true; } return super.keyPressed(i); }
    @Override public boolean shouldPause() { return true; }
    @Override public void close() { if (client != null) client.setScreen(parent); }
}
