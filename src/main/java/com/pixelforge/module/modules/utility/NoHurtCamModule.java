package com.pixelforge.module.modules.utility;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class NoHurtCamModule extends Module {

    public NoHurtCamModule() {
        super("NoHurtCam", "Disables the camera shake when taking damage", Category.UTILITY);
        setEnabled(true);
    }
}
