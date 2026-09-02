package com.pixelforge.module.modules.visual;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

/** Configuration holder for the vanilla block selection outline. */
public class BlockOutlineModule extends Module {
    private int outlineColor=0xFFFFFFFF;
    private float lineWidth=2.0f;
    public BlockOutlineModule(){super("Block Outline","Custom block selection outline color and thickness",Category.VISUAL);}
    public int getOutlineColor(){return outlineColor;}
    public void setOutlineColor(int color){outlineColor=color;}
    public float getLineWidth(){return lineWidth;}
    public void setLineWidth(float width){lineWidth=Math.max(1f,Math.min(6f,width));}
}
