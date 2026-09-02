package com.pixelforge.module.modules.performance;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class EntityCullingModule extends Module {

    public EntityCullingModule() {
        super("Entity Culling", "Skips rendering entities that are not visible", Category.PERFORMANCE);
    }
}
