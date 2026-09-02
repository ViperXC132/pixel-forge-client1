package com.pixelforge.gui;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.config.ProfileManager;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Lunar-style ClickGUI — opaque-transparent panels, short rows, Right Shift.
 */
public class ClickGui extends Screen {

    private final List<Panel> panels = new ArrayList<>();
    private String search = "";
    private int profileIndex = 0;
    private final String[] profileNames;

    private static final int BG_DIM       = 0x88060A14; // see world through
    private static final int BG_PANEL     = 0xD0101424; // opaque transparent
    private static final int BG_HEADER    = 0xE0141830;
    private static final int ACCENT       = 0xFF3B5BDB;
    private static final int TEXT_MAIN    = 0xFFE8ECFF;
    private static final int TEXT_DIM     = 0xFF8A90B0;
    private static final int ENABLED_BG   = 0xB0183A28;
    private static final int DISABLED_BG  = 0xB0121628;
    private static final int HOVER_BG     = 0xC01A2040;

    public ClickGui() {
        super(Text.literal("PixelForge"));
        profileNames = ProfileManager.getProfileNames();
        int x = 14;
        for (Category category : Category.values()) {
            if (category == Category.SYSTEM) continue;
            panels.add(new Panel(category, x, 34));
            x += 106;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderUtil.fill(context, 0, 0, width, height, BG_DIM);

        RenderUtil.fill(context, 0, 0, width, 26, 0xE00A0C14);
        RenderUtil.fill(context, 0, 25, width, 26, ACCENT);

        RenderUtil.drawText(context, textRenderer, "PixelForge", 10, 8, ACCENT, false);

        String searchDisplay = search.isEmpty() ? "Search..." : search + (System.currentTimeMillis() % 1000 > 500 ? "|" : "");
        RenderUtil.drawText(context, textRenderer, searchDisplay, 88, 8,
                search.isEmpty() ? TEXT_DIM : TEXT_MAIN, false);

        String profileLabel = "Profile: " + profileNames[profileIndex];
        int pw = textRenderer.getWidth(profileLabel);
        RenderUtil.drawText(context, textRenderer, profileLabel, width - pw - 10, 8, TEXT_MAIN, false);

        for (Panel panel : panels) {
            panel.render(context, mouseX, mouseY, search);
        }

        RenderUtil.drawText(context, textRenderer,
                "RShift close  ·  LMB toggle  ·  RMB collapse  ·  Profile cycles presets",
                8, height - 12, 0xFF3D4A6A, false);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        String profileLabel = "Profile: " + profileNames[profileIndex];
        int pw = textRenderer.getWidth(profileLabel);
        if (mouseY < 26 && mouseX >= width - pw - 14) {
            if (button == 0) {
                profileIndex = (profileIndex + 1) % profileNames.length;
                ProfileManager.loadProfile(profileNames[profileIndex]);
                PixelForgeClient.getInstance().getNotificationManager()
                        .push("Profile: " + profileNames[profileIndex], ACCENT);
            }
            return true;
        }

        for (Panel panel : panels) {
            if (panel.mouseClicked(mouseX, mouseY, button, search)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || keyCode == 344) {
            close();
            return true;
        }
        if (keyCode == 259 && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (Character.isLetterOrDigit(chr) || chr == ' ' || chr == '_' || chr == '-') {
            if (search.length() < 24) search += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private class Panel {
        final Category category;
        final int x, y;
        boolean open = true;
        static final int WIDTH = 98;

        Panel(Category category, int x, int y) {
            this.category = category;
            this.x = x;
            this.y = y;
        }

        void render(DrawContext context, int mouseX, int mouseY, String filter) {
            List<Module> mods = PixelForgeClient.getInstance().getModuleManager()
                    .getModulesByCategory(category).stream()
                    .filter(m -> filter.isEmpty() || m.getName().toLowerCase().contains(filter.toLowerCase()))
                    .toList();

            int headerH = 15;
            int rowH = 13;
            int bodyH = open ? mods.size() * rowH + 3 : 0;

            RenderUtil.fill(context, x, y, x + WIDTH, y + headerH + bodyH, BG_PANEL);
            RenderUtil.fill(context, x, y, x + 2, y + headerH + bodyH, ACCENT);
            RenderUtil.fill(context, x + 2, y, x + WIDTH, y + headerH, BG_HEADER);
            RenderUtil.drawText(context, textRenderer, category.getDisplayName(), x + 6, y + 3, TEXT_MAIN, false);

            if (open) {
                int oy = y + headerH + 1;
                for (Module m : mods) {
                    boolean hover = mouseX >= x + 2 && mouseX <= x + WIDTH - 2 && mouseY >= oy && mouseY <= oy + rowH - 1;
                    int bg = m.isEnabled() ? ENABLED_BG : (hover ? HOVER_BG : DISABLED_BG);
                    RenderUtil.fill(context, x + 3, oy, x + WIDTH - 2, oy + rowH - 1, bg);
                    RenderUtil.drawText(context, textRenderer, m.getName(), x + 6, oy + 2,
                            m.isEnabled() ? 0xFF6DFF9A : TEXT_DIM, false);
                    oy += rowH;
                }
            }
        }

        boolean mouseClicked(double mouseX, double mouseY, int button, String filter) {
            if (mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + 15) {
                if (button == 1) open = !open;
                return true;
            }
            if (!open) return false;

            List<Module> mods = PixelForgeClient.getInstance().getModuleManager()
                    .getModulesByCategory(category).stream()
                    .filter(m -> filter.isEmpty() || m.getName().toLowerCase().contains(filter.toLowerCase()))
                    .toList();

            int oy = y + 16;
            for (Module m : mods) {
                if (mouseX >= x + 3 && mouseX <= x + WIDTH - 2 && mouseY >= oy && mouseY <= oy + 12) {
                    if (button == 0) m.toggle();
                    return true;
                }
                oy += 13;
            }
            return false;
        }
    }
}
