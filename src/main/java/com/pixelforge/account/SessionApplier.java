package com.pixelforge.account;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/**
 * Applies an account to the live Minecraft session on 1.21.11.
 * Uses the accessor mixin so the final Session field is actually replaced.
 */
public final class SessionApplier {
    private SessionApplier() {}

    public static boolean apply(String username, String uuidStr, String accessToken, AccountManager.AccountType type) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || username == null || username.isBlank()) return false;

        try {
            UUID uuid = parseUuid(uuidStr, username);
            String token = (accessToken == null || accessToken.isBlank()) ? "0" : accessToken;

            // 1.21.x Session: username, uuid, accessToken, xuid, clientId
            Session session = new Session(
                    username,
                    uuid,
                    token,
                    Optional.empty(),
                    Optional.empty()
            );

            MinecraftClientAccessor accessor = (MinecraftClientAccessor) (Object) client;
            accessor.pixelforge$setSession(session);

            // Verify the write stuck
            Session now = client.getSession();
            if (now == null || !username.equalsIgnoreCase(now.getUsername())) {
                PixelForgeClient.LOGGER.error("Session write did not stick for {}", username);
                return false;
            }

            PixelForgeClient.LOGGER.info("Session applied: {} ({}) uuid={}", username, type.displayName, uuid);
            return true;
        } catch (Throwable t) {
            PixelForgeClient.LOGGER.error("Failed to apply session for {}", username, t);
            return false;
        }
    }

    private static UUID parseUuid(String uuidStr, String username) {
        try {
            String normalized = uuidStr == null ? "" : uuidStr.trim();
            if (normalized.length() == 32 && !normalized.contains("-")) {
                normalized = insertDashes(normalized);
            }
            return UUID.fromString(normalized);
        } catch (Exception ignored) {
            return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
        }
    }

    private static String insertDashes(String flat) {
        return flat.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5"
        );
    }

    public static String currentUsername() {
        try {
            MinecraftClient c = MinecraftClient.getInstance();
            if (c != null && c.getSession() != null) return c.getSession().getUsername();
        } catch (Throwable ignored) {}
        return "?";
    }
}
