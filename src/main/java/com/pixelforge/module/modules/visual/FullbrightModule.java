package com.pixelforge.module.modules.visual;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

/**
 * True render-side fullbright.
 *
 * The lightmap mixin handles lighting while this module only owns the toggle state.
 * No potion/status effects are ever applied and the user's gamma setting is untouched.
 */
public final class FullbrightModule extends Module {
    public FullbrightModule() {
        super("Fullbright", "Render-side maximum lighting without night vision", Category.VISUAL);
    }
}
