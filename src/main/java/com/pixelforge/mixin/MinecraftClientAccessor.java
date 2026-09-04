package com.pixelforge.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Proper accessor interface for swapping the live Minecraft session.
 * Using @Accessor + @Mutable so the final session field can be replaced at runtime.
 */
@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {

    @Accessor("session")
    Session pixelforge$getSession();

    @Mutable
    @Accessor("session")
    void pixelforge$setSession(Session session);
}
