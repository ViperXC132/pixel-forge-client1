package com.pixelforge.account;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.pixelforge.PixelForgeClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves player head skin identifiers for account previews.
 * Uses session/player list when available; offline UUID fallback otherwise.
 */
public final class SkinHelper {

    private static final Map<String, Identifier> CACHE = new ConcurrentHashMap<>();

    private SkinHelper() {}

    public static Identifier getSkinTexture(String username) {
        if (username == null || username.isBlank()) {
            return DefaultSkinHelper.getSteve().texture();
        }

        return CACHE.computeIfAbsent(username.toLowerCase(), key -> resolve(key));
    }

    private static Identifier resolve(String username) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return DefaultSkinHelper.getSteve().texture();

        // Active multiplayer list entry
        if (mc.getNetworkHandler() != null) {
            for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
                if (entry.getProfile().getName().equalsIgnoreCase(username)) {
                    return entry.getSkinTextures().texture();
                }
            }
        }

        // Local player
        if (mc.player != null && mc.player.getGameProfile().getName().equalsIgnoreCase(username)) {
            return mc.player.getSkinTextures().texture();
        }

        // Offline UUID skin (Steve/Alex model default until real texture loads)
        UUID offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
        try {
            return DefaultSkinHelper.getSkinTextures(offlineId).texture();
        } catch (Exception e) {
            PixelForgeClient.LOGGER.debug("Skin resolve fallback for {}", username);
            return DefaultSkinHelper.getSteve().texture();
        }
    }

    /** Draws an 8x8 head face region scaled up. */
    public static void drawHead(net.minecraft.client.gui.DrawContext context, String username, int x, int y, int size) {
        Identifier skin = getSkinTexture(username);
        // Vanilla skin: head is at u=8,v=8 size 8x8 on 64x64 texture
        // DrawContext.drawTexture(Identifier, x, y, u, v, width, height, textureWidth, textureHeight) varies by version
        try {
            context.drawTexture(skin, x, y, size, size, 8.0f, 8.0f, 8, 8, 64, 64);
            // hat layer
            context.drawTexture(skin, x, y, size, size, 40.0f, 8.0f, 8, 8, 64, 64);
        } catch (Throwable t) {
            // Signature differences across mappings — solid fallback
            context.fill(x, y, x + size, y + size, 0xFF1A2040);
        }
    }
}
