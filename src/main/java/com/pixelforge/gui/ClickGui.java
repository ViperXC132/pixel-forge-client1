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

/** Lunar-style ClickGUI with expandable module cards and settings. */
public class ClickGui extends Screen {
    private final List<Category> categories = new ArrayList<>();
    private Category selectedCategory;
    private Module expandedModule;
    private TextFieldWidget searchField;
    private int moduleScroll;
    private boolean waitingForKey;
    private boolean draggingSlider;
    private Module.Setting<?> draggedSetting;

    private static final int BG = 0xF00B0E16;
    private static final int CARD = 0xFF161B2A;
    private static final int CARD_HOVER = 0xFF1C2336;
    private static final int ACCENT = 0xFF5B6CFF;
    private static final int ACCENT_DIM = 0xFF3A4699;
    private static final int TEXT = 0xFFEAF0FF;
    private static final int DIM = 0xFF8B95B0;
    private static final int GREEN = 0xFF4ADE80;
    private static final int RED = 0xFFF87171;
    private static final int BORDER = 0xFF252B3D;

    private int left, top, cw, ch;
    private final Map<String, Boolean> expanded = new HashMap<>();

    public ClickGui() {
        super(Text.literal("PixelForge ClickGUI"));
        for (Category c : Category.values()) {
            if (c != Category.SYSTEM) categories.add(c);
        }
        if (!categories.isEmpty()) selectedCategory = categories.get(0);
    }

    @Override
    protected void init() {
        searchField = new TextFieldWidget(textRenderer, 0, 0, 180, 18, Text.literal("Search"));
        searchField.setPlaceholder(Text.literal("Search modules..."));
        searchField.setMaxLength(40);
        addDrawableChild(searchField);
        layout();
    }

    private void layout() {
        cw = Math.min(width - 32, Math.max(680, (int) (width * 0.72)));
        ch = Math.min(height - 32, Math.max(400, (int) (height * 0.72)));
        left = (width - cw) / 2;
        top = (height - ch) / 2;
        if (searchField != null) {
            searchField.setX(left + cw - 200);
            searchField.setY(top + 12);
            searchField.setWidth(180);
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
        RenderUtil.fill(c, 0, 0, width, height, 0x99000000);
        RenderUtil.fill(c, left, top, left + cw, top + ch, BG);
        RenderUtil.drawBorder(c, left, top, cw, ch, BORDER);

        RenderUtil.fill(c, left, top, left + cw, top + 42, 0xF00E1220);
        RenderUtil.drawText(c, textRenderer, "PIXELFORGE", left + 16, top + 15, TEXT, false);
        RenderUtil.drawText(c, textRenderer, "RSHIFT", left + 110, top + 15, DIM, false);

        int tabX = left + 16;
        int tabY = top + 50;
        for (Category cat : categories) {
            String label = cat.getDisplayName();
            int tw = textRenderer.getWidth(label) + 18;
            boolean on = cat == selectedCategory;
            boolean hover = mx >= tabX && mx <= tabX + tw && my >= tabY && my <= tabY + 20;
            RenderUtil.fill(c, tabX, tabY, tabX + tw, tabY + 20, on ? 0x403B5BDB : (hover ? 0x201E2540 : 0));
            if (on) RenderUtil.fill(c, tabX, tabY + 19, tabX + tw, tabY + 20, ACCENT);
            RenderUtil.drawText(c, textRenderer, label, tabX + 9, tabY + 6, on ? ACCENT : DIM, false);
            tabX += tw + 6;
        }

        int listTop = top + 80;
        int listBottom = top + ch - 40;
        int listLeft = left + 12;
        int listRight = left + cw - 12;
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
                drawModuleCard(c, m, listLeft, yy, contentW, mx, my);
            }
            yy += h;
        }
        c.disableScissor();

        if (maxScroll > 0) {
            int view = listBottom - listTop;
            int thumb = Math.max(18, (int) (view * (view / (double) (view + maxScroll))));
            int ty = listTop + (int) ((view - thumb) * (moduleScroll / (double) maxScroll));
            RenderUtil.fill(c, listRight - 4, listTop, listRight - 1, listBottom, 0x301E2540);
            RenderUtil.fill(c, listRight - 4, ty, listRight - 1, ty + thumb, ACCENT);
        }

        RenderUtil.fill(c, left, top + ch - 36, left + cw, top + ch, 0xF00E1220);
        boolean hudHover = mx >= left + 14 && mx <= left + 120 && my >= top + ch - 28 && my <= top + ch - 10;
        RenderUtil.fill(c, left + 14, top + ch - 28, left + 120, top + ch - 10, hudHover ? 0x403B5BDB : 0x201E2540);
        RenderUtil.drawBorder(c, left + 14, top + ch - 28, 106, 18, ACCENT);
        RenderUtil.drawText(c, textRenderer, "HUD Editor", left + 36, top + ch - 23, ACCENT, false);
        RenderUtil.drawText(c, textRenderer, "Scroll · LMB expand · RMB toggle", left + 136, top + ch - 22, DIM, false);

        super.render(c, mx, my, d);
    }

    private int rowHeight(Module m) {
        boolean open = expanded.getOrDefault(m.getName(), false);
        int base = 36;
        if (!open) return base;
        int settings = 2 + m.getSettings().size();
        return base + 8 + settings * 28;
    }

    private void drawModuleCard(DrawContext c, Module m, int x, int y, int w, int mx, int my) {
        boolean open = expanded.getOrDefault(m.getName(), false);
        int h = rowHeight(m);
        boolean hover = mx >= x && mx <= x + w && my >= y && my < y + 36;

        RenderUtil.fill(c, x, y, x + w, y + h - 4, hover || open ? CARD_HOVER : CARD);
        RenderUtil.drawBorder(c, x, y, w, h - 4, open ? ACCENT_DIM : BORDER);

        if (m.isEnabled()) {
            RenderUtil.fill(c, x, y, x + 3, y + h - 4, ACCENT);
        }

        RenderUtil.drawText(c, textRenderer, m.getName(), x + 12, y + 8, m.isEnabled() ? TEXT : DIM, false);
        String desc = m.getDescription();
        if (desc.length() > 48) desc = desc.substring(0, 45) + "...";
        RenderUtil.drawText(c, textRenderer, desc, x + 12, y + 20, 0xFF5A6478, false);

        String pill = m.isEnabled() ? "ON" : "OFF";
        int pw = 36;
        int px = x + w - pw - 36;
        RenderUtil.fill(c, px, y + 8, px + pw, y + 26, m.isEnabled() ? 0x4030A060 : 0x40A03030);
        RenderUtil.drawBorder(c, px, y + 8, pw, 18, m.isEnabled() ? GREEN : RED);
        RenderUtil.drawText(c, textRenderer, pill, px + (m.isEnabled() ? 11 : 9), y + 13, m.isEnabled() ? GREEN : RED, false);
        RenderUtil.drawText(c, textRenderer, open ? "v" : ">", x + w - 20, y + 12, DIM, false);

        if (open) {
            int sy = y + 40;
            drawSettingRow(c, x + 10, sy, w - 20, "Enabled", m.isEnabled() ? "ON" : "OFF", m.isEnabled());
            sy += 28;
            String keyLabel = waitingForKey && expandedModule == m
                    ? "Press a key..."
                    : (m.getKeybind() < 0 ? "None" : ("Key " + m.getKeybind()));
            drawSettingRow(c, x + 10, sy, w - 20, "Keybind", keyLabel, false);
            sy += 28;

            for (Module.Setting<?> s : m.getSettings()) {
                Object v = s.get();
                if (v instanceof Boolean b) {
                    drawSettingRow(c, x + 10, sy, w - 20, s.getName(), b ? "ON" : "OFF", b);
                } else if (v instanceof Number n) {
                    drawSliderRow(c, x + 10, sy, w - 20, s, n);
                } else {
                    drawSettingRow(c, x + 10, sy, w - 20, s.getName(), String.valueOf(v), false);
                }
                sy += 28;
            }
        }
    }

    private void drawSettingRow(DrawContext c, int x, int y, int w, String name, String value, boolean on) {
        RenderUtil.drawText(c, textRenderer, name, x + 4, y + 6, DIM, false);
        int vw = Math.max(40, textRenderer.getWidth(value) + 16);
        int vx = x + w - vw - 4;
        RenderUtil.fill(c, vx, y + 2, vx + vw, y + 20, on ? 0x4030A060 : 0x201E2540);
        RenderUtil.drawBorder(c, vx, y + 2, vw, 18, on ? GREEN : BORDER);
        RenderUtil.drawText(c, textRenderer, value, vx + 8, y + 7, on ? GREEN : TEXT, false);
    }

    private void drawSliderRow(DrawContext c, int x, int y, int w, Module.Setting<?> s, Number n) {
        RenderUtil.drawText(c, textRenderer, s.getName(), x + 4, y + 2, DIM, false);
        double min = s.getMin();
        double max = s.getMax();
        double value = Math.max(min, Math.min(max, n.doubleValue()));
        int bx = x + 4;
        int bw = w - 70;
        int by = y + 14;
        RenderUtil.fill(c, bx, by, bx + bw, by + 4, 0x503B5BDB);
        double ratio = (value - min) / Math.max(0.0001, max - min);
        int knob = bx + (int) (bw * Math.max(0, Math.min(1, ratio)));
        RenderUtil.fill(c, bx, by, knob, by + 4, ACCENT);
        RenderUtil.fill(c, knob - 4, by - 3, knob + 4, by + 7, 0xFFEAF0FF);
        String label = formatNumber(n);
        RenderUtil.drawText(c, textRenderer, label, x + w - 50, y + 6, ACCENT, false);
    }

    private String formatNumber(Number n) {
        if (n instanceof Integer || n instanceof Long) return String.valueOf(n.longValue());
        return String.format(Locale.ROOT, "%.2f", n.doubleValue());
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        layout();
        double x = click.x(), y = click.y();
        int b = click.button();

        if (b == 0 && x >= left + 14 && x <= left + 120 && y >= top + ch - 28 && y <= top + ch - 10) {
            client.setScreen(new HudEditor());
            return true;
        }

        int tabX = left + 16;
        int tabY = top + 50;
        for (Category cat : categories) {
            String label = cat.getDisplayName();
            int tw = textRenderer.getWidth(label) + 18;
            if (x >= tabX && x <= tabX + tw && y >= tabY && y <= tabY + 20) {
                selectedCategory = cat;
                expandedModule = null;
                moduleScroll = 0;
                return true;
            }
            tabX += tw + 6;
        }

        int listTop = top + 80;
        int listBottom = top + ch - 40;
        int listLeft = left + 12;
        int listRight = left + cw - 12;
        int contentW = listRight - listLeft;

        if (x >= listLeft && x <= listRight && y >= listTop && y <= listBottom) {
            List<Module> list = modules();
            int yy = listTop - moduleScroll;
            for (Module m : list) {
                int h = rowHeight(m);
                if (y >= yy && y < yy + h) {
                    if (y < yy + 36) {
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
                        int sy = yy + 40;
                        if (y >= sy && y < sy + 28) {
                            m.toggle();
                            return true;
                        }
                        sy += 28;
                        if (y >= sy && y < sy + 28) {
                            waitingForKey = true;
                            expandedModule = m;
                            setFocused(null);
                            return true;
                        }
                        sy += 28;
                        for (Module.Setting<?> s : m.getSettings()) {
                            Object v = s.get();
                            if (y >= sy && y < sy + 28) {
                                if (v instanceof Boolean) {
                                    setBoolean(s, !((Boolean) v));
                                    return true;
                                }
                                if (v instanceof Number) {
                                    draggingSlider = true;
                                    draggedSetting = s;
                                    expandedModule = m;
                                    setNumberFromMouse(s, x, listLeft + 14, contentW - 40);
                                    return true;
                                }
                            }
                            sy += 28;
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
        double min = s.getMin();
        double max = s.getMax();
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
            int listLeft = left + 12;
            int contentW = cw - 24;
            setNumberFromMouse(draggedSetting, click.x(), listLeft + 14, contentW - 40);
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
        if (mx >= left && mx <= left + cw && my >= top + 80 && my <= top + ch - 40) {
            moduleScroll = Math.max(0, moduleScroll - (int) (vertical * 28));
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
