package com.pixelforge.gui;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.config.ConfigManager;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
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

/**
 * Compact Lunar-style ClickGUI — small centered rounded panel.
 */
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

    private static final int PANEL_W = 360;
    private static final int PANEL_H = 420;

    private static final int BG = 0xF0121624;
    private static final int HEADER = 0xF00E1220;
    private static final int CARD = 0xFF1A2030;
    private static final int CARD_HOVER = 0xFF222A3C;
    private static final int ACCENT = 0xFF5B6CFF;
    private static final int ACCENT_DIM = 0xFF3A4699;
    private static final int TEXT = 0xFFEAF0FF;
    private static final int DIM = 0xFF8B95B0;
    private static final int GREEN = 0xFF4ADE80;
    private static final int RED = 0xFFF87171;
    private static final int BORDER = 0xFF2A3350;

    private int left, top;

    public ClickGui() {
        super(Text.literal("PixelForge ClickGUI"));
        for (Category c : Category.values()) {
            if (c != Category.SYSTEM) categories.add(c);
        }
        if (!categories.isEmpty()) selectedCategory = categories.get(0);
    }

    @Override
    protected void init() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;
        searchField = new TextFieldWidget(textRenderer, left + 10, top + 36, PANEL_W - 20, 16, Text.literal("Search"));
        searchField.setPlaceholder(Text.literal("Search..."));
        searchField.setMaxLength(40);
        addDrawableChild(searchField);
    }

    private void layout() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;
        if (searchField != null) {
            searchField.setX(left + 10);
            searchField.setY(top + 36);
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

    private void drawRoundedPanel(DrawContext c, int x, int y, int w, int h, int fill, int border) {
        RenderUtil.fill(c, x + 2, y, x + w - 2, y + h, fill);
        RenderUtil.fill(c, x, y + 2, x + w, y + h - 2, fill);
        RenderUtil.fill(c, x + 1, y + 1, x + 2, y + 2, fill);
        RenderUtil.fill(c, x + w - 2, y + 1, x + w - 1, y + 2, fill);
        RenderUtil.fill(c, x + 1, y + h - 2, x + 2, y + h - 1, fill);
        RenderUtil.fill(c, x + w - 2, y + h - 2, x + w - 1, y + h - 1, fill);
        RenderUtil.fill(c, x + 2, y, x + w - 2, y + 1, border);
        RenderUtil.fill(c, x + 2, y + h - 1, x + w - 2, y + h, border);
        RenderUtil.fill(c, x, y + 2, x + 1, y + h - 2, border);
        RenderUtil.fill(c, x + w - 1, y + 2, x + w, y + h - 2, border);
        RenderUtil.fill(c, x + 1, y + 1, x + 2, y + 2, border);
        RenderUtil.fill(c, x + w - 2, y + 1, x + w - 1, y + 2, border);
        RenderUtil.fill(c, x + 1, y + h - 2, x + 2, y + h - 1, border);
        RenderUtil.fill(c, x + w - 2, y + h - 2, x + w - 1, y + h - 1, border);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        layout();
        RenderUtil.fill(c, 0, 0, width, height, 0x88000000);
        drawRoundedPanel(c, left, top, PANEL_W, PANEL_H, BG, BORDER);

        RenderUtil.fill(c, left + 2, top + 2, left + PANEL_W - 2, top + 30, HEADER);
        RenderUtil.drawText(c, textRenderer, "PIXELFORGE", left + 12, top + 11, TEXT, false);
        RenderUtil.drawText(c, textRenderer, "RSHIFT", left + PANEL_W - 48, top + 11, DIM, false);

        int tabY = top + 56;
        int tabX = left + 10;
        for (Category cat : categories) {
            String label = cat.getDisplayName();
            int tw = textRenderer.getWidth(label) + 12;
            if (tabX + tw > left + PANEL_W - 10) break;
            boolean on = cat == selectedCategory;
            boolean hover = mx >= tabX && mx <= tabX + tw && my >= tabY && my <= tabY + 16;
            RenderUtil.fill(c, tabX, tabY, tabX + tw, tabY + 16, on ? 0x503B5BDB : (hover ? 0x301E2540 : 0x20101824));
            RenderUtil.drawBorder(c, tabX, tabY, tw, 16, on ? ACCENT : BORDER);
            RenderUtil.drawText(c, textRenderer, label, tabX + 6, tabY + 4, on ? ACCENT : DIM, false);
            tabX += tw + 4;
        }

        int listTop = top + 78;
        int listBottom = top + PANEL_H - 32;
        int listLeft = left + 8;
        int listRight = left + PANEL_W - 8;
        int contentW = listRight - listLeft;

        List<Module> list = modules();
        int totalH = 0;
        for (Module m : list) totalH += rowHeight(m);
        int maxScroll = Math.max(0, totalH - (listBottom - listTop));
        moduleScroll = Math.max(0, Math.min(moduleScroll, maxScroll));

        c.enableScissor(listLeft, listTop, listRight, listBottom);
        int yy = listTop - moduleScroll;
        for (Module m : list) {
            int h = rowHeight(m);
            if (yy + h >= listTop && yy <= listBottom) {
                drawModuleRow(c, m, listLeft, yy, contentW, mx, my);
            }
            yy += h;
        }
        c.disableScissor();

        if (maxScroll > 0) {
            int view = listBottom - listTop;
            int thumb = Math.max(12, (int) (view * (view / (double) (view + maxScroll))));
            int ty = listTop + (int) ((view - thumb) * (moduleScroll / (double) maxScroll));
            RenderUtil.fill(c, listRight - 3, listTop, listRight - 1, listBottom, 0x301E2540);
            RenderUtil.fill(c, listRight - 3, ty, listRight - 1, ty + thumb, ACCENT);
        }

        RenderUtil.fill(c, left + 2, top + PANEL_H - 28, left + PANEL_W - 2, top + PANEL_H - 2, HEADER);
        boolean hudHover = mx >= left + 10 && mx <= left + 100 && my >= top + PANEL_H - 22 && my <= top + PANEL_H - 8;
        RenderUtil.fill(c, left + 10, top + PANEL_H - 22, left + 100, top + PANEL_H - 8, hudHover ? 0x403B5BDB : 0x201E2540);
        RenderUtil.drawBorder(c, left + 10, top + PANEL_H - 22, 90, 14, ACCENT);
        RenderUtil.drawText(c, textRenderer, "HUD Editor", left + 28, top + PANEL_H - 19, ACCENT, false);
        RenderUtil.drawText(c, textRenderer, "RMB toggle · LMB expand", left + 110, top + PANEL_H - 19, DIM, false);

        super.render(c, mx, my, d);
    }

    private int rowHeight(Module m) {
        boolean open = expanded.getOrDefault(m.getName(), false);
        if (!open) return 28;
        return 28 + 8 + (2 + m.getSettings().size()) * 22;
    }

    private void drawModuleRow(DrawContext c, Module m, int x, int y, int w, int mx, int my) {
        boolean open = expanded.getOrDefault(m.getName(), false);
        int h = rowHeight(m);
        boolean hover = mx >= x && mx <= x + w && my >= y && my < y + 26;

        RenderUtil.fill(c, x, y, x + w, y + h - 2, hover || open ? CARD_HOVER : CARD);
        RenderUtil.drawBorder(c, x, y, w, h - 2, open ? ACCENT_DIM : BORDER);

        if (m.isEnabled()) {
            RenderUtil.fill(c, x, y, x + 2, y + h - 2, ACCENT);
        }

        RenderUtil.drawText(c, textRenderer, m.getName(), x + 8, y + 9, m.isEnabled() ? TEXT : DIM, false);

        String pill = m.isEnabled() ? "ON" : "OFF";
        int px = x + w - 48;
        RenderUtil.fill(c, px, y + 6, px + 28, y + 20, m.isEnabled() ? 0x4030A060 : 0x40A03030);
        RenderUtil.drawBorder(c, px, y + 6, 28, 14, m.isEnabled() ? GREEN : RED);
        RenderUtil.drawText(c, textRenderer, pill, px + (m.isEnabled() ? 7 : 5), y + 9, m.isEnabled() ? GREEN : RED, false);
        RenderUtil.drawText(c, textRenderer, open ? "v" : ">", x + w - 14, y + 9, DIM, false);

        if (open) {
            int sy = y + 30;
            drawSettingChip(c, x + 6, sy, w - 12, "Enabled", m.isEnabled() ? "ON" : "OFF", m.isEnabled());
            sy += 22;
            String keyLabel = waitingForKey && expandedModule == m
                    ? "Press key..."
                    : (m.getKeybind() < 0 ? "None" : ("Key " + m.getKeybind()));
            drawSettingChip(c, x + 6, sy, w - 12, "Keybind", keyLabel, false);
            sy += 22;
            for (Module.Setting<?> s : m.getSettings()) {
                Object v = s.get();
                if (v instanceof Boolean b) {
                    drawSettingChip(c, x + 6, sy, w - 12, s.getName(), b ? "ON" : "OFF", b);
                } else if (v instanceof Number n) {
                    drawSlider(c, x + 6, sy, w - 12, s, n);
                } else {
                    drawSettingChip(c, x + 6, sy, w - 12, s.getName(), String.valueOf(v), false);
                }
                sy += 22;
            }
        }
    }

    private void drawSettingChip(DrawContext c, int x, int y, int w, String name, String value, boolean on) {
        RenderUtil.drawText(c, textRenderer, name, x + 2, y + 4, DIM, false);
        int vw = Math.max(28, textRenderer.getWidth(value) + 10);
        int vx = x + w - vw - 2;
        RenderUtil.fill(c, vx, y + 1, vx + vw, y + 15, on ? 0x4030A060 : 0x201E2540);
        RenderUtil.drawBorder(c, vx, y + 1, vw, 14, on ? GREEN : BORDER);
        RenderUtil.drawText(c, textRenderer, value, vx + 5, y + 4, on ? GREEN : TEXT, false);
    }

    private void drawSlider(DrawContext c, int x, int y, int w, Module.Setting<?> s, Number n) {
        RenderUtil.drawText(c, textRenderer, s.getName(), x + 2, y + 1, DIM, false);
        double min = s.getMin(), max = s.getMax();
        double value = Math.max(min, Math.min(max, n.doubleValue()));
        int bx = x + 2, bw = w - 48, by = y + 12;
        RenderUtil.fill(c, bx, by, bx + bw, by + 3, 0x503B5BDB);
        double ratio = (value - min) / Math.max(0.0001, max - min);
        int knob = bx + (int) (bw * Math.max(0, Math.min(1, ratio)));
        RenderUtil.fill(c, bx, by, knob, by + 3, ACCENT);
        RenderUtil.fill(c, knob - 3, by - 2, knob + 3, by + 5, 0xFFEAF0FF);
        String label = (n instanceof Integer || n instanceof Long) ? String.valueOf(n.longValue())
                : String.format(Locale.ROOT, "%.2f", n.doubleValue());
        RenderUtil.drawText(c, textRenderer, label, x + w - 40, y + 3, ACCENT, false);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        layout();
        double x = click.x(), y = click.y();
        int b = click.button();

        if (b == 0 && x >= left + 10 && x <= left + 100 && y >= top + PANEL_H - 22 && y <= top + PANEL_H - 8) {
            client.setScreen(new HudEditor());
            return true;
        }

        int tabY = top + 56;
        int tabX = left + 10;
        for (Category cat : categories) {
            String label = cat.getDisplayName();
            int tw = textRenderer.getWidth(label) + 12;
            if (tabX + tw > left + PANEL_W - 10) break;
            if (x >= tabX && x <= tabX + tw && y >= tabY && y <= tabY + 16) {
                selectedCategory = cat;
                expandedModule = null;
                expanded.clear();
                moduleScroll = 0;
                return true;
            }
            tabX += tw + 4;
        }

        int listTop = top + 78;
        int listBottom = top + PANEL_H - 32;
        int listLeft = left + 8;
        int listRight = left + PANEL_W - 8;
        int contentW = listRight - listLeft;

        if (x >= listLeft && x <= listRight && y >= listTop && y <= listBottom) {
            List<Module> list = modules();
            int yy = listTop - moduleScroll;
            for (Module m : list) {
                int h = rowHeight(m);
                if (y >= yy && y < yy + h) {
                    if (y < yy + 26) {
                        if (b == 1) {
                            m.toggle();
                            return true;
                        }
                        if (b == 0) {
                            boolean was = expanded.getOrDefault(m.getName(), false);
                            expanded.clear();
                            if (!was) {
                                expanded.put(m.getName(), true);
                                expandedModule = m;
                            } else {
                                expandedModule = null;
                            }
                            return true;
                        }
                    } else if (expanded.getOrDefault(m.getName(), false) && b == 0) {
                        int sy = yy + 30;
                        if (y >= sy && y < sy + 22) {
                            m.toggle();
                            return true;
                        }
                        sy += 22;
                        if (y >= sy && y < sy + 22) {
                            waitingForKey = true;
                            expandedModule = m;
                            setFocused(null);
                            return true;
                        }
                        sy += 22;
                        for (Module.Setting<?> s : m.getSettings()) {
                            Object v = s.get();
                            if (y >= sy && y < sy + 22) {
                                if (v instanceof Boolean) {
                                    setBoolean(s, !((Boolean) v));
                                    return true;
                                }
                                if (v instanceof Number) {
                                    draggingSlider = true;
                                    draggedSetting = s;
                                    expandedModule = m;
                                    setNumberFromMouse(s, x, listLeft + 8, contentW - 56);
                                    return true;
                                }
                            }
                            sy += 22;
                        }
                    }
                    return true;
                }
                yy += h;
            }
        }

        if (searchField != null && searchField.mouseClicked(click, doubled)) {
            setFocused(searchField);
            return true;
        }
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
        if (draggingSlider && draggedSetting != null && expandedModule != null) {
            int listLeft = left + 8;
            int contentW = PANEL_W - 16;
            setNumberFromMouse(draggedSetting, click.x(), listLeft + 8, contentW - 56);
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) {
        draggingSlider = false;
        draggedSetting = null;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        layout();
        if (mx >= left && mx <= left + PANEL_W && my >= top + 78 && my <= top + PANEL_H - 32) {
            moduleScroll = Math.max(0, moduleScroll - (int) (vertical * 22));
            return true;
        }
        return super.mouseScrolled(mx, my, horizontal, vertical);
    }

    @Override
    public boolean keyPressed(KeyInput i) {
        if (i.key() == 256) {
            if (waitingForKey) {
                waitingForKey = false;
                return true;
            }
            close();
            return true;
        }
        if (waitingForKey && expandedModule != null) {
            if (i.key() != 256) {
                expandedModule.setKeybind(i.key());
                ConfigManager.saveModule(expandedModule);
            }
            waitingForKey = false;
            return true;
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
    public boolean shouldPause() {
        return false;
    }
}
