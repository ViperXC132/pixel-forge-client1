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

/** Persistent settings — module enabled state IS restored on every launch. */
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("pixelforge");

    public ConfigManager() {
        try { Files.createDirectories(CONFIG_DIR); }
        catch (IOException e) { PixelForgeClient.LOGGER.error("Failed to create config directory", e); }
    }

    public void loadAll() {
        for (Module module : PixelForgeClient.getInstance().getModuleManager().getModules())
            loadModule(module);
    }

    public static void loadModule(Module module) {
        Path file = pathFor(module);
        if (!Files.exists(file)) return;
        try {
            JsonObject obj = JsonParser.parseString(Files.readString(file)).getAsJsonObject();

            // Restore enabled state — this is what was missing before
            if (obj.has("enabled")) {
                boolean savedEnabled = obj.get("enabled").getAsBoolean();
                if (savedEnabled != module.isEnabled())
                    module.setEnabledSilent(savedEnabled);
            }

            if (obj.has("keybind")) module.setKeybind(obj.get("keybind").getAsInt());

            if (obj.has("settings") && obj.get("settings").isJsonObject()) {
                JsonObject settings = obj.getAsJsonObject("settings");
                for (Module.Setting<?> setting : module.getSettings())
                    if (settings.has(setting.getName()))
                        applySetting(setting, settings.get(setting.getName()));
            }
        } catch (Exception e) {
            PixelForgeClient.LOGGER.warn("Failed to load config for {}", module.getName(), e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void applySetting(Module.Setting setting, com.google.gson.JsonElement value) {
        try {
            Object current = setting.get(), converted;
            if      (current instanceof Boolean) converted = value.getAsBoolean();
            else if (current instanceof Integer) converted = value.getAsInt();
            else if (current instanceof Long)    converted = value.getAsLong();
            else if (current instanceof Double)  converted = value.getAsDouble();
            else if (current instanceof Float)   converted = value.getAsFloat();
            else if (current instanceof Short)   converted = value.getAsShort();
            else if (current instanceof Byte)    converted = value.getAsByte();
            else if (current instanceof String)  converted = value.getAsString();
            else return;
            setting.set(converted);
        } catch (RuntimeException ignored) {}
    }

    public static void saveModule(Module module) {
        Path file = pathFor(module);
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("enabled", module.isEnabled());
            obj.addProperty("keybind", module.getKeybind());
            JsonObject settings = new JsonObject();
            for (Module.Setting<?> setting : module.getSettings()) {
                Object val = setting.get();
                if      (val instanceof Boolean b) settings.addProperty(setting.getName(), b);
                else if (val instanceof Number  n) settings.addProperty(setting.getName(), n);
                else if (val instanceof String  s) settings.addProperty(setting.getName(), s);
                else if (val != null)              settings.addProperty(setting.getName(), String.valueOf(val));
            }
            obj.add("settings", settings);
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(tmp, GSON.toJson(obj));
            try {
                Files.move(tmp, file,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(tmp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            PixelForgeClient.LOGGER.error("Failed to save config for {}", module.getName(), e);
        }
    }

    public void saveAll() {
        for (Module module : PixelForgeClient.getInstance().getModuleManager().getModules())
            saveModule(module);
    }

    private static Path pathFor(Module module) {
        return CONFIG_DIR.resolve(module.getName().toLowerCase().replaceAll("[^a-z0-9]+", "_") + ".json");
    }
}
