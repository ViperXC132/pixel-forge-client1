package com.pixelforge.mod;

import com.pixelforge.PixelForgeClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Downloads a Modrinth mod jar directly into the Minecraft mods folder.
 */
public final class ModInstaller {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private ModInstaller() {}

    public static void install(ModrinthApi.ModResult mod, Consumer<Boolean> callback) {
        CompletableFuture.runAsync(() -> {
            boolean ok = installSync(mod);
            MinecraftClient.getInstance().execute(() -> callback.accept(ok));
        });
    }

    private static boolean installSync(ModrinthApi.ModResult mod) {
        try {
            String downloadUrl = ModrinthApi.getDownloadUrl(mod.projectId);
            if (downloadUrl == null || downloadUrl.isEmpty()) {
                PixelForgeClient.LOGGER.error("No download URL for {}", mod.title);
                return false;
            }

            Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
            if (!Files.exists(modsDir)) {
                Files.createDirectories(modsDir);
            }

            String fileName = mod.slug + "-pixelforge.jar";
            // Try to keep original filename from URL if possible
            String pathPart = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
            if (pathPart.contains(".jar")) {
                fileName = pathPart.split("\\?")[0];
            }

            Path target = modsDir.resolve(fileName);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(downloadUrl))
                    .header("User-Agent", "PixelForge/1.0.0 (github.com/ViperXC132/pixel-forge-client)")
                    .GET()
                    .timeout(Duration.ofMinutes(2))
                    .build();

            HttpResponse<InputStream> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofInputStream());
            if (res.statusCode() != 200) {
                PixelForgeClient.LOGGER.error("Download failed HTTP {}", res.statusCode());
                return false;
            }

            try (InputStream in = res.body()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            PixelForgeClient.LOGGER.info("Installed {} -> {}", mod.title, target);
            return true;
        } catch (Exception e) {
            PixelForgeClient.LOGGER.error("Install failed for {}", mod.title, e);
            return false;
        }
    }
}
