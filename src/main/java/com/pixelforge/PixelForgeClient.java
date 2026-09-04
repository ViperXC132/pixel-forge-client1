package com.pixelforge;

import com.pixelforge.config.ConfigManager;
import com.pixelforge.event.EventBus;
import com.pixelforge.hud.HudRenderer;
import com.pixelforge.keybind.KeybindManager;
import com.pixelforge.module.ModuleManager;
import com.pixelforge.util.NotificationManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PixelForgeClient implements ClientModInitializer {
    public static final String MOD_ID = "pixelforge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static PixelForgeClient INSTANCE;
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private KeybindManager keybindManager;
    private HudRenderer hudRenderer;
    private NotificationManager notificationManager;
    private EventBus eventBus;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        LOGGER.info("Initializing PixelForge Client for Minecraft 1.21.11");
        eventBus = new EventBus();
        moduleManager = new ModuleManager();
        configManager = new ConfigManager();
        keybindManager = new KeybindManager();
        hudRenderer = new HudRenderer();
        notificationManager = new NotificationManager();
        moduleManager.init();
        configManager.loadAll();
        keybindManager.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            moduleManager.onTick();
            if (client.getWindow() != null) {
                try {
                    client.getWindow().setTitle("PixelForge 1.21.11");
                } catch (Throwable ignored) {}
            }
        });

        HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
            hudRenderer.render(graphics, tickCounter.getTickProgress(false));
        });

        LOGGER.info("PixelForge ready — {} modules loaded", moduleManager.getModules().size());
    }

    public static PixelForgeClient getInstance() { return INSTANCE; }
    public ModuleManager getModuleManager() { return moduleManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public KeybindManager getKeybindManager() { return keybindManager; }
    public HudRenderer getHudRenderer() { return hudRenderer; }
    public NotificationManager getNotificationManager() { return notificationManager; }
    public EventBus getEventBus() { return eventBus; }
}
