package com.pixelforge.module.modules.visual;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class BlockOutlineModule extends Module {

    private int outlineColor = 0xFFFFFFFF;
    private float lineWidth = 2.0f;

    public BlockOutlineModule() {
        super("Block Outline", "Custom block selection outline color and thickness", Category.VISUAL);
    }

    public int getOutlineColor() { return outlineColor; }
    public float getLineWidth() { return lineWidth; }
}
