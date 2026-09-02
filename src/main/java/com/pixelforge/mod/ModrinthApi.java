package com.pixelforge.mod;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pixelforge.PixelForgeClient;
import net.minecraft.client.MinecraftClient;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Minimal Modrinth API client for search + version lookup.
 * Runs off-thread; results posted back to the client thread.
 */
public final class ModrinthApi {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private ModrinthApi() {}

    public static class ModResult {
        public final String projectId;
        public final String slug;
        public final String title;
        public final String description;
        public final String iconUrl;

        public ModResult(String projectId, String slug, String title, String description, String iconUrl) {
            this.projectId = projectId;
            this.slug = slug;
            this.title = title;
            this.description = description;
            this.iconUrl = iconUrl;
        }
    }

    public static void searchAsync(String query, Consumer<List<ModResult>> callback) {
        CompletableFuture.runAsync(() -> {
            List<ModResult> list = search(query);
            MinecraftClient.getInstance().execute(() -> callback.accept(list));
        });
    }

    public static List<ModResult> search(String query) {
        List<ModResult> out = new ArrayList<>();
        try {
            String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
            // Facet: fabric + 1.21.11
            String facets = URLEncoder.encode("[[\"categories:fabric\"],[\"versions:1.21.11\"]]", StandardCharsets.UTF_8);
            String url = "https://api.modrinth.com/v2/search?query=" + q + "&limit=20&facets=" + facets;

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "PixelForge/1.0.0 (github.com/ViperXC132/pixel-forge-client)")
                    .GET()
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) return out;

            JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
            JsonArray hits = root.getAsJsonArray("hits");
            if (hits == null) return out;

            for (JsonElement el : hits) {
                JsonObject o = el.getAsJsonObject();
                String id = o.has("project_id") ? o.get("project_id").getAsString() : "";
                String slug = o.has("slug") ? o.get("slug").getAsString() : "";
                String title = o.has("title") ? o.get("title").getAsString() : slug;
                String desc = o.has("description") ? o.get("description").getAsString() : "";
                String icon = o.has("icon_url") && !o.get("icon_url").isJsonNull()
                        ? o.get("icon_url").getAsString() : null;
                // Prefer cdn style icon when possible
                if (icon == null && !id.isEmpty()) {
                    icon = "https://cdn.modrinth.com/data/" + id + "/icon.png";
                }
                out.add(new ModResult(id, slug, title, desc, icon));
            }
        } catch (Exception e) {
            PixelForgeClient.LOGGER.warn("Modrinth search failed: {}", e.getMessage());
        }
        return out;
    }

    /** Returns direct download URL for the latest Fabric 1.21.11 version, or null. */
    public static String getDownloadUrl(String projectId) {
        try {
            String url = "https://api.modrinth.com/v2/project/" + projectId + "/version?loaders=[%22fabric%22]&game_versions=[%221.21.11%22]";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "PixelForge/1.0.0 (github.com/ViperXC132/pixel-forge-client)")
                    .GET()
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) return null;

            JsonArray versions = JsonParser.parseString(res.body()).getAsJsonArray();
            if (versions.isEmpty()) return null;

            JsonObject ver = versions.get(0).getAsJsonObject();
            JsonArray files = ver.getAsJsonArray("files");
            if (files == null || files.isEmpty()) return null;

            // Prefer primary file
            for (JsonElement f : files) {
                JsonObject file = f.getAsJsonObject();
                if (file.has("primary") && file.get("primary").getAsBoolean()) {
                    return file.get("url").getAsString();
                }
            }
            return files.get(0).getAsJsonObject().get("url").getAsString();
        } catch (Exception e) {
            PixelForgeClient.LOGGER.warn("Modrinth version lookup failed: {}", e.getMessage());
            return null;
        }
    }
}
