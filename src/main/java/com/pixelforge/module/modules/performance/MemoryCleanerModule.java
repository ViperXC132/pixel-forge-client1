package com.pixelforge.module.modules.performance;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class MemoryCleanerModule extends Module {

    private long lastClean = 0;

    public MemoryCleanerModule() {
        super("Memory Cleaner", "Periodically suggests GC and cleans caches", Category.PERFORMANCE);
    }

    @Override
    public void onTick() {
        long now = System.currentTimeMillis();
        if (now - lastClean > 120_000) { // every 2 minutes
            System.gc();
            lastClean = now;
            PixelForgeClient.getInstance().getNotificationManager()
                    .push("Memory cleaned", 0xFF55FF55);
        }
    }
}
