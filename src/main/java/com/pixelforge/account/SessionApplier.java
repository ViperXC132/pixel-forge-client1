package com.pixelforge.account;

import com.mojang.authlib.GameProfile;
import com.pixelforge.PixelForgeClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

/**
 * Applies a new Session to MinecraftClient so the active account actually changes.
 * Uses reflection for compatibility across minor mapping changes.
 */
public final class SessionApplier {

    private SessionApplier() {}

    public static boolean apply(String username, String uuidStr, String accessToken, AccountManager.AccountType type) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return false;

        try {
            UUID uuid = UUID.fromString(uuidStr.contains("-") ? uuidStr : insertDashes(uuidStr));
            Session.AccountType sessionType = (type == AccountManager.AccountType.OFFLINE)
                    ? Session.AccountType.LEGACY
                    : Session.AccountType.MSA;

            // Yarn 1.21.x Session constructor variants — try common ones
            Session session = createSession(username, uuid, accessToken, sessionType);
            if (session == null) {
                PixelForgeClient.LOGGER.error("Could not construct Session");
                return false;
            }

            // Set MinecraftClient.session field
            Field sessionField = findSessionField(MinecraftClient.class);
            if (sessionField == null) {
                PixelForgeClient.LOGGER.error("Could not find session field on MinecraftClient");
                return false;
            }
            sessionField.setAccessible(true);
            sessionField.set(client, session);

            // Clear user API service cache if present (best-effort)
            tryClearUserCache(client);

            PixelForgeClient.LOGGER.info("Session applied: {} ({})", username, type);
            return true;
        } catch (Exception e) {
            PixelForgeClient.LOGGER.error("Failed to apply session", e);
            return false;
        }
    }

    private static Session createSession(String username, UUID uuid, String token, Session.AccountType type) {
        // Try modern Session(String username, UUID uuid, String accessToken, Optional<String> xuid, Optional<String> clientId, AccountType)
        try {
            return new Session(username, uuid, token, Optional.empty(), Optional.empty(), type);
        } catch (Throwable ignored) {}

        // Fallback via reflection on constructors
        for (var ctor : Session.class.getDeclaredConstructors()) {
            try {
                ctor.setAccessible(true);
                Class<?>[] p = ctor.getParameterTypes();
                if (p.length == 6) {
                    Object[] args = new Object[6];
                    for (int i = 0; i < 6; i++) {
                        if (p[i] == String.class) {
                            if (args[0] == null) args[i] = username;
                            else args[i] = token;
                        } else if (p[i] == UUID.class) {
                            args[i] = uuid;
                        } else if (p[i] == Optional.class) {
                            args[i] = Optional.empty();
                        } else if (p[i].isEnum()) {
                            args[i] = type;
                        } else {
                            args[i] = null;
                        }
                    }
                    // Ensure order roughly username, uuid, token...
                    args[0] = username;
                    for (int i = 0; i < p.length; i++) {
                        if (p[i] == UUID.class) args[i] = uuid;
                        if (p[i] == String.class && i > 0 && args[i] == username) args[i] = token;
                    }
                    return (Session) ctor.newInstance(args);
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static Field findSessionField(Class<?> clazz) {
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getType().getName().contains("Session")) {
                return f;
            }
        }
        // recursive super
        if (clazz.getSuperclass() != null) {
            return findSessionField(clazz.getSuperclass());
        }
        return null;
    }

    private static void tryClearUserCache(MinecraftClient client) {
        try {
            for (Field f : client.getClass().getDeclaredFields()) {
                String n = f.getType().getName().toLowerCase();
                if (n.contains("userapi") || n.contains("profilekeys") || n.contains("social")) {
                    f.setAccessible(true);
                    // leave as-is; clearing can NPE — just touch session is enough for name display
                }
            }
        } catch (Throwable ignored) {}
    }

    private static String insertDashes(String flat) {
        if (flat.length() != 32) return flat;
        return flat.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5");
    }

    public static String currentUsername() {
        try {
            MinecraftClient c = MinecraftClient.getInstance();
            if (c != null && c.getSession() != null) {
                return c.getSession().getUsername();
            }
        } catch (Throwable ignored) {}
        return "?";
    }
}
