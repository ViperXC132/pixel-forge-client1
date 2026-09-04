package com.pixelforge.module.modules.hud;

import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class BiomeChunkModule extends Module {
    public BiomeChunkModule() {
        super("Biome Chunk", "Shows biome and chunk coordinates", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.player == null || mc.world == null || mc.textRenderer == null) return;
        BlockPos pos = mc.player.getBlockPos();
        ChunkPos chunk = new ChunkPos(pos);
        String biome = mc.world.getBiome(pos).getKey().map(k -> k.getValue().getPath()).orElse("?");
        String text = String.format("Biome: %s | Chunk: %d %d", biome, chunk.x, chunk.z);
        RenderUtil.drawHudBox(context, mc.textRenderer, text, HudRenderer.getX(getName()), HudRenderer.getY(getName()), 0xFF55FF55);
    }
}
