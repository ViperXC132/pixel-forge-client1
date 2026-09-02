package com.pixelforge.gui;

import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class HudEditor extends Screen {

    public HudEditor() {
        super(Text.literal("HUD Editor"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderUtil.fill(context, 0, 0, width, height, 0x88000000);
        RenderUtil.drawCenteredText(context, textRenderer, "HUD Editor - Drag elements (coming full drag support)",
                width / 2, 20, 0xFFFFFFFF, true);
        RenderUtil.drawCenteredText(context, textRenderer, "Press ESC to close",
                width / 2, 36, 0xFFAAAAAA, true);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
