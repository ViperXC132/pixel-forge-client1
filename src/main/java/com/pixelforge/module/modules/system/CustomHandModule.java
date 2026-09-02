package com.pixelforge.module.modules.system;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class CustomHandModule extends Module {

    private float handX = 0f;
    private float handY = 0f;
    private float handScale = 1f;

    public CustomHandModule() {
        super("Custom Hand", "Adjust first-person hand position and scale", Category.SYSTEM);
    }

    public float getHandX() { return handX; }
    public float getHandY() { return handY; }
    public float getHandScale() { return handScale; }
}
