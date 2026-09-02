package com.pixelforge.module.modules.hud;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;

public class BiomeChunkModule extends Module {

    private int x = 4;
    private int y = 86;

    public BiomeChunkModule() {
        super("Biome Chunk", "Shows current biome and chunk coordinates", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender(DrawContext context, float tickDelta) {
        if (mc == null || mc.player == null || mc.world == null || mc.textRenderer == null) return;

        BlockPos pos = mc.player.getBlockPos();
        ChunkPos chunk = new ChunkPos(pos);

        RegistryEntry<Biome> biomeEntry = mc.world.getBiome(pos);
        String biome = biomeEntry.getKey().map(k -> k.getValue().getPath()).orElse("unknown");

        String text = String.format("Biome: %s | Chunk: %d %d", biome, chunk.x, chunk.z);
        RenderUtil.drawText(context, mc.textRenderer, text, x, y, 0xFF88FF88, true);
    }
}
