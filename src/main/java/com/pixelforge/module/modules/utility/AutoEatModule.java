package com.pixelforge.module.modules.utility;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

public class AutoEatModule extends Module {
    private final int hungerThreshold = 14;

    public AutoEatModule() {
        super("AutoEat", "Automatically eats food when hunger is low", Category.UTILITY);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.player == null) return;
        if (mc.player.getHungerManager().getFoodLevel() > hungerThreshold) return;
        if (mc.player.isUsingItem()) return;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.contains(DataComponentTypes.FOOD)) {
                mc.player.getInventory().setSelectedSlot(i);
                mc.options.useKey.setPressed(true);
                return;
            }
        }
    }
}
