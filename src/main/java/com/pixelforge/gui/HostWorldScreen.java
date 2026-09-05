package com.pixelforge.gui;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

/**
 * In-client local hosting controls. This deliberately uses Minecraft's integrated
 * server so the singleplayer world remains authoritative; Playit is an optional
 * external tunnel process on top of that local port.
 */
public class HostWorldScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget portField;
    private String status = "Offline";
    private Process playitProcess;

    public HostWorldScreen(Screen parent) {
        super(Text.literal("Host World"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 360;
        int left = (width - w) / 2;
        int top = (height - 220) / 2;
        portField = new TextFieldWidget(textRenderer, left + 40, top + 72, 120, 20, Text.literal("Port"));
        portField.setText("25565");
        portField.setMaxLength(5);
        addDrawableChild(portField);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        int w = 360, h = 220;
        int left = (width - w) / 2, top = (height - h) / 2;
        c.fill(0, 0, width, height, 0x66000000);
        c.fill(left, top, left + w, top + h, 0xE0101010);
        border(c, left, top, w, h, 0x66FFFFFF);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal("Host World"), left + w / 2, top + 16, 0xFFFFFFFF);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(status), left + w / 2, top + 42, 0xFFCCCCCC);
        c.drawTextWithShadow(textRenderer, Text.literal("Port"), left + 40, top + 58, 0xFFAAAAAA);

        button(c, left + 175, top + 72, 145, 20, "Start Local Host", mx, my);
        button(c, left + 40, top + 108, 145, 20, "Launch Playit", mx, my);
        button(c, left + 175, top + 108, 145, 20, "Stop Playit", mx, my);
        button(c, left + 40, top + 144, 280, 20, "Back", mx, my);

        String info = "Playit requires the playit executable installed and available on PATH.";
        c.drawTextWithShadow(textRenderer, Text.literal(info), left + 40, top + 184, 0xFF888888);
        super.render(c, mx, my, delta);
    }

    private void border(DrawContext c, int x, int y, int w, int h, int color) {
        c.fill(x, y, x + w, y + 1, color);
        c.fill(x, y + h - 1, x + w, y + h, color);
        c.fill(x, y, x + 1, y + h, color);
        c.fill(x + w - 1, y, x + w, y + h, color);
    }

    private void button(DrawContext c, int x, int y, int w, int h, String text, int mx, int my) {
        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + h;
        c.fill(x, y, x + w, y + h, hover ? 0x40FFFFFF : 0x20FFFFFF);
        border(c, x, y, w, h, hover ? 0xAAFFFFFF : 0x40FFFFFF);
        c.drawCenteredTextWithShadow(textRenderer, Text.literal(text), x + w / 2, y + 6, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        int w = 360, h = 220;
        int left = (width - w) / 2, top = (height - h) / 2;
        double x = click.x(), y = click.y();

        if (x >= left + 175 && x <= left + 320 && y >= top + 72 && y <= top + 92) {
            startHost();
            return true;
        }
        if (x >= left + 40 && x <= left + 185 && y >= top + 108 && y <= top + 128) {
            launchPlayit();
            return true;
        }
        if (x >= left + 175 && x <= left + 320 && y >= top + 108 && y <= top + 128) {
            stopPlayit();
            return true;
        }
        if (x >= left + 40 && x <= left + 320 && y >= top + 144 && y <= top + 164) {
            stopPlayit();
            client.setScreen(parent);
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    private void startHost() {
        if (client.world == null) {
            status = "Open a singleplayer world first";
            return;
        }
        IntegratedServer server = client.getServer();
        if (server == null) {
            status = "Integrated server unavailable";
            return;
        }
        int port;
        try {
            port = Integer.parseInt(portField.getText());
            if (port < 1024 || port > 65535) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            status = "Port must be 1024-65535";
            return;
        }
        try {
            boolean opened = server.openToLan(GameMode.SURVIVAL, true, port);
            status = opened ? "Hosting locally on port " + server.getServerPort() : "Could not open LAN";
        } catch (Throwable t) {
            status = "Host failed: " + t.getClass().getSimpleName();
        }
    }

    private void launchPlayit() {
        if (client.getServer() == null) {
            status = "Start the local host first";
            return;
        }
        if (playitProcess != null && playitProcess.isAlive()) {
            status = "Playit already running";
            return;
        }
        try {
            playitProcess = new ProcessBuilder("playit").redirectErrorStream(true).start();
            status = "Playit started — configure its tunnel for port " + client.getServer().getServerPort();
        } catch (Throwable t) {
            status = "Playit not found — install playit and add it to PATH";
        }
    }

    private void stopPlayit() {
        if (playitProcess != null) {
            playitProcess.destroy();
            playitProcess = null;
            status = "Playit stopped";
        }
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        if (input.key() == 256) {
            stopPlayit();
            client.setScreen(parent);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        stopPlayit();
        client.setScreen(parent);
    }
}
