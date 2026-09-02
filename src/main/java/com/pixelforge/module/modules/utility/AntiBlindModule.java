package com.pixelforge.module.modules.utility;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import net.minecraft.entity.effect.StatusEffects;

public class AntiBlindModule extends Module {

    public AntiBlindModule() {
        super("AntiBlind", "Removes blindness and darkness effects visually", Category.UTILITY);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.player == null) return;
        if (mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) {
            mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
        }
        if (mc.player.hasStatusEffect(StatusEffects.DARKNESS)) {
            mc.player.removeStatusEffect(StatusEffects.DARKNESS);
        }
    }
}
