package com.pixelforge.gui;

import com.pixelforge.config.PresetManager;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/** Minimal, persistent preset manager with save/load/delete controls. */
public final class PresetsScreen extends Screen {
    private TextFieldWidget nameField;
    private String selected;

    public PresetsScreen() { super(Text.literal("Presets")); }

    @Override
    protected void init() {
        nameField = new TextFieldWidget(textRenderer, width / 2 - 130, height / 2 - 130, 260, 20, Text.literal("Preset name"));
        nameField.setPlaceholder(Text.literal("New preset name..."));
        nameField.setMaxLength(32);
        addDrawableChild(nameField);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        int w = 520, h = 360, left = (width - w) / 2, top = (height - h) / 2;
        RenderUtil.fill(c, 0, 0, width, height, 0x72000000);
        RenderUtil.drawRoundedPanel(c, left, top, w, h, 0xE0101010, 0x55FFFFFF);
        RenderUtil.drawText(c, textRenderer, "PRESETS", left + 18, top + 16, 0xFFFFFFFF, false);
        RenderUtil.drawText(c, textRenderer, "Save your exact module setup and restore it later.", left + 18, top + 31, 0xFF9E9E9E, false);

        button(c, left + 18, top + 62, 156, 22, "Save current", mx, my);
        button(c, left + 184, top + 62, 100, 22, "Back", mx, my);

        int y = top + 98;
        for (String name : PresetManager.names()) {
            boolean active = name.equals(selected);
            RenderUtil.fill(c, left + 18, y, left + 502, y + 24, active ? 0x35FFFFFF : 0x1DFFFFFF);
            RenderUtil.drawBorder(c, left + 18, y, 484, 24, active ? 0xAAFFFFFF : 0x35FFFFFF);
            RenderUtil.drawText(c, textRenderer, name, left + 28, y + 7, 0xFFFFFFFF, false);
            button(c, left + 382, y + 3, 54, 18, "Load", mx, my);
            button(c, left + 440, y + 3, 54, 18, "Delete", mx, my);
            y += 30;
            if (y > top + h - 42) break;
        }
        RenderUtil.drawText(c, textRenderer, "Preset data is stored locally in config/pixelforge.", left + 18, top + h - 24, 0xFF777777, false);
        super.render(c, mx, my, d);
    }

    private void button(DrawContext c, int x, int y, int w, int h, String text, int mx, int my) {
        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + h;
        RenderUtil.fill(c, x, y, x + w, y + h, hover ? 0x45FFFFFF : 0x20FFFFFF);
        RenderUtil.drawBorder(c, x, y, w, h, hover ? 0xAAFFFFFF : 0x45FFFFFF);
        RenderUtil.drawCenteredText(c, textRenderer, text, x + w / 2, y + 6, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int w = 520, h = 360, left = (width - w) / 2, top = (height - h) / 2;
        int mx = (int) click.x(), my = (int) click.y();
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        if (nameField != null && nameField.mouseClicked(click, doubled)) { setFocused(nameField); return true; }
        if (mx >= left + 18 && mx <= left + 174 && my >= top + 62 && my <= top + 84) {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) { PresetManager.save(name); selected = name; nameField.setText(""); }
            return true;
        }
        if (mx >= left + 184 && mx <= left + 284 && my >= top + 62 && my <= top + 84) { close(); return true; }
        int y = top + 98;
        for (String name : PresetManager.names()) {
            if (my >= y && my <= y + 24) {
                selected = name;
                if (mx >= left + 382 && mx <= left + 436) PresetManager.apply(name);
                else if (mx >= left + 440 && mx <= left + 494) PresetManager.delete(name);
                return true;
            }
            y += 30;
            if (y > top + h - 42) break;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput i) {
        if (i.key() == 256) { close(); return true; }
        if (nameField != null && nameField.isFocused() && nameField.keyPressed(i)) return true;
        return super.keyPressed(i);
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharInput i) {
        if (nameField != null && nameField.isFocused() && nameField.charTyped(i)) return true;
        return super.charTyped(i);
    }

    @Override public boolean shouldPause() { return false; }
    @Override public void close() { if (client != null) client.setScreen(new ClickGui()); }
}
