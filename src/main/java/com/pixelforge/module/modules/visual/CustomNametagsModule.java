package com.pixelforge.module.modules.visual;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class CustomNametagsModule extends Module {

    public CustomNametagsModule() {
        super("Custom Nametags", "Enhanced nametags with health and ping", Category.VISUAL);
    }

    // Actual rendering is handled via mixin / event in a full implementation.
    // For now this module acts as a toggle flag that other systems can query.
}
