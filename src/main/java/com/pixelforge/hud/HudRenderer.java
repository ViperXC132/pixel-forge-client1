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
import java.util.HashMap;
import java.util.Map;

/**
 * Shared HUD layout positions used by the in-game renderer and HUD editor.
 * Positions are persisted to config/pixelforge/hud_layout.json
 */
public class HudRenderer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("pixelforge/hud_layout.json");
    private static final Map<String, int[]> POSITIONS = new HashMap<>();
    private static int nextX = 8, nextY = 8;
    private static boolean loaded = false;

    static {
        loadPositions();
    }

    public static int getX(String name) {
        return position(name)[0];
    }

    public static int getY(String name) {
        return position(name)[1];
    }

    public static void setPosition(String name, int x, int y) {
        POSITIONS.put(name, new int[]{Math.max(2, x), Math.max(2, y)});
    }

    public static int[] position(String name) {
        return POSITIONS.computeIfAbsent(name, k -> {
            int[] p = {nextX, nextY};
            nextY += 24;
            if (nextY > 180) {
                nextY = 8;
                nextX += 190;
            }
            return p;
        });
    }

    public static void resetLayout() {
        POSITIONS.clear();
        nextX = 8;
        nextY = 8;
    }

    public static void savePositions() {
        try {
            Files.createDirectories(FILE.getParent());
            Map<String, int[]> copy = new HashMap<>(POSITIONS);
            Files.writeString(FILE, GSON.toJson(copy));
        } catch (Exception e) {
            if (PixelForgeClient.LOGGER != null) {
                PixelForgeClient.LOGGER.warn("Failed to save HUD layout", e);
            }
        }
    }

    public static void loadPositions() {
        if (loaded) return;
        loaded = true;
        try {
            if (!Files.exists(FILE)) return;
            String json = Files.readString(FILE);
            Type type = new TypeToken<Map<String, int[]>>() {}.getType();
            Map<String, int[]> loadedMap = GSON.fromJson(json, type);
            if (loadedMap != null) {
                POSITIONS.clear();
                POSITIONS.putAll(loadedMap);
            }
        } catch (Exception e) {
            // ignore corrupt layout
        }
    }

    public void render(DrawContext context, float tickDelta) {
        PixelForgeClient client = PixelForgeClient.getInstance();
        if (client == null || client.getModuleManager() == null) return;
        for (Module module : client.getModuleManager().getModules()) {
            if (!module.isEnabled()) continue;
            if (module.getCategory() != Category.HUD && module.getCategory() != Category.TRAINER) continue;
            try {
                module.onRender(context, tickDelta);
            } catch (Throwable t) {
                PixelForgeClient.LOGGER.error("HUD module {} failed", module.getName(), t);
            }
        }
    }
}
