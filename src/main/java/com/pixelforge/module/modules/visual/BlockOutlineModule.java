package com.pixelforge.module.modules.visual;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class BlockOutlineModule extends Module {
    private final Setting<Integer> red = addSetting(new Setting<>("Red", 255, 0, 255));
    private final Setting<Integer> green = addSetting(new Setting<>("Green", 255, 0, 255));
    private final Setting<Integer> blue = addSetting(new Setting<>("Blue", 255, 0, 255));
    private final Setting<Integer> alpha = addSetting(new Setting<>("Alpha", 180, 30, 255));

    public BlockOutlineModule() {
        super("Block Outline", "Custom block selection outline", Category.VISUAL);
        setEnabled(true);
    }

    public int getOutlineArgb() {
        int a = Math.max(30, Math.min(255, alpha.get()));
        int r = Math.max(0, Math.min(255, red.get()));
        int g = Math.max(0, Math.min(255, green.get()));
        int b = Math.max(0, Math.min(255, blue.get()));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
