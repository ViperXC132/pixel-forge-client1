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

/** Lunar ClickGUI — transparent glass, white text, no purple. */
public class ClickGui extends Screen {
    private final List<Category> categories = new ArrayList<>();
    private Category selectedCategory;
    private Module expandedModule;
    private TextFieldWidget searchField;
    private int moduleScroll;
    private boolean waitingForKey;
    private boolean draggingSlider;
    private Module.Setting<?> draggedSetting;
    private final Map<String, Boolean> expanded = new HashMap<>();

    private static final int PANEL_W = 340;
    private static final int PANEL_H = 400;

    private int left, top;

    public ClickGui() {
        super(Text.literal("PixelForge"));
        for (Category c : Category.values()) {
            if (c != Category.SYSTEM) categories.add(c);
        }
        if (!categories.isEmpty()) selectedCategory = categories.get(0);
    }

    @Override
    protected void init() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;
        searchField = new TextFieldWidget(textRenderer, left + 10, top + 34, PANEL_W - 20, 16, Text.literal("Search"));
        searchField.setPlaceholder(Text.literal("Search..."));
        searchField.setMaxLength(40);
        addDrawableChild(searchField);
    }

    private void layout() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;
        if (searchField != null) {
            searchField.setX(left + 10);
            searchField.setY(top + 34);
            searchField.setWidth(PANEL_W - 20);
        }
    }

    private List<Module> modules() {
        if (selectedCategory == null) return List.of();
        String q = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        return PixelForgeClient.getInstance().getModuleManager().getModulesByCategory(selectedCategory).stream()
                .filter(m -> q.isEmpty()
                        || m.getName().toLowerCase(Locale.ROOT).contains(q)
                        || m.getDescription().toLowerCase(Locale.ROOT).contains(q))
                .toList();
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        layout();
        RenderUtil.fill(c, 0, 0, width, height, 0x66000000);
        RenderUtil.drawRoundedPanel(c, left, top, PANEL_W, PANEL_H, 0xD0101010, 0x55FFFFFF);

        RenderUtil.drawText(c, textRenderer, "PIXELFORGE", left + 12, top + 12, RenderUtil.TEXT, false);
        RenderUtil.drawText(c, textRenderer, "RSHIFT", left + PANEL_W - 48, top + 12, RenderUtil.DIM, false);

        int tabY = top + 54, tabX = left + 10;
        for (Category cat : categories) {
            String label = cat.getDisplayName();
            int tw = textRenderer.getWidth(label) + 10;
            if (tabX + tw > left + PANEL_W - 10) break;
            boolean on = cat == selectedCategory;
            boolean hover = mx >= tabX && mx <= tabX + tw && my >= tabY && my <= tabY + 14;
            RenderUtil.fill(c, tabX, tabY, tabX + tw, tabY + 14, on ? 0x40FFFFFF : (hover ? 0x25FFFFFF : 0x10FFFFFF));
            RenderUtil.drawBorder(c, tabX, tabY, tw, 14, on ? 0xAAFFFFFF : 0x30FFFFFF);
            RenderUtil.drawText(c, textRenderer, label, tabX + 5, tabY + 3, on ? RenderUtil.TEXT : RenderUtil.DIM, false);
            tabX += tw + 3;
        }

        int listTop = top + 74, listBottom = top + PANEL_H - 30;
        int listLeft = left + 8, listRight = left + PANEL_W - 8, contentW = listRight - listLeft;
        List<Module> list = modules();
        int totalH = 0;
        for (Module m : list) totalH += rowHeight(m);
        int maxScroll = Math.max(0, totalH - (listBottom - listTop));
        moduleScroll = Math.max(0, Math.min(moduleScroll, maxScroll));

        c.enableScissor(listLeft, listTop, listRight, listBottom);
        int yy = listTop - moduleScroll;
        for (Module m : list) {
            int h = rowHeight(m);
            if (yy + h >= listTop && yy <= listBottom) drawModuleRow(c, m, listLeft, yy, contentW, mx, my);
            yy += h;
        }
        c.disableScissor();

        boolean hudHover = mx >= left + 10 && mx <= left + 90 && my >= top + PANEL_H - 24 && my <= top + PANEL_H - 8;
        RenderUtil.fill(c, left + 10, top + PANEL_H - 24, left + 90, top + PANEL_H - 8, hudHover ? 0x40FFFFFF : 0x18FFFFFF);
        RenderUtil.drawBorder(c, left + 10, top + PANEL_H - 24, 80, 16, 0x55FFFFFF);
        RenderUtil.drawText(c, textRenderer, "HUD Editor", left + 22, top + PANEL_H - 20, RenderUtil.TEXT, false);

        super.render(c, mx, my, d);
    }

    private int rowHeight(Module m) {
        boolean open = expanded.getOrDefault(m.getName(), false);
        return open ? 28 + 8 + (2 + m.getSettings().size()) * 20 : 26;
    }

    private void drawModuleRow(DrawContext c, Module m, int x, int y, int w, int mx, int my) {
        boolean open = expanded.getOrDefault(m.getName(), false);
        int h = rowHeight(m);
        boolean hover = mx >= x && mx <= x + w && my >= y && my < y + 24;

        RenderUtil.fill(c, x, y, x + w, y + h - 2, hover || open ? 0x28FFFFFF : 0x14FFFFFF);
        RenderUtil.drawBorder(c, x, y, w, h - 2, open ? 0x70FFFFFF : 0x28FFFFFF);
        RenderUtil.drawText(c, textRenderer, m.getName(), x + 8, y + 8, m.isEnabled() ? RenderUtil.TEXT : RenderUtil.DIM, false);

        String pill = m.isEnabled() ? "ON" : "OFF";
        int px = x + w - 42;
        RenderUtil.fill(c, px, y + 5, px + 26, y + 19, m.isEnabled() ? 0x3030A060 : 0x30A03030);
        RenderUtil.drawBorder(c, px, y + 5, 26, 14, m.isEnabled() ? RenderUtil.GREEN : RenderUtil.RED);
        RenderUtil.drawText(c, textRenderer, pill, px + (m.isEnabled() ? 6 : 4), y + 8, m.isEnabled() ? RenderUtil.GREEN : RenderUtil.RED, false);

        if (open) {
            int sy = y + 28;
            drawChip(c, x + 6, sy, w - 12, "Enabled", m.isEnabled() ? "ON" : "OFF", m.isEnabled());
            sy += 20;
            String keyLabel = waitingForKey && expandedModule == m ? "..." : KeyUtil.name(m.getKeybind());
            drawChip(c, x + 6, sy, w - 12, "Keybind", keyLabel, false);
            sy += 20;
            for (Module.Setting<?> s : m.getSettings()) {
                Object v = s.get();
                if (v instanceof Boolean b) drawChip(c, x + 6, sy, w - 12, s.getName(), b ? "ON" : "OFF", b);
                else if (v instanceof Number n) drawSlider(c, x + 6, sy, w - 12, s, n);
                else drawChip(c, x + 6, sy, w - 12, s.getName(), String.valueOf(v), false);
                sy += 20;
            }
        }
    }

    private void drawChip(DrawContext c, int x, int y, int w, String name, String value, boolean on) {
        RenderUtil.drawText(c, textRenderer, name, x + 2, y + 3, RenderUtil.DIM, false);
        int vw = Math.max(24, textRenderer.getWidth(value) + 8);
        int vx = x + w - vw - 2;
        RenderUtil.fill(c, vx, y, vx + vw, y + 14, on ? 0x3030A060 : 0x18FFFFFF);
        RenderUtil.drawBorder(c, vx, y, vw, 14, on ? RenderUtil.GREEN : 0x40FFFFFF);
        RenderUtil.drawText(c, textRenderer, value, vx + 4, y + 3, on ? RenderUtil.GREEN : RenderUtil.TEXT, false);
    }

    private void drawSlider(DrawContext c, int x, int y, int w, Module.Setting<?> s, Number n) {
        RenderUtil.drawText(c, textRenderer, s.getName(), x + 2, y, RenderUtil.DIM, false);
        double min = s.getMin(), max = s.getMax();
        double value = Math.max(min, Math.min(max, n.doubleValue()));
        int bx = x + 2, bw = w - 44, by = y + 11;
        RenderUtil.fill(c, bx, by, bx + bw, by + 2, 0x40FFFFFF);
        double ratio = (value - min) / Math.max(0.0001, max - min);
        int knob = bx + (int) (bw * Math.max(0, Math.min(1, ratio)));
        RenderUtil.fill(c, bx, by, knob, by + 2, 0xAAFFFFFF);
        RenderUtil.fill(c, knob - 2, by - 2, knob + 2, by + 4, 0xFFFFFFFF);
        String label = (n instanceof Integer || n instanceof Long) ? String.valueOf(n.longValue())
                : String.format(Locale.ROOT, "%.1f", n.doubleValue());
        RenderUtil.drawText(c, textRenderer, label, x + w - 36, y + 2, RenderUtil.TEXT, false);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        layout();
        double x = click.x(), y = click.y();
        int b = click.button();

        if (b == 0 && x >= left + 10 && x <= left + 90 && y >= top + PANEL_H - 24 && y <= top + PANEL_H - 8) {
            client.setScreen(new HudEditor());
            return true;
        }

        int tabY = top + 54, tabX = left + 10;
        for (Category cat : categories) {
            String label = cat.getDisplayName();
            int tw = textRenderer.getWidth(label) + 10;
            if (tabX + tw > left + PANEL_W - 10) break;
            if (x >= tabX && x <= tabX + tw && y >= tabY && y <= tabY + 14) {
                selectedCategory = cat; expanded.clear(); expandedModule = null; moduleScroll = 0;
                return true;
            }
            tabX += tw + 3;
        }

        int listTop = top + 74, listBottom = top + PANEL_H - 30;
        int listLeft = left + 8, listRight = left + PANEL_W - 8, contentW = listRight - listLeft;

        if (x >= listLeft && x <= listRight && y >= listTop && y <= listBottom) {
            List<Module> list = modules();
            int yy = listTop - moduleScroll;
            for (Module m : list) {
                int h = rowHeight(m);
                if (y >= yy && y < yy + h) {
                    if (y < yy + 24) {
                        if (b == 1) { m.toggle(); return true; }
                        if (b == 0) {
                            boolean was = expanded.getOrDefault(m.getName(), false);
                            expanded.clear();
                            if (!was) { expanded.put(m.getName(), true); expandedModule = m; }
                            else expandedModule = null;
                            return true;
                        }
                    } else if (expanded.getOrDefault(m.getName(), false) && b == 0) {
                        int sy = yy + 28;
                        if (y >= sy && y < sy + 20) { m.toggle(); return true; }
                        sy += 20;
                        if (y >= sy && y < sy + 20) { waitingForKey = true; expandedModule = m; setFocused(null); return true; }
                        sy += 20;
                        for (Module.Setting<?> s : m.getSettings()) {
                            Object v = s.get();
                            if (y >= sy && y < sy + 20) {
                                if (v instanceof Boolean) { setBoolean(s, !((Boolean) v)); return true; }
                                if (v instanceof Number) {
                                    draggingSlider = true; draggedSetting = s; expandedModule = m;
                                    setNumberFromMouse(s, x, listLeft + 10, contentW - 56);
                                    return true;
                                }
                            }
                            sy += 20;
                        }
                    }
                    return true;
                }
                yy += h;
            }
        }

        if (searchField != null && searchField.mouseClicked(click, doubled)) { setFocused(searchField); return true; }
        return super.mouseClicked(click, doubled);
    }

    @SuppressWarnings("unchecked")
    private void setBoolean(Module.Setting<?> s, boolean value) {
        ((Module.Setting<Boolean>) s).set(value);
        if (expandedModule != null) ConfigManager.saveModule(expandedModule);
    }

    @SuppressWarnings("unchecked")
    private void setNumberFromMouse(Module.Setting<?> s, double mouseX, int bx, int bw) {
        Object v = s.get();
        double min = s.getMin(), max = s.getMax();
        double ratio = Math.max(0, Math.min(1, (mouseX - bx) / (double) Math.max(1, bw)));
        double nv = min + (max - min) * ratio;
        if (v instanceof Integer) ((Module.Setting<Integer>) s).set((int) Math.round(nv));
        else if (v instanceof Long) ((Module.Setting<Long>) s).set(Math.round(nv));
        else if (v instanceof Float) ((Module.Setting<Float>) s).set((float) nv);
        else if (v instanceof Double) ((Module.Setting<Double>) s).set(nv);
        if (expandedModule != null) ConfigManager.saveModule(expandedModule);
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        if (draggingSlider && draggedSetting != null) {
            setNumberFromMouse(draggedSetting, click.x(), left + 18, PANEL_W - 72);
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) {
        draggingSlider = false; draggedSetting = null;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        layout();
        if (mx >= left && mx <= left + PANEL_W && my >= top + 74 && my <= top + PANEL_H - 30) {
            moduleScroll = Math.max(0, moduleScroll - (int) (v * 20));
            return true;
        }
        return super.mouseScrolled(mx, my, h, v);
    }

    @Override
    public boolean keyPressed(KeyInput i) {
        if (i.key() == 256) {
            if (waitingForKey) { waitingForKey = false; return true; }
            close(); return true;
        }
        if (waitingForKey && expandedModule != null) {
            if (i.key() != 256) {
                expandedModule.setKeybind(i.key());
                ConfigManager.saveModule(expandedModule);
            }
            waitingForKey = false; return true;
        }
        if (searchField != null && searchField.isFocused() && searchField.keyPressed(i)) return true;
        return super.keyPressed(i);
    }

    @Override
    public boolean charTyped(CharInput i) {
        if (searchField != null && searchField.isFocused() && searchField.charTyped(i)) return true;
        return super.charTyped(i);
    }

    @Override
    public boolean shouldPause() { return false; }
}
