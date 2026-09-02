package com.pixelforge.module.modules.utility;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.HashSet;
import java.util.Set;

public class PotionAlertModule extends Module {

    private final Set<String> warned = new HashSet<>();

    public PotionAlertModule() {
        super("PotionAlert", "Notifies when important potion effects are about to expire", Category.UTILITY);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.player == null) return;

        Set<String> current = new HashSet<>();
        for (StatusEffectInstance effect : mc.player.getStatusEffects()) {
            String key = effect.getEffectType().value().getName().getString();
            current.add(key);
            int secs = effect.getDuration() / 20;
            if (secs <= 10 && secs > 0 && !warned.contains(key)) {
                PixelForgeClient.getInstance().getNotificationManager()
                        .push(key + " expiring in " + secs + "s", 0xFFFFAA00);
                warned.add(key);
            }
        }
        warned.retainAll(current);
    }
}
