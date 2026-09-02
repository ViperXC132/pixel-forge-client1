package com.pixelforge.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.Module;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("pixelforge");

    public ConfigManager() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
        } catch (IOException e) {
            PixelForgeClient.LOGGER.error("Failed to create config directory", e);
        }
    }

    public void loadAll() {
        for (Module module : PixelForgeClient.getInstance().getModuleManager().getModules()) {
            loadModule(module);
        }
    }

    public static void loadModule(Module module) {
        Path file = CONFIG_DIR.resolve(module.getName().toLowerCase().replace(" ", "_") + ".json");
        if (!Files.exists(file)) return;

        try {
            String json = Files.readString(file);
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            if (obj.has("enabled")) {
                boolean enabled = obj.get("enabled").getAsBoolean();
                if (enabled != module.isEnabled()) {
                    module.setEnabled(enabled);
                }
            }
            if (obj.has("keybind")) {
                module.setKeybind(obj.get("keybind").getAsInt());
            }

            if (obj.has("settings")) {
                JsonObject settings = obj.getAsJsonObject("settings");
                for (Module.Setting<?> setting : module.getSettings()) {
                    if (settings.has(setting.getName())) {
                        // Basic type handling
                        Object val = settings.get(setting.getName());
                        // For simplicity we store as string and parse later if needed
                    }
                }
            }
        } catch (Exception e) {
            PixelForgeClient.LOGGER.warn("Failed to load config for {}", module.getName(), e);
        }
    }

    public static void saveModule(Module module) {
        Path file = CONFIG_DIR.resolve(module.getName().toLowerCase().replace(" ", "_") + ".json");
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("enabled", module.isEnabled());
            obj.addProperty("keybind", module.getKeybind());

            JsonObject settings = new JsonObject();
            for (Module.Setting<?> setting : module.getSettings()) {
                Object val = setting.get();
                if (val instanceof Boolean) settings.addProperty(setting.getName(), (Boolean) val);
                else if (val instanceof Number) settings.addProperty(setting.getName(), (Number) val);
                else if (val instanceof String) settings.addProperty(setting.getName(), (String) val);
                else settings.addProperty(setting.getName(), String.valueOf(val));
            }
            obj.add("settings", settings);

            Files.writeString(file, GSON.toJson(obj));
        } catch (IOException e) {
            PixelForgeClient.LOGGER.error("Failed to save config for {}", module.getName(), e);
        }
    }

    public void saveAll() {
        for (Module module : PixelForgeClient.getInstance().getModuleManager().getModules()) {
            saveModule(module);
        }
    }
}
