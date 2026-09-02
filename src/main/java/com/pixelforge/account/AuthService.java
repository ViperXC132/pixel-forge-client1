package com.pixelforge.account;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pixelforge.PixelForgeClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/** Authentication for Offline, Microsoft/Xbox, ely.by and LittleSkin. */
public final class AuthService {
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(12)).build();
    // Public client id used by the Minecraft launcher family for Microsoft sign-in.
    private static final String MS_CLIENT_ID = "00000000402b5328";
    private static final String MS_SCOPE = "XboxLive.signin offline_access";

    public static final class AuthResult {
        public final boolean ok; public final String message; public final String username;
        public final String uuid; public final String accessToken; public final AccountManager.AccountType type;
        public final String refreshToken;
        public AuthResult(boolean ok, String message, String username, String uuid, String accessToken,
                          AccountManager.AccountType type) { this(ok, message, username, uuid, accessToken, type, null); }
        public AuthResult(boolean ok, String message, String username, String uuid, String accessToken,
                          AccountManager.AccountType type, String refreshToken) {
            this.ok=ok; this.message=message; this.username=username; this.uuid=uuid;
            this.accessToken=accessToken; this.type=type; this.refreshToken=refreshToken;
        }
        public static AuthResult fail(String msg) { return new AuthResult(false,msg,null,null,null,null,null); }
    }

    private AuthService() {}

    public static AuthResult login(AccountManager.AccountType type, String username, String password) {
        return switch (type) {
            case OFFLINE -> offline(username);
            case ELYBY -> yggdrasil("https://authserver.ely.by/auth/authenticate", username, password, type);
            case LITTLESKIN -> yggdrasil("https://littleskin.cn/api/yggdrasil/authserver/authenticate", username, password, type);
            case MICROSOFT -> AuthResult.fail("Use the Microsoft sign-in button to open the secure device-code flow");
        };
    }

    /** Starts Microsoft device-code login without asking PixelForge to collect a Microsoft password. */
    public static void loginMicrosoftAsync(Consumer<AuthResult> callback) {
        CompletableFuture.runAsync(() -> {
            AuthResult result = microsoftLogin();
            MinecraftClient.getInstance().execute(() -> callback.accept(result));
        });
    }

    private static AuthResult microsoftLogin() {
        try {
            String form = "client_id=" + enc(MS_CLIENT_ID) + "&scope=" + enc(MS_SCOPE);
            HttpResponse<String> device = HTTP.send(post("https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode", form), HttpResponse.BodyHandlers.ofString());
            if (device.statusCode() != 200) return AuthResult.fail("Microsoft device login unavailable (HTTP " + device.statusCode() + ")");
            JsonObject d = JsonParser.parseString(device.body()).getAsJsonObject();
            String code = d.get("user_code").getAsString();
            String uri = d.get("verification_uri").getAsString();
            long expires = System.currentTimeMillis() + d.get("expires_in").getAsLong() * 1000L;
            String message = "Open " + uri + " and enter code " + code;
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> {
                client.inGameHud.getChatHud().addMessage(net.minecraft.text.Text.literal("PixelForge Microsoft login: " + message));
                Util.getOperatingSystem().open(uri);
            });

            long interval = Math.max(2, d.has("interval") ? d.get("interval").getAsLong() : 5);
            JsonObject token = null;
            while (System.currentTimeMillis() < expires) {
                Thread.sleep(interval * 1000L);
                String tokenForm = "grant_type=urn:ietf:params:oauth:grant-type:device_code&client_id=" + enc(MS_CLIENT_ID) + "&device_code=" + enc(d.get("device_code").getAsString());
                HttpResponse<String> tr = HTTP.send(post("https://login.microsoftonline.com/consumers/oauth2/v2.0/token", tokenForm), HttpResponse.BodyHandlers.ofString());
                JsonObject body = JsonParser.parseString(tr.body()).getAsJsonObject();
                if (tr.statusCode() == 200 && body.has("access_token")) { token = body; break; }
                String err = body.has("error") ? body.get("error").getAsString() : "";
                if (!"authorization_pending".equals(err) && !"slow_down".equals(err)) return AuthResult.fail("Microsoft sign-in failed: " + err);
                if ("slow_down".equals(err)) interval += 5;
            }
            if (token == null) return AuthResult.fail("Microsoft sign-in timed out");

            String msToken = token.get("access_token").getAsString();
            String refresh = token.has("refresh_token") ? token.get("refresh_token").getAsString() : null;
            String xbl = xboxUserToken(msToken);
            String xsts = xstsToken(xbl);
            String mcToken = minecraftToken(xsts);
            JsonObject profile = minecraftProfile(mcToken);
            if (!profile.has("id") || !profile.has("name")) return AuthResult.fail("No Minecraft Java profile found on this Microsoft account");
            String id = dashed(profile.get("id").getAsString());
            return new AuthResult(true, "Logged in as " + profile.get("name").getAsString(), profile.get("name").getAsString(), id, mcToken, AccountManager.AccountType.MICROSOFT, refresh);
        } catch (Exception e) {
            PixelForgeClient.LOGGER.error("Microsoft auth error", e);
            return AuthResult.fail("Microsoft sign-in error: " + e.getMessage());
        }
    }

    private static String xboxUserToken(String accessToken) throws Exception {
        JsonObject props = new JsonObject(); props.addProperty("AuthMethod", "RPS"); props.addProperty("SiteName", "user.auth.xboxlive.com"); props.addProperty("RpsTicket", "d=" + accessToken);
        JsonObject body = new JsonObject(); body.add("Properties", props); body.addProperty("RelyingParty", "http://auth.xboxlive.com"); body.addProperty("TokenType", "JWT");
        JsonObject r = sendJson("https://user.auth.xboxlive.com/user/authenticate", body);
        return r.get("Token").getAsString() + "|" + r.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString();
    }

    private static String xstsToken(String packed) throws Exception {
        String[] p = packed.split("\\|", 2);
        JsonObject props = new JsonObject(); JsonArray tokens = new JsonArray(); tokens.add(p[0]); props.add("UserTokens", tokens);
        JsonObject body = new JsonObject(); body.add("Properties", props); body.addProperty("RelyingParty", "rp://api.minecraftservices.com/"); body.addProperty("TokenType", "JWT");
        JsonObject r = sendJson("https://xsts.auth.xboxlive.com/xsts/authorize", body);
        return r.get("Token").getAsString() + "|" + p[1];
    }

    private static String minecraftToken(String packed) throws Exception {
        String[] p = packed.split("\\|", 2);
        JsonObject body = new JsonObject(); body.addProperty("identityToken", "XBL3.0 x=" + p[1] + ";" + p[0]);
        JsonObject r = sendJson("https://api.minecraftservices.com/authentication/login_with_xbox", body);
        return r.get("access_token").getAsString();
    }

    private static JsonObject minecraftProfile(String token) throws Exception {
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create("https://api.minecraftservices.com/minecraft/profile"))
                .header("Authorization", "Bearer " + token).GET().timeout(Duration.ofSeconds(15)).build();
        HttpResponse<String> r = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        return JsonParser.parseString(r.body()).getAsJsonObject();
    }

    private static JsonObject sendJson(String url, JsonObject body) throws Exception {
        HttpResponse<String> r = HTTP.send(HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body.toString())).timeout(Duration.ofSeconds(20)).build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() < 200 || r.statusCode() >= 300) throw new IllegalStateException("HTTP " + r.statusCode());
        return JsonParser.parseString(r.body()).getAsJsonObject();
    }

    private static HttpRequest post(String url, String form) { return HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(form)).timeout(Duration.ofSeconds(20)).build(); }
    private static String enc(String s) { return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8); }

    private static AuthResult offline(String username) {
        if (username == null || username.isBlank()) return AuthResult.fail("Enter a username");
        username=username.trim(); if(username.length()<3||username.length()>16) return AuthResult.fail("Username must be 3–16 characters");
        UUID uuid=UUID.nameUUIDFromBytes(("OfflinePlayer:"+username).getBytes(StandardCharsets.UTF_8));
        return new AuthResult(true,"Offline login OK",username,uuid.toString(),"0",AccountManager.AccountType.OFFLINE);
    }

    private static AuthResult yggdrasil(String url,String username,String password,AccountManager.AccountType type) {
        if(username==null||username.isBlank())return AuthResult.fail("Enter username or email"); if(password==null||password.isBlank())return AuthResult.fail("Enter password");
        try { JsonObject body=new JsonObject(); body.addProperty("username",username.trim()); body.addProperty("password",password); body.addProperty("clientToken",UUID.randomUUID().toString().replace("-","")); body.addProperty("requestUser",true); JsonObject agent=new JsonObject(); agent.addProperty("name","Minecraft"); agent.addProperty("version",1); body.add("agent",agent);
            HttpResponse<String> res=HTTP.send(HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body.toString())).timeout(Duration.ofSeconds(20)).build(),HttpResponse.BodyHandlers.ofString()); String resp=res.body()==null?"":res.body();
            if(res.statusCode()!=200){try{JsonObject err=JsonParser.parseString(resp).getAsJsonObject();String msg=err.has("errorMessage")?err.get("errorMessage").getAsString():resp;if(msg.toLowerCase().contains("two factor"))return AuthResult.fail("2FA required — append :CODE to password");return AuthResult.fail(msg.isEmpty()?"Auth failed ("+res.statusCode()+")":msg);}catch(Exception e){return AuthResult.fail("Auth failed (HTTP "+res.statusCode()+")");}}
            JsonObject json=JsonParser.parseString(resp).getAsJsonObject(); if(!json.has("accessToken"))return AuthResult.fail("Invalid auth response"); JsonObject profile=json.has("selectedProfile")?json.getAsJsonObject("selectedProfile"):json.getAsJsonArray("availableProfiles").get(0).getAsJsonObject(); String id=dashed(profile.get("id").getAsString()); return new AuthResult(true,"Logged in as "+profile.get("name").getAsString(),profile.get("name").getAsString(),id,json.get("accessToken").getAsString(),type);
        }catch(Exception e){PixelForgeClient.LOGGER.error("Auth error",e);return AuthResult.fail("Network error: "+e.getMessage());}
    }
    private static String dashed(String id){if(id.length()!=32)return id;return id.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)","$1-$2-$3-$4-$5");}
}
