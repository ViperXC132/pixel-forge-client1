package com.pixelforge.module.modules.visual;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class HitColorModule extends Module {

    private int hitColor = 0xFFFF0000;

    public HitColorModule() {
        super("Hit Color", "Changes the color entities flash when hit", Category.VISUAL);
    }

    public int getHitColor() {
        return hitColor;
    }

    public void setHitColor(int hitColor) {
        this.hitColor = hitColor;
    }
}
