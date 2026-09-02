package com.pixelforge.gui.screens;

import com.pixelforge.mod.ModInstaller;
import com.pixelforge.mod.ModrinthApi;
import com.pixelforge.util.RenderUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Installed mods list + Modrinth browse/install.
 * Downloads go straight into the mods folder.
 * VulkanMod safe.
 */
public class ModsScreen extends Screen {

    private final Screen parent;
    private boolean browseMode = false;
    private TextFieldWidget searchField;
    private final List<ModrinthApi.ModResult> results = new ArrayList<>();
    private String status = "";
    private boolean restartNeeded = false;
    private int scroll;

    private static final int ACCENT = 0xFF3B5BDB;
    private static final int TEXT = 0xFFC8D0E0;
    private static final int DIM = 0xFF3D4A6A;
    private static final int PANEL = 0xE00A0D18;
    private static final int BORDER = 0xFF1E2540;

    public ModsScreen(Screen parent) {
        super(Text.literal("Mods"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        searchField = new TextFieldWidget(textRenderer, 20, 48, width - 40, 18, Text.literal("Search"));
        searchField.setPlaceholder(Text.literal("Search Modrinth..."));
        searchField.setChangedListener(s -> {
            if (browseMode && s.length() >= 2) {
                status = "Searching...";
                ModrinthApi.searchAsync(s, list -> {
                    results.clear();
                    results.addAll(list);
                    status = results.isEmpty() ? "No results" : "";
                });
            }
        });
        addSelectableChild(searchField);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderUtil.fill(context, 0, 0, width, height, 0xFF0E1117);

        // Header
        RenderUtil.fill(context, 0, 0, width, 36, 0xCC0A0C14);
        RenderUtil.drawText(context, textRenderer, "Mods", 16, 12, TEXT, false);

        // Tabs
        drawTab(context, "Installed", 80, !browseMode);
        drawTab(context, "Browse Modrinth", 150, browseMode);

        if (restartNeeded) {
            RenderUtil.fill(context, 16, 40, width - 16, 58, 0x30FA5252);
            RenderUtil.drawBorder(context, 16, 40, width - 32, 18, 0xFFFA5252);
            RenderUtil.drawText(context, textRenderer, "Restart required to apply changes", 24, 45, 0xFFFF8787, false);
            RenderUtil.drawText(context, textRenderer, "[Restart]", width - 80, 45, 0xFFFF8787, false);
        }

        int listY = restartNeeded ? 66 : 44;

        if (browseMode) {
            searchField.setY(listY);
            searchField.render(context, mouseX, mouseY, delta);
            listY += 24;

            if (!status.isEmpty()) {
                RenderUtil.drawText(context, textRenderer, status, 20, listY, DIM, false);
            }

            int y = listY + (status.isEmpty() ? 0 : 14);
            for (ModrinthApi.ModResult mod : results) {
                if (y > height - 30) break;
                drawModRow(context, 16, y, width - 32, mod.title, mod.description, mod.iconUrl, true, mod);
                y += 36;
            }
        } else {
            Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();
            int y = listY;
            for (ModContainer mod : mods) {
                if (y > height - 30) break;
                String id = mod.getMetadata().getId();
                if (id.equals("minecraft") || id.equals("java") || id.equals("fabricloader")) continue;
                String name = mod.getMetadata().getName();
                String ver = mod.getMetadata().getVersion().getFriendlyString();
                drawModRow(context, 16, y, width - 32, name, ver + " · " + id, null, false, null);
                y += 36;
            }
        }

        RenderUtil.drawText(context, textRenderer, "ESC back", 12, height - 14, DIM, false);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawTab(DrawContext context, String label, int x, boolean on) {
        int c = on ? 0xFF748FFF : DIM;
        RenderUtil.drawText(context, textRenderer, label, x, 12, c, false);
        if (on) {
            RenderUtil.fill(context, x, 28, x + textRenderer.getWidth(label), 29, ACCENT);
        }
    }

    private void drawModRow(DrawContext context, int x, int y, int w, String name, String sub,
                            String iconUrl, boolean browse, ModrinthApi.ModResult result) {
        RenderUtil.fill(context, x, y, x + w, y + 32, 0x0AFFFFFF);
        RenderUtil.drawBorder(context, x, y, w, 32, BORDER);

        // Icon placeholder (blue square = PixelForge style fallback)
        RenderUtil.fill(context, x + 6, y + 6, x + 26, y + 26, ACCENT);
        RenderUtil.fill(context, x + 10, y + 10, x + 22, y + 22, 0xFFFFFFFF);

        RenderUtil.drawText(context, textRenderer, name, x + 34, y + 6, TEXT, false);
        String subTrunc = sub.length() > 50 ? sub.substring(0, 47) + "..." : sub;
        RenderUtil.drawText(context, textRenderer, subTrunc, x + 34, y + 17, DIM, false);

        if (browse && result != null) {
            RenderUtil.fill(context, x + w - 70, y + 8, x + w - 8, y + 24, 0x403B5BDB);
            RenderUtil.drawBorder(context, x + w - 70, y + 8, 62, 16, ACCENT);
            RenderUtil.drawText(context, textRenderer, "Install", x + w - 58, y + 12, 0xFF748FFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Tabs
        if (mouseY < 30) {
            if (mouseX >= 80 && mouseX < 140) { browseMode = false; return true; }
            if (mouseX >= 150 && mouseX < 260) { browseMode = true; return true; }
        }

        // Restart button
        if (restartNeeded && mouseY >= 40 && mouseY <= 58 && mouseX >= width - 80) {
            // Best-effort restart: close the game. Launcher must reopen.
            // Full auto-relaunch is launcher-specific and not reliable from inside the client.
            if (client != null) client.scheduleStop();
            return true;
        }

        if (browseMode) {
            int listY = restartNeeded ? 90 : 68;
            int y = listY;
            for (ModrinthApi.ModResult mod : results) {
                if (y > height - 30) break;
                int rowRight = width - 16;
                if (mouseX >= rowRight - 70 && mouseX <= rowRight - 8 && mouseY >= y + 8 && mouseY <= y + 24) {
                    status = "Installing " + mod.title + "...";
                    ModInstaller.install(mod, ok -> {
                        if (ok) {
                            status = "Installed " + mod.title;
                            restartNeeded = true;
                        } else {
                            status = "Install failed";
                        }
                    });
                    return true;
                }
                y += 36;
            }
        }

        return searchField.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            client.setScreen(parent);
            return true;
        }
        return searchField.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return searchField.charTyped(chr, modifiers) || super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
