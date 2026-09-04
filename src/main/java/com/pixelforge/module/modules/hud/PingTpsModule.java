package com.pixelforge.module.modules.hud;

import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public class PingTpsModule extends Module {
    private final Setting<Boolean> showTps = addSetting(new Setting<>("Show TPS", true));

    public PingTpsModule() {
        super("Ping TPS", "Shows latency and estimated TPS", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.player == null || mc.textRenderer == null) return;
        int ping = 0;
        if (mc.getNetworkHandler() != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) ping = entry.getLatency();
        }
        float tps = 20.0f;
        if (mc.getServer() != null) {
            try {
                tps = Math.min(20.0f, 1000.0f / Math.max(1f, mc.getServer().getAverageTickTime()));
            } catch (Throwable ignored) {}
        }
        int color = ping < 50 ? 0xFF55FF55 : (ping < 100 ? 0xFFFFFF55 : 0xFFFF5555);
        String text = showTps.get()
                ? String.format("Ping: %dms | TPS: %.1f", ping, tps)
                : String.format("Ping: %dms", ping);
        RenderUtil.drawHudBox(context, mc.textRenderer, text, HudRenderer.getX(getName()), HudRenderer.getY(getName()), color);
    }
}
