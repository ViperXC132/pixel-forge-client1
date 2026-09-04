package com.pixelforge.util;

import net.minecraft.client.gui.DrawContext;

/** Disabled — Lunar clients stay silent. Kept for API compatibility. */
public class NotificationManager {
    public void push(String message, int color) { /* no-op */ }
    public void tick() { /* no-op */ }
    public void render(DrawContext graphics) { /* no-op */ }
}
