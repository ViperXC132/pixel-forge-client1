package com.pixelforge;

import com.pixelforge.config.ConfigManager;
import com.pixelforge.config.ProfileManager;
import com.pixelforge.event.EventBus;
import com.pixelforge.keybind.KeybindManager;
import com.pixelforge.module.ModuleManager;
import com.pixelforge.hud.HudRenderer;
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
        this.eventBus = new EventBus();
        this.moduleManager = new ModuleManager();
        this.configManager = new ConfigManager();
        this.keybindManager = new KeybindManager();
        this.hudRenderer = new HudRenderer();
        this.notificationManager = new NotificationManager();
        moduleManager.init();
        configManager.loadAll();
        keybindManager.init();
        boolean anyEnabled = moduleManager.getModules().stream().anyMatch(m -> m.isEnabled() && m.getCategory() != com.pixelforge.module.Category.SYSTEM);
        if (!anyEnabled) ProfileManager.loadProfile("Default");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                moduleManager.onTick();
                notificationManager.tick();
            }
        });

        HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
            hudRenderer.render(graphics, tickCounter.getTickProgress(false));
            notificationManager.render(graphics);
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
