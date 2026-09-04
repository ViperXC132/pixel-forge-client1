package com.pixelforge.module.modules.hud;

import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ArmorStatusModule extends Module {
    public ArmorStatusModule() {
        super("Armor Status", "Displays armor durability", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.player == null || mc.textRenderer == null) return;
        int x = HudRenderer.getX(getName());
        int y = HudRenderer.getY(getName());
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

        int rows = 0;
        for (EquipmentSlot slot : slots) {
            if (!mc.player.getEquippedStack(slot).isEmpty()) rows++;
        }
        if (rows == 0) return;

        int boxW = 70;
        int boxH = rows * 18 + 6;
        RenderUtil.drawHudBoxFrame(context, x, y, boxW, boxH);

        int offsetY = 3;
        for (EquipmentSlot slot : slots) {
            ItemStack stack = mc.player.getEquippedStack(slot);
            if (stack.isEmpty()) continue;
            context.drawItem(stack, x + 4, y + offsetY);
            if (stack.isDamageable()) {
                int max = stack.getMaxDamage();
                int remaining = max - stack.getDamage();
                float pct = max == 0 ? 1f : remaining / (float) max;
                int color = pct > 0.5f ? 0xFF55FF55 : (pct > 0.25f ? 0xFFFFFF55 : 0xFFFF5555);
                String txt = String.valueOf(remaining);
                RenderUtil.drawText(context, mc.textRenderer, txt, x + 24, y + offsetY + 4, color, false);
            }
            offsetY += 18;
        }
    }
}
