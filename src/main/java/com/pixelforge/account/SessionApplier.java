package com.pixelforge.account;

import com.pixelforge.PixelForgeClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.lang.reflect.Field;
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
            String accountTypeName = type == AccountManager.AccountType.OFFLINE ? "LEGACY" : "MSA";

            Session session = createSession(username, uuid, accessToken, accountTypeName);
            if (session == null) {
                PixelForgeClient.LOGGER.error("Could not construct Session");
                return false;
            }

            Field sessionField = findSessionField(MinecraftClient.class);
            if (sessionField == null) {
                PixelForgeClient.LOGGER.error("Could not find session field on MinecraftClient");
                return false;
            }
            sessionField.setAccessible(true);
            sessionField.set(client, session);
            tryClearUserCache(client);

            PixelForgeClient.LOGGER.info("Session applied: {} ({})", username, type);
            return true;
        } catch (Exception e) {
            PixelForgeClient.LOGGER.error("Failed to apply session", e);
            return false;
        }
    }

    private static Session createSession(String username, UUID uuid, String token, String accountTypeName) {
        for (var ctor : Session.class.getDeclaredConstructors()) {
            try {
                ctor.setAccessible(true);
                Class<?>[] p = ctor.getParameterTypes();
                if (p.length != 6) continue;

                Object[] args = new Object[6];
                int stringIndex = 0;
                boolean valid = true;
                for (int i = 0; i < p.length; i++) {
                    if (p[i] == String.class) {
                        args[i] = stringIndex++ == 0 ? username : token;
                    } else if (p[i] == UUID.class) {
                        args[i] = uuid;
                    } else if (p[i] == Optional.class) {
                        args[i] = Optional.empty();
                    } else if (p[i].isEnum()) {
                        @SuppressWarnings("unchecked")
                        Class<? extends Enum> enumClass = (Class<? extends Enum>) p[i];
                        try {
                            args[i] = Enum.valueOf(enumClass, accountTypeName);
                        } catch (IllegalArgumentException ex) {
                            args[i] = Enum.valueOf(enumClass, "LEGACY");
                        }
                    } else {
                        valid = false;
                        break;
                    }
                }
                if (valid) return (Session) ctor.newInstance(args);
            } catch (Throwable ignored) {
                // Try the next constructor shape.
            }
        }
        return null;
    }

    private static Field findSessionField(Class<?> clazz) {
        for (Field f : clazz.getDeclaredFields()) {
            if (f.getType().getName().contains("Session")) return f;
        }
        if (clazz.getSuperclass() != null) return findSessionField(clazz.getSuperclass());
        return null;
    }

    private static void tryClearUserCache(MinecraftClient client) {
        // Session replacement is sufficient for the account display. Keep this best-effort
        // hook without touching internal services whose fields change between releases.
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
            if (c != null && c.getSession() != null) return c.getSession().getUsername();
        } catch (Throwable ignored) {}
        return "?";
    }
}
