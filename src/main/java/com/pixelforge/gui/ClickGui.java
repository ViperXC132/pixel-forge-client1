package com.pixelforge.gui;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.config.ConfigManager;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.KeyUtil;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** PixelForge's clean Lunar-inspired module browser. */
public final class ClickGui extends Screen {
    private static final int PANEL_W = 760, PANEL_H = 500, SIDEBAR_W = 150;
    private final List<Category> categories = new ArrayList<>();
    private final Map<String, Boolean> expanded = new HashMap<>();
    private Category selectedCategory;
    private Module expandedModule;
    private TextFieldWidget searchField;
    private int scroll;
    private boolean waitingForKey;
    private boolean draggingSlider;
    private Module.Setting<?> draggedSetting;
    private int left, top;

    public ClickGui() {
        super(Text.literal("PixelForge"));
        for (Category c : Category.values()) if (c != Category.SYSTEM) categories.add(c);
        if (!categories.isEmpty()) selectedCategory = categories.get(0);
    }

    @Override
    protected void init() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;
        searchField = new TextFieldWidget(textRenderer, left + SIDEBAR_W + 18, top + 20, PANEL_W - SIDEBAR_W - 36, 24, Text.literal("Search"));
        searchField.setPlaceholder(Text.literal("Search modules..."));
        searchField.setMaxLength(40);
        addDrawableChild(searchField);
    }

    private List<Module> modules() {
        if (selectedCategory == null) return List.of();
        String q = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        return PixelForgeClient.getInstance().getModuleManager().getModulesByCategory(selectedCategory).stream()
                .filter(m -> q.isEmpty() || m.getName().toLowerCase(Locale.ROOT).contains(q) || m.getDescription().toLowerCase(Locale.ROOT).contains(q))
                .toList();
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        left = (width - PANEL_W) / 2; top = (height - PANEL_H) / 2;
        if (searchField != null) { searchField.setX(left + SIDEBAR_W + 18); searchField.setY(top + 20); searchField.setWidth(PANEL_W - SIDEBAR_W - 36); }
        RenderUtil.fill(c, 0, 0, width, height, 0x78000000);
        RenderUtil.drawRoundedPanel(c, left, top, PANEL_W, PANEL_H, 0xEE101010, 0x60FFFFFF);
        RenderUtil.fill(c, left, top, left + SIDEBAR_W, top + PANEL_H, 0x22000000);
        RenderUtil.drawText(c, textRenderer, "PIXELFORGE", left + 18, top + 18, 0xFFFFFFFF, false);
        RenderUtil.drawText(c, textRenderer, "CLIENT", left + 18, top + 32, 0xFF777777, false);

        int cy = top + 62;
        for (Category cat : categories) {
            boolean on = cat == selectedCategory;
            boolean hover = mx >= left + 10 && mx <= left + SIDEBAR_W - 10 && my >= cy && my <= cy + 28;
            if (on || hover) RenderUtil.fill(c, left + 10, cy, left + SIDEBAR_W - 10, cy + 28, on ? 0x42FFFFFF : 0x20FFFFFF);
            RenderUtil.drawText(c, textRenderer, cat.getDisplayName(), left + 22, cy + 9, 0xFFFFFFFF, false);
            cy += 31;
        }

        int contentLeft = left + SIDEBAR_W + 18;
        RenderUtil.drawText(c, textRenderer, selectedCategory == null ? "Modules" : selectedCategory.getDisplayName(), contentLeft, top + 54, 0xFFFFFFFF, false);
        RenderUtil.drawText(c, textRenderer, modules().size() + " modules", contentLeft + 100, top + 54, 0xFF777777, false);

        int listTop = top + 70, listBottom = top + PANEL_H - 52, listW = PANEL_W - SIDEBAR_W - 36;
        List<Module> list = modules();
        int total = list.stream().mapToInt(this::rowHeight).sum();
        int maxScroll = Math.max(0, total - (listBottom - listTop));
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        c.enableScissor(contentLeft, listTop, contentLeft + listW, listBottom);
        int y = listTop - scroll;
        for (Module m : list) { int h = rowHeight(m); if (y + h >= listTop && y <= listBottom) drawModule(c, m, contentLeft, y, listW, mx, my); y += h; }
        c.disableScissor();

        int fy = top + PANEL_H - 38;
        footerButton(c, left + SIDEBAR_W + 18, fy, 112, 24, "HUD Editor", mx, my);
        footerButton(c, left + SIDEBAR_W + 136, fy, 90, 24, "Presets", mx, my);
        footerButton(c, left + PANEL_W - 118, fy, 100, 24, "Close", mx, my);
        super.render(c, mx, my, d);
    }

    private int rowHeight(Module m) { return expanded.getOrDefault(m.getName(), false) ? 42 + m.getSettings().size() * 22 : 38; }

    private void drawModule(DrawContext c, Module m, int x, int y, int w, int mx, int my) {
        boolean open = expanded.getOrDefault(m.getName(), false);
        boolean hover = mx >= x && mx <= x + w && my >= y && my < y + 36;
        int h = rowHeight(m);
        RenderUtil.fill(c, x, y, x + w, y + h - 4, hover || open ? 0x2EFFFFFF : 0x18FFFFFF);
        RenderUtil.drawBorder(c, x, y, w, h - 4, open ? 0x75FFFFFF : 0x35FFFFFF);
        RenderUtil.drawText(c, textRenderer, m.getName(), x + 12, y + 8, 0xFFFFFFFF, false);
        RenderUtil.drawText(c, textRenderer, m.getDescription(), x + 12, y + 21, 0xFF8D8D8D, false);
        int tx = x + w - 42;
        RenderUtil.fill(c, tx, y + 10, tx + 26, y + 22, m.isEnabled() ? 0x55FFFFFF : 0x18FFFFFF);
        RenderUtil.drawBorder(c, tx, y + 10, 26, 12, m.isEnabled() ? 0xFFFFFFFF : 0x45FFFFFF);
        RenderUtil.drawText(c, textRenderer, m.isEnabled() ? "ON" : "OFF", tx + 5, y + 12, 0xFFFFFFFF, false);
        if (!open) return;
        int sy = y + 42;
        chip(c, x + 10, sy, w - 20, "Keybind", waitingForKey && expandedModule == m ? "PRESS KEY" : KeyUtil.name(m.getKeybind()), false);
        sy += 22;
        for (Module.Setting<?> s : m.getSettings()) {
            Object v = s.get();
            if (v instanceof Boolean b) chip(c, x + 10, sy, w - 20, s.getName(), b ? "ON" : "OFF", b);
            else if (v instanceof Number n) slider(c, x + 10, sy, w - 20, s, n);
            else chip(c, x + 10, sy, w - 20, s.getName(), String.valueOf(v), false);
            sy += 22;
        }
    }

    private void chip(DrawContext c, int x, int y, int w, String name, String value, boolean on) {
        RenderUtil.drawText(c, textRenderer, name, x + 2, y + 4, 0xFF9A9A9A, false);
        int vw = Math.max(48, textRenderer.getWidth(value) + 12), vx = x + w - vw;
        RenderUtil.fill(c, vx, y, vx + vw, y + 16, on ? 0x38FFFFFF : 0x18FFFFFF);
        RenderUtil.drawBorder(c, vx, y, vw, 16, on ? 0xFFFFFFFF : 0x38FFFFFF);
        RenderUtil.drawText(c, textRenderer, value, vx + 6, y + 4, 0xFFFFFFFF, false);
    }

    private void slider(DrawContext c, int x, int y, int w, Module.Setting<?> s, Number n) {
        RenderUtil.drawText(c, textRenderer, s.getName(), x + 2, y + 2, 0xFF9A9A9A, false);
        int bx = x + 130, bw = w - 180, by = y + 8;
        double ratio = (n.doubleValue() - s.getMin()) / Math.max(0.0001, s.getMax() - s.getMin());
        int knob = bx + (int)(bw * Math.max(0, Math.min(1, ratio)));
        RenderUtil.fill(c, bx, by, bx + bw, by + 2, 0x45FFFFFF);
        RenderUtil.fill(c, bx, by, knob, by + 2, 0xFFFFFFFF);
        RenderUtil.fill(c, knob - 2, by - 2, knob + 2, by + 4, 0xFFFFFFFF);
        String value = (n instanceof Integer || n instanceof Long) ? String.valueOf(n.longValue()) : String.format(Locale.ROOT, "%.1f", n.doubleValue());
        RenderUtil.drawText(c, textRenderer, value, x + w - 38, y + 2, 0xFFFFFFFF, false);
    }

    private void footerButton(DrawContext c, int x, int y, int w, int h, String text, int mx, int my) {
        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + h;
        RenderUtil.fill(c, x, y, x + w, y + h, hover ? 0x42FFFFFF : 0x1CFFFFFF);
        RenderUtil.drawBorder(c, x, y, w, h, hover ? 0xAAFFFFFF : 0x42FFFFFF);
        RenderUtil.drawCenteredText(c, textRenderer, text, x + w / 2, y + 7, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y(); int b = click.button();
        int fy = top + PANEL_H - 38;
        if (b == 0 && mx >= left + SIDEBAR_W + 18 && mx <= left + SIDEBAR_W + 130 && my >= fy && my <= fy + 24) { client.setScreen(new HudEditor()); return true; }
        if (b == 0 && mx >= left + SIDEBAR_W + 136 && mx <= left + SIDEBAR_W + 226 && my >= fy && my <= fy + 24) { client.setScreen(new PresetsScreen()); return true; }
        if (b == 0 && mx >= left + PANEL_W - 118 && mx <= left + PANEL_W - 18 && my >= fy && my <= fy + 24) { close(); return true; }

        int cy = top + 62;
        for (Category cat : categories) {
            if (b == 0 && mx >= left + 10 && mx <= left + SIDEBAR_W - 10 && my >= cy && my <= cy + 28) { selectedCategory = cat; expanded.clear(); expandedModule = null; scroll = 0; return true; }
            cy += 31;
        }

        int contentLeft = left + SIDEBAR_W + 18, listTop = top + 70, listBottom = top + PANEL_H - 52, listW = PANEL_W - SIDEBAR_W - 36;
        if (mx >= contentLeft && mx <= contentLeft + listW && my >= listTop && my <= listBottom) {
            int y = listTop - scroll;
            for (Module m : modules()) {
                int h = rowHeight(m);
                if (my >= y && my < y + h) {
                    if (my < y + 36) {
                        if (b == 1) { m.toggle(); return true; }
                        if (b == 0) { boolean was = expanded.getOrDefault(m.getName(), false); expanded.clear(); expanded.put(m.getName(), !was); expandedModule = was ? null : m; return true; }
                    } else if (b == 0 && expanded.getOrDefault(m.getName(), false)) {
                        int sy = y + 42;
                        if (my >= sy && my < sy + 22) { waitingForKey = true; expandedModule = m; setFocused(null); return true; }
                        sy += 22;
                        for (Module.Setting<?> s : m.getSettings()) {
                            if (my >= sy && my < sy + 22) {
                                Object v = s.get();
                                if (v instanceof Boolean bool) { setBoolean(s, !bool); return true; }
                                if (v instanceof Number) { draggingSlider = true; draggedSetting = s; setNumberFromMouse(s, mx, contentLeft + 10, listW - 20); return true; }
                            }
                            sy += 22;
                        }
                    }
                    return true;
                }
                y += h;
            }
        }
        if (searchField != null && searchField.mouseClicked(click, doubled)) { setFocused(searchField); return true; }
        return super.mouseClicked(click, doubled);
    }

    @SuppressWarnings("unchecked") private void setBoolean(Module.Setting<?> s, boolean value) { ((Module.Setting<Boolean>)s).set(value); if (expandedModule != null) ConfigManager.saveModule(expandedModule); }
    @SuppressWarnings("unchecked") private void setNumberFromMouse(Module.Setting<?> s, double mouseX, int bx, int bw) { double ratio=Math.max(0,Math.min(1,(mouseX-bx)/(double)Math.max(1,bw))); double nv=s.getMin()+(s.getMax()-s.getMin())*ratio; Object v=s.get(); if(v instanceof Integer)((Module.Setting<Integer>)s).set((int)Math.round(nv)); else if(v instanceof Long)((Module.Setting<Long>)s).set(Math.round(nv)); else if(v instanceof Float)((Module.Setting<Float>)s).set((float)nv); else if(v instanceof Double)((Module.Setting<Double>)s).set(nv); if(expandedModule!=null)ConfigManager.saveModule(expandedModule); }
    @Override public boolean mouseDragged(Click click,double dx,double dy){if(draggingSlider&&draggedSetting!=null){setNumberFromMouse(draggedSetting,click.x(),left+SIDEBAR_W+28,PANEL_W-SIDEBAR_W-56);return true;}return super.mouseDragged(click,dx,dy);}
    @Override public boolean mouseReleased(Click click){draggingSlider=false;draggedSetting=null;return super.mouseReleased(click);}
    @Override public boolean mouseScrolled(double mx,double my,double h,double v){int listTop=top+70,listBottom=top+PANEL_H-52;if(mx>=left+SIDEBAR_W&&mx<=left+PANEL_W&&my>=listTop&&my<=listBottom){scroll=Math.max(0,scroll-(int)(v*24));return true;}return super.mouseScrolled(mx,my,h,v);}
    @Override public boolean keyPressed(KeyInput i){if(i.key()==256){if(waitingForKey){waitingForKey=false;return true;}close();return true;}if(waitingForKey&&expandedModule!=null){if(i.key()!=256){expandedModule.setKeybind(i.key());ConfigManager.saveModule(expandedModule);}waitingForKey=false;return true;}if(searchField!=null&&searchField.isFocused()&&searchField.keyPressed(i))return true;return super.keyPressed(i);}
    @Override public boolean charTyped(CharInput i){if(searchField!=null&&searchField.isFocused()&&searchField.charTyped(i))return true;return super.charTyped(i);}
    @Override public boolean shouldPause(){return false;}
    @Override public void close(){if(client!=null)client.setScreen(null);}
}
