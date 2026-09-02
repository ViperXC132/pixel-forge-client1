package com.pixelforge.module.modules.hud;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public class PingTpsModule extends Module {

    private int x = 4;
    private int y = 74;

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

        // Simple TPS estimate (client-side only, not perfect)
        float tps = 20.0f;
        if (mc.getServer() != null) {
            // integrated server
            tps = Math.min(20.0f, 1000.0f / Math.max(1, mc.getServer().getAverageTickTime()));
        }

        int pingColor = ping < 50 ? 0xFF55FF55 : (ping < 100 ? 0xFFFFFF55 : 0xFFFF5555);
        String text = String.format("Ping: %dms | TPS: %.1f", ping, tps);
        RenderUtil.drawText(context, mc.textRenderer, text, x, y, pingColor, true);
    }
}
