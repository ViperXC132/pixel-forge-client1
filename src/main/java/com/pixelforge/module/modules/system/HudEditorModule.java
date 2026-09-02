package com.pixelforge.module.modules.system;

import com.pixelforge.gui.HudEditor;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class HudEditorModule extends Module {

    public HudEditorModule() {
        super("HUD Editor", "Drag and drop HUD elements", Category.SYSTEM);
    }

    @Override
    public void onEnable() {
        if (mc != null) {
            mc.setScreen(new HudEditor());
        }
        setEnabled(false);
    }
}
