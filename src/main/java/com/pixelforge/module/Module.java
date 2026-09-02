package com.pixelforge.module;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.config.ConfigManager;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {

    protected final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;
    private int keybind;
    private final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = false;
        this.keybind = -1;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            onEnable();
            PixelForgeClient.getInstance().getNotificationManager()
                    .push(name + " enabled", 0x55FF55);
        } else {
            onDisable();
            PixelForgeClient.getInstance().getNotificationManager()
                    .push(name + " disabled", 0xFF5555);
        }
        ConfigManager.saveModule(this);
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    public void onRender(net.minecraft.client.gui.DrawContext context, float tickDelta) {}

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public int getKeybind() { return keybind; }
    public void setKeybind(int keybind) { this.keybind = keybind; }
    public List<Setting<?>> getSettings() { return settings; }

    protected <T> Setting<T> addSetting(Setting<T> setting) {
        settings.add(setting);
        return setting;
    }

    public static class Setting<T> {
        private final String name;
        private T value;
        private final T defaultValue;

        public Setting(String name, T defaultValue) {
            this.name = name;
            this.value = defaultValue;
            this.defaultValue = defaultValue;
        }

        public String getName() { return name; }
        public T get() { return value; }
        public void set(T value) { this.value = value; }
        public T getDefault() { return defaultValue; }
    }
}
