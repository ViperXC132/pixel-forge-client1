package com.pixelforge.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared HUD layout positions used by the in-game renderer and HUD editor.
 * Layouts are loaded once after client initialization and use deterministic defaults,
 * so enabling/disabling another module can never reshuffle existing elements.
 */
public final class HudRenderer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("pixelforge/hud_layout.json");
    private static final Type TYPE = new TypeToken<Map<String, int[]>>() {}.getType();
    private static final Map<String, int[]> POSITIONS = new LinkedHashMap<>();
    private static boolean loaded;

    public HudRenderer() {
        loadPositions();
    }

    public static int getX(String name) { return position(name)[0]; }
    public static int getY(String name) { return position(name)[1]; }

    public static int[] position(String name) {
        int[] saved = POSITIONS.get(name);
        if (saved == null || saved.length < 2) {
            saved = defaultPosition(name);
            POSITIONS.put(name, saved);
        }
        return saved;
    }

    public static void setPosition(String name, int x, int y) {
        POSITIONS.put(name, new int[]{Math.max(2, x), Math.max(2, y)});
    }

    private static int[] defaultPosition(String name) {
        int index = 0;
        try {
            var manager = PixelForgeClient.getInstance() == null ? null : PixelForgeClient.getInstance().getModuleManager();
            if (manager != null) {
                for (Module module : manager.getModules()) {
                    if (module.getCategory() != Category.HUD) continue;
                    if (module.getName().equals(name)) break;
                    index++;
                }
            }
        } catch (Throwable ignored) {}
        int column = index / 8;
        int row = index % 8;
        return new int[]{8 + column * 150, 8 + row * 24};
    }

    public static void resetLayout() {
        POSITIONS.clear();
        if (PixelForgeClient.getInstance() != null && PixelForgeClient.getInstance().getModuleManager() != null) {
            for (Module module : PixelForgeClient.getInstance().getModuleManager().getModules()) {
                if (module.getCategory() == Category.HUD) position(module.getName());
            }
        }
    }

    public static void savePositions() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(POSITIONS));
        } catch (Exception e) {
            if (PixelForgeClient.LOGGER != null) PixelForgeClient.LOGGER.warn("Failed to save HUD layout", e);
        }
    }

    public static void loadPositions() {
        if (loaded) return;
        loaded = true;
        try {
            if (!Files.exists(FILE)) return;
            Map<String, int[]> loadedMap = GSON.fromJson(Files.readString(FILE), TYPE);
            POSITIONS.clear();
            if (loadedMap != null) {
                for (var entry : loadedMap.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null && entry.getValue().length >= 2) {
                        POSITIONS.put(entry.getKey(), new int[]{Math.max(2, entry.getValue()[0]), Math.max(2, entry.getValue()[1])});
                    }
                }
            }
        } catch (Exception e) {
            POSITIONS.clear();
            if (PixelForgeClient.LOGGER != null) PixelForgeClient.LOGGER.warn("Failed to load HUD layout; using defaults", e);
        }
    }

    public void render(DrawContext context, float tickDelta) {
        PixelForgeClient client = PixelForgeClient.getInstance();
        if (client == null || client.getModuleManager() == null) return;
        for (Module module : client.getModuleManager().getModules()) {
            if (!module.isEnabled() || module.getCategory() != Category.HUD) continue;
            try {
                module.onRender(context, tickDelta);
            } catch (Throwable t) {
                PixelForgeClient.LOGGER.error("HUD module {} failed", module.getName(), t);
            }
        }
    }
}
