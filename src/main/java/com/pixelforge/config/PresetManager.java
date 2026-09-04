package com.pixelforge.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.Module;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Lightweight custom preset slots for the client UI. */
public final class PresetManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("pixelforge/presets.json");
    private static final Map<String, List<String>> PRESETS = new LinkedHashMap<>();
    private static boolean loaded;

    private PresetManager() {}

    public static void load() {
        if (loaded) return;
        loaded = true;
        try {
            if (!Files.exists(FILE)) return;
            Map<String, List<String>> data = GSON.fromJson(Files.readString(FILE), new TypeToken<Map<String, List<String>>>() {}.getType());
            if (data != null) PRESETS.putAll(data);
        } catch (Exception e) {
            PixelForgeClient.LOGGER.warn("Failed to load presets", e);
        }
    }

    public static List<String> names() {
        load();
        return new ArrayList<>(PRESETS.keySet());
    }

    public static void save(String name) {
        if (name == null || name.isBlank()) return;
        load();
        List<String> enabled = new ArrayList<>();
        for (Module module : PixelForgeClient.getInstance().getModuleManager().getModules()) {
            if (module.isEnabled()) enabled.add(module.getName());
        }
        PRESETS.put(name.trim(), enabled);
        persist();
    }

    public static boolean apply(String name) {
        load();
        List<String> enabled = PRESETS.get(name);
        if (enabled == null) return false;
        for (Module module : PixelForgeClient.getInstance().getModuleManager().getModules()) {
            boolean shouldEnable = enabled.contains(module.getName());
            if (module.isEnabled() != shouldEnable) module.setEnabled(shouldEnable);
        }
        return true;
    }

    public static void delete(String name) {
        load();
        PRESETS.remove(name);
        persist();
    }

    private static void persist() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(PRESETS));
        } catch (Exception e) {
            PixelForgeClient.LOGGER.warn("Failed to save presets", e);
        }
    }
}
