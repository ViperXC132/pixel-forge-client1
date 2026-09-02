package com.pixelforge.module.modules.hud;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

public class PotionEffectsModule extends Module {

    private int x = 4;
    private int y = 140;

    public PotionEffectsModule() {
        super("Potion Effects", "Lists active potion effects with duration", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.player == null || mc.textRenderer == null) return;

        int offset = 0;
        for (StatusEffectInstance effect : mc.player.getStatusEffects()) {
            String name = effect.getEffectType().value().getName().getString();
            int amp = effect.getAmplifier() + 1;
            int duration = effect.getDuration() / 20;
            int mins = duration / 60;
            int secs = duration % 60;

            String text = String.format("%s %d %02d:%02d", name, amp, mins, secs);
            int color = effect.getEffectType().value().isBeneficial() ? 0xFF55FF55 : 0xFFFF5555;

            RenderUtil.drawText(context, mc.textRenderer, text, x, y + offset, color, true);
            offset += 10;
        }
    }
}
