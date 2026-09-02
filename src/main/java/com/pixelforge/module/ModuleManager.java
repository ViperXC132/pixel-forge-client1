package com.pixelforge.module;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.modules.hud.*;
import com.pixelforge.module.modules.visual.*;
import com.pixelforge.module.modules.utility.*;
import com.pixelforge.module.modules.performance.*;
import com.pixelforge.module.modules.movement.*;
import com.pixelforge.module.modules.trainer.*;
import com.pixelforge.module.modules.system.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void init() {
        if (!modules.isEmpty()) return;

        register(new FpsModule()); register(new CpsModule()); register(new CoordsModule());
        register(new ArmorStatusModule()); register(new PotionEffectsModule()); register(new SpeedModule());
        register(new CompassModule()); register(new PingTpsModule()); register(new KeystrokesModule());
        register(new ToggleSprintHudModule()); register(new ModListModule()); register(new BiomeChunkModule());
        register(new LightLevelModule()); register(new MemoryEntityModule()); register(new ReachModule());

        register(new FullbrightModule()); register(new CustomNametagsModule()); register(new HitColorModule());
        register(new TimeWeatherModule()); register(new CustomCrosshairModule()); register(new MobHealthbarsModule());
        register(new BlockOutlineModule()); register(new ArmorDurabilityModule()); register(new NoParticlesModule());

        register(new ZoomModule()); register(new FreeLookModule()); register(new AutoRespawnModule());
        register(new AntiBlindModule()); register(new NoHurtCamModule()); register(new BetterChatModule());
        register(new FastPlaceModule()); register(new MidClickModule()); register(new AutoToolModule());
        register(new PotionAlertModule()); register(new BorderAlertModule()); register(new ServerInfoModule());
        register(new TabListModule()); register(new ScreenshotManagerModule());

        register(new ToggleSprintModule()); register(new ToggleSneakModule());

        register(new DynamicFpsModule()); register(new EntityCullingModule()); register(new SmartRenderDistanceModule());
        register(new ParticleLimiterModule()); register(new MemoryCleanerModule()); register(new SmoothChunkLoadingModule());

        register(new KnockbackTrainerModule()); register(new CpsTrainerModule()); register(new AimTrainerModule());
        register(new WTapTrainerModule()); register(new BlockHitTrainerModule()); register(new PingSimulatorModule());
        register(new StrafeTrainerModule());

        register(new ClickGuiModule()); register(new HudEditorModule()); register(new FovChangerModule());
        register(new DiscordRpcModule()); register(new CustomHandModule());
    }

    private void register(Module module) {
        if (getModule(module.getName()) == null) modules.add(module);
    }

    public List<Module> getModules() { return Collections.unmodifiableList(modules); }

    public List<Module> getModulesByCategory(Category category) {
        return modules.stream().filter(m -> m.getCategory() == category).collect(Collectors.toList());
    }

    public Module getModule(String name) {
        return modules.stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        return modules.stream().filter(clazz::isInstance).map(clazz::cast).findFirst().orElse(null);
    }

    public void onTick() {
        for (Module module : modules) {
            if (!module.isEnabled()) continue;
            try {
                module.onTick();
            } catch (Throwable t) {
                PixelForgeClient.LOGGER.error("Module {} failed during tick; disabling it", module.getName(), t);
                try { module.setEnabled(false); } catch (Throwable ignored) {}
            }
        }
    }
}
