package com.pixelforge.config;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.Module;
import com.pixelforge.module.modules.hud.*;
import com.pixelforge.module.modules.movement.ToggleSprintModule;
import com.pixelforge.module.modules.utility.*;
import com.pixelforge.module.modules.visual.*;
import com.pixelforge.module.modules.performance.*;

/**
 * Prebuilt + user profiles.
 * Built-in: PvP, Survival, Minigames, UHC, SMP
 */
public final class ProfileManager {

    private static final String[] NAMES = {"PvP", "Survival", "Minigames", "UHC", "SMP", "Default"};

    private ProfileManager() {}

    public static String[] getProfileNames() {
        return NAMES.clone();
    }

    public static void loadProfile(String name) {
        for (Module m : PixelForgeClient.getInstance().getModuleManager().getModules()) {
            if (m.getCategory() == com.pixelforge.module.Category.SYSTEM) continue;
            m.setEnabled(false);
        }

        switch (name) {
            case "PvP" -> applyPvp();
            case "Survival" -> applySurvival();
            case "Minigames" -> applyMinigames();
            case "UHC" -> applyUhc();
            case "SMP" -> applySmp();
            default -> applyDefault();
        }
    }

    private static void enable(Class<? extends Module> clazz) {
        Module m = PixelForgeClient.getInstance().getModuleManager().getModule(clazz);
        if (m != null) m.setEnabled(true);
    }

    private static void applyPvp() {
        enable(FpsModule.class);
        enable(CpsModule.class);
        enable(CoordsModule.class);
        enable(ArmorStatusModule.class);
        enable(PotionEffectsModule.class);
        enable(KeystrokesModule.class);
        enable(PingTpsModule.class);
        enable(ReachModule.class);
        enable(ToggleSprintModule.class);
        enable(CustomCrosshairModule.class);
        enable(NoHurtCamModule.class);
        enable(FullbrightModule.class);
        enable(PotionAlertModule.class);
        enable(BorderAlertModule.class);
        enable(DynamicFpsModule.class);
    }

    private static void applySurvival() {
        enable(FpsModule.class);
        enable(CoordsModule.class);
        enable(ArmorStatusModule.class);
        enable(PotionEffectsModule.class);
        enable(BiomeChunkModule.class);
        enable(LightLevelModule.class);
        enable(MemoryEntityModule.class);
        enable(CompassModule.class);
        enable(SpeedModule.class);
        enable(ToggleSprintModule.class);
        enable(AutoToolModule.class);
        enable(FullbrightModule.class);
        enable(CustomCrosshairModule.class);
        enable(DynamicFpsModule.class);
        enable(MemoryCleanerModule.class);
    }

    private static void applyMinigames() {
        enable(FpsModule.class);
        enable(CpsModule.class);
        enable(KeystrokesModule.class);
        enable(PingTpsModule.class);
        enable(ToggleSprintModule.class);
        enable(CustomCrosshairModule.class);
        enable(NoHurtCamModule.class);
        enable(FullbrightModule.class);
        enable(DynamicFpsModule.class);
    }

    private static void applyUhc() {
        enable(FpsModule.class);
        enable(CpsModule.class);
        enable(CoordsModule.class);
        enable(ArmorStatusModule.class);
        enable(PotionEffectsModule.class);
        enable(KeystrokesModule.class);
        enable(PingTpsModule.class);
        enable(BorderAlertModule.class);
        enable(ReachModule.class);
        enable(ToggleSprintModule.class);
        enable(CustomCrosshairModule.class);
        enable(NoHurtCamModule.class);
        enable(FullbrightModule.class);
        enable(PotionAlertModule.class);
        enable(DynamicFpsModule.class);
    }

    private static void applySmp() {
        enable(FpsModule.class);
        enable(CoordsModule.class);
        enable(ArmorStatusModule.class);
        enable(PotionEffectsModule.class);
        enable(BiomeChunkModule.class);
        enable(CompassModule.class);
        enable(SpeedModule.class);
        enable(ModListModule.class);
        enable(ToggleSprintModule.class);
        enable(AutoToolModule.class);
        enable(BetterChatModule.class);
        enable(CustomCrosshairModule.class);
        enable(FullbrightModule.class);
        enable(DynamicFpsModule.class);
        enable(MemoryCleanerModule.class);
    }

    private static void applyDefault() {
        enable(FpsModule.class);
        enable(CoordsModule.class);
        enable(CustomCrosshairModule.class);
        enable(ToggleSprintModule.class);
        enable(DynamicFpsModule.class);
    }
}
