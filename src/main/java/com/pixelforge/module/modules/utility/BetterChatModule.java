package com.pixelforge.module.modules.utility;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class BetterChatModule extends Module {

    public BetterChatModule() {
        super("BetterChat", "Timestamps, longer history and copy support", Category.UTILITY);
        setEnabled(true);
    }
}
