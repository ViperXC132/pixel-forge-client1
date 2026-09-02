package com.pixelforge.module.modules.visual;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/** Reliable Fullbright: gamma plus a client-side night-vision fallback. */
public class FullbrightModule extends Module {
    private double previousGamma = 1.0;
    private boolean suppliedNightVision;
    private final Setting<Boolean> nightVision = addSetting(new Setting<>("Night vision fallback", true));
    private final Setting<Boolean> forceGamma = addSetting(new Setting<>("Force gamma", true));

    public FullbrightModule() {
        super("Fullbright", "Maximum client-side brightness with a reliable fallback", Category.VISUAL);
    }

    @Override public void onEnable() {
        if (mc == null || mc.options == null) return;
        previousGamma = mc.options.getGamma().getValue();
        if (forceGamma.get()) mc.options.getGamma().setValue(16.0);
        applyNightVision();
    }

    private void applyNightVision() {
        if (!nightVision.get() || mc == null || mc.player == null) return;
        if (mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) return;
        mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 260, 0, false, false, false));
        suppliedNightVision = true;
    }

    @Override public void onTick() {
        if (!isEnabled() || mc == null) return;
        if (forceGamma.get() && mc.options != null && mc.options.getGamma().getValue() < 16.0) mc.options.getGamma().setValue(16.0);
        if (mc.player != null && nightVision.get()) {
            var effect = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (effect == null || effect.getDuration() < 100) applyNightVision();
        }
    }

    @Override public void onDisable() {
        if (mc == null) return;
        if (mc.options != null) mc.options.getGamma().setValue(previousGamma);
        if (mc.player != null && suppliedNightVision) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            suppliedNightVision = false;
        }
    }
}
