package com.pixelforge.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pixelforge.PixelForgeClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class AccountManager {

    public enum AccountType {
        MICROSOFT("Microsoft"),
        ELYBY("ely.by"),
        LITTLESKIN("LittleSkin"),
        OFFLINE("Offline");

        public final String displayName;
        AccountType(String displayName) { this.displayName = displayName; }
    }

    public static class Account {
        public String username;
        public AccountType type;
        public boolean active;
        public String uuid;
        public String accessToken; // stored for re-apply; offline uses "0"

        public Account() {}

        public Account(String username, AccountType type, boolean active, String uuid, String accessToken) {
            this.username = username;
            this.type = type;
            this.active = active;
            this.uuid = uuid;
            this.accessToken = accessToken;
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("pixelforge/accounts.json");
    private static final List<Account> ACCOUNTS = new ArrayList<>();

    static {
        load();
        if (ACCOUNTS.isEmpty()) {
            String name = "Player";
            try {
                if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().getSession() != null) {
                    name = MinecraftClient.getInstance().getSession().getUsername();
                }
            } catch (Exception ignored) {}
            AuthService.AuthResult offline = AuthService.login(AccountType.OFFLINE, name, "");
            if (offline.ok) {
                ACCOUNTS.add(new Account(offline.username, AccountType.OFFLINE, true, offline.uuid, offline.accessToken));
                save();
            }
        }
    }

    private AccountManager() {}

    public static List<Account> getAccounts() {
        return Collections.unmodifiableList(ACCOUNTS);
    }

    /** Login with username+password (or username only for offline), then apply session. */
    public static void loginAsync(AccountType type, String username, String password, Consumer<String> callback) {
        CompletableFuture.runAsync(() -> {
            AuthService.AuthResult result = AuthService.login(type, username, password);
            MinecraftClient.getInstance().execute(() -> {
                if (!result.ok) {
                    callback.accept(result.message);
                    return;
                }
                boolean applied = SessionApplier.apply(result.username, result.uuid, result.accessToken, result.type);
                if (!applied) {
                    callback.accept("Auth OK but failed to apply session");
                    return;
                }
                for (Account a : ACCOUNTS) a.active = false;
                // update existing or add
                Account existing = null;
                for (Account a : ACCOUNTS) {
                    if (a.username.equalsIgnoreCase(result.username) && a.type == result.type) {
                        existing = a;
                        break;
                    }
                }
                if (existing != null) {
                    existing.active = true;
                    existing.uuid = result.uuid;
                    existing.accessToken = result.accessToken;
                } else {
                    ACCOUNTS.add(new Account(result.username, result.type, true, result.uuid, result.accessToken));
                }
                save();
                PixelForgeClient.getInstance().getNotificationManager()
                        .push("Logged in as " + result.username, 0xFF40C057);
                callback.accept("OK:" + result.username);
            });
        });
    }

    public static void switchTo(Account account) {
        if (account.uuid == null || account.accessToken == null) {
            PixelForgeClient.getInstance().getNotificationManager()
                    .push("Re-login required for " + account.username, 0xFFFFAA00);
            return;
        }
        boolean ok = SessionApplier.apply(account.username, account.uuid, account.accessToken, account.type);
        if (!ok) {
            PixelForgeClient.getInstance().getNotificationManager()
                    .push("Failed to switch session", 0xFFFF5555);
            return;
        }
        for (Account a : ACCOUNTS) a.active = (a == account);
        save();
        PixelForgeClient.getInstance().getNotificationManager()
                .push("Switched to " + account.username, 0xFF3B5BDB);
    }

    public static Account getActive() {
        return ACCOUNTS.stream().filter(a -> a.active).findFirst().orElse(null);
    }

    private static void load() {
        try {
            if (!Files.exists(FILE)) return;
            String json = Files.readString(FILE);
            Type type = new TypeToken<List<Account>>(){}.getType();
            List<Account> loaded = GSON.fromJson(json, type);
            if (loaded != null) {
                ACCOUNTS.clear();
                ACCOUNTS.addAll(loaded);
            }
        } catch (Exception e) {
            PixelForgeClient.LOGGER.warn("Failed to load accounts", e);
        }
    }

    private static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(ACCOUNTS));
        } catch (IOException e) {
            PixelForgeClient.LOGGER.error("Failed to save accounts", e);
        }
    }
}
