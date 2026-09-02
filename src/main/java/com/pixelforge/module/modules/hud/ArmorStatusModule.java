package com.pixelforge.module.modules.hud;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ArmorStatusModule extends Module {

    private int x = 4;
    private int y = 52;

    public ArmorStatusModule() {
        super("Armor Status", "Displays armor durability", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.player == null || mc.textRenderer == null) return;

        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        int offsetY = 0;

        for (EquipmentSlot slot : slots) {
            ItemStack stack = mc.player.getEquippedStack(slot);
            if (stack.isEmpty()) continue;

            context.drawItem(stack, x, y + offsetY);

            if (stack.isDamageable()) {
                int max = stack.getMaxDamage();
                int dmg = stack.getDamage();
                int remaining = max - dmg;
                float pct = remaining / (float) max;

                int color = pct > 0.6f ? 0xFF55FF55 : (pct > 0.3f ? 0xFFFFFF55 : 0xFFFF5555);
                String text = remaining + "/" + max;
                RenderUtil.drawText(context, mc.textRenderer, text, x + 20, y + offsetY + 4, color, true);
            }
            offsetY += 18;
        }
    }
}
