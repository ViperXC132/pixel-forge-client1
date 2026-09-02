package com.pixelforge.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientAccessor {
    @Shadow @Final @Mutable private Session session;

    public void pixelforge$setSession(Session session) {
        this.session = session;
    }
}
