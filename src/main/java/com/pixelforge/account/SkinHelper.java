package com.pixelforge.account;

import com.pixelforge.PixelForgeClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Resolves player skin identifiers for account previews. */
public final class SkinHelper {
    private static final Map<String, Identifier> CACHE = new ConcurrentHashMap<>();

    private SkinHelper() {}

    public static Identifier getSkinTexture(String username) {
        if (username == null || username.isBlank()) return DefaultSkinHelper.getSteve().body().texturePath();
        return CACHE.computeIfAbsent(username.toLowerCase(), SkinHelper::resolve);
    }

    private static Identifier resolve(String username) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return DefaultSkinHelper.getSteve().body().texturePath();

        if (mc.getNetworkHandler() != null) {
            for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
                if (entry.getProfile().name().equalsIgnoreCase(username)) {
                    return entry.getSkinTextures().body().texturePath();
                }
            }
        }

        if (mc.player != null && mc.player.getGameProfile().name().equalsIgnoreCase(username)) {
            return DefaultSkinHelper.getSkinTextures(mc.player.getGameProfile()).body().texturePath();
        }

        UUID offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
        try {
            return DefaultSkinHelper.getSkinTextures(offlineId).body().texturePath();
        } catch (Exception e) {
            PixelForgeClient.LOGGER.debug("Skin resolve fallback for {}", username);
            return DefaultSkinHelper.getSteve().body().texturePath();
        }
    }

    /** Draws an 8x8 head face region scaled up. */
    public static void drawHead(DrawContext context, String username, int x, int y, int size) {
        Identifier skin = getSkinTexture(username);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, skin, x, y,
                8.0f, 8.0f, size, size, 8, 8, 64, 64);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, skin, x, y,
                40.0f, 8.0f, size, size, 8, 8, 64, 64);
    }
}
