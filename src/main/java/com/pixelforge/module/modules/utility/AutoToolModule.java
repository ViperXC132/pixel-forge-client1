package com.pixelforge.module.modules.utility;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoToolModule extends Module {
    public AutoToolModule() {
        super("AutoTool", "Switches to the best tool for the block you are mining", Category.UTILITY);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.player == null || mc.world == null) return;
        if (!mc.options.attackKey.isPressed()) return;
        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult bhr = (BlockHitResult) hit;
        BlockState state = mc.world.getBlockState(bhr.getBlockPos());
        int bestSlot = -1;
        float bestSpeed = 1.0f;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        if (bestSlot != -1 && bestSlot != mc.player.getInventory().getSelectedSlot()) {
            mc.player.getInventory().setSelectedSlot(bestSlot);
        }
    }
}
