package com.pixelforge.module.modules.performance;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class DynamicFpsModule extends Module {

    public DynamicFpsModule() {
        super("Dynamic FPS", "Reduces FPS when the game window is not focused", Category.PERFORMANCE);
        setEnabled(true);
    }
}
