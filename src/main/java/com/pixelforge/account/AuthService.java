package com.pixelforge.account;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pixelforge.PixelForgeClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

/**
 * Authenticates against Offline / ely.by / LittleSkin (Yggdrasil).
 */
public final class AuthService {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(12))
            .build();

    public static final class AuthResult {
        public final boolean ok;
        public final String message;
        public final String username;
        public final String uuid;
        public final String accessToken;
        public final AccountManager.AccountType type;

        public AuthResult(boolean ok, String message, String username, String uuid, String accessToken, AccountManager.AccountType type) {
            this.ok = ok;
            this.message = message;
            this.username = username;
            this.uuid = uuid;
            this.accessToken = accessToken;
            this.type = type;
        }

        public static AuthResult fail(String msg) {
            return new AuthResult(false, msg, null, null, null, null);
        }
    }

    private AuthService() {}

    public static AuthResult login(AccountManager.AccountType type, String username, String password) {
        return switch (type) {
            case OFFLINE -> offline(username);
            case ELYBY -> yggdrasil("https://authserver.ely.by/auth/authenticate", username, password, type);
            case LITTLESKIN -> yggdrasil("https://littleskin.cn/api/yggdrasil/authserver/authenticate", username, password, type);
            case MICROSOFT -> AuthResult.fail("Microsoft login needs browser OAuth — use Offline / ely.by / LittleSkin for password login");
        };
    }

    private static AuthResult offline(String username) {
        if (username == null || username.isBlank()) {
            return AuthResult.fail("Enter a username");
        }
        username = username.trim();
        if (username.length() < 3 || username.length() > 16) {
            return AuthResult.fail("Username must be 3–16 characters");
        }
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
        return new AuthResult(true, "Offline login OK", username, uuid.toString(), "0", AccountManager.AccountType.OFFLINE);
    }

    private static AuthResult yggdrasil(String url, String username, String password, AccountManager.AccountType type) {
        if (username == null || username.isBlank()) return AuthResult.fail("Enter username or email");
        if (password == null || password.isBlank()) return AuthResult.fail("Enter password");

        try {
            String clientToken = UUID.randomUUID().toString().replace("-", "");
            JsonObject body = new JsonObject();
            body.addProperty("username", username.trim());
            body.addProperty("password", password);
            body.addProperty("clientToken", clientToken);
            body.addProperty("requestUser", true);

            JsonObject agent = new JsonObject();
            agent.addProperty("name", "Minecraft");
            agent.addProperty("version", 1);
            body.add("agent", agent);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "PixelForge/1.0.0")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            String resp = res.body() == null ? "" : res.body();

            if (res.statusCode() != 200) {
                try {
                    JsonObject err = JsonParser.parseString(resp).getAsJsonObject();
                    String msg = err.has("errorMessage") ? err.get("errorMessage").getAsString() : resp;
                    if (msg != null && msg.toLowerCase().contains("two factor")) {
                        return AuthResult.fail("2FA required — append :CODE to password (e.g. pass:123456)");
                    }
                    return AuthResult.fail(msg.isEmpty() ? ("Auth failed (" + res.statusCode() + ")") : msg);
                } catch (Exception e) {
                    return AuthResult.fail("Auth failed (HTTP " + res.statusCode() + ")");
                }
            }

            JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
            if (!json.has("accessToken")) {
                return AuthResult.fail("Invalid auth response");
            }

            String accessToken = json.get("accessToken").getAsString();
            String name;
            String id;

            if (json.has("selectedProfile") && json.get("selectedProfile").isJsonObject()) {
                JsonObject profile = json.getAsJsonObject("selectedProfile");
                name = profile.get("name").getAsString();
                id = profile.get("id").getAsString();
            } else if (json.has("availableProfiles") && json.getAsJsonArray("availableProfiles").size() > 0) {
                JsonObject profile = json.getAsJsonArray("availableProfiles").get(0).getAsJsonObject();
                name = profile.get("name").getAsString();
                id = profile.get("id").getAsString();
            } else {
                return AuthResult.fail("No profile on this account");
            }

            // Normalize UUID to dashed form if needed
            if (id.length() == 32) {
                id = id.replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                        "$1-$2-$3-$4-$5");
            }

            return new AuthResult(true, "Logged in as " + name, name, id, accessToken, type);
        } catch (Exception e) {
            PixelForgeClient.LOGGER.error("Auth error", e);
            return AuthResult.fail("Network error: " + e.getMessage());
        }
    }
}
