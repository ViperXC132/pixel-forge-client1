package com.pixelforge.module.modules.hud;

import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.ArrayList;
import java.util.List;

public class PotionEffectsModule extends Module {
    public PotionEffectsModule() {
        super("Potion Effects", "Lists active potion effects with duration", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.player == null || mc.textRenderer == null) return;
        List<String> lines = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (StatusEffectInstance effect : mc.player.getStatusEffects()) {
            String name = effect.getEffectType().value().getName().getString();
            int amp = effect.getAmplifier() + 1;
            int duration = effect.getDuration() / 20;
            int mins = duration / 60;
            int secs = duration % 60;
            lines.add(String.format("%s %d (%d:%02d)", name, amp, mins, secs));
            colors.add(effect.getEffectType().value().isBeneficial() ? 0xFF55FF55 : 0xFFFF5555);
        }
        if (lines.isEmpty()) return;
        int[] cols = colors.stream().mapToInt(Integer::intValue).toArray();
        RenderUtil.drawHudBox(context, mc.textRenderer, lines.toArray(new String[0]), cols,
                HudRenderer.getX(getName()), HudRenderer.getY(getName()));
    }
}
