package com.pixelforge.gui.screens;

import com.pixelforge.account.AccountManager;
import com.pixelforge.account.AccountManager.Account;
import com.pixelforge.account.AccountManager.AccountType;
import com.pixelforge.account.SessionApplier;
import com.pixelforge.account.SkinHelper;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AccountsScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget userField;
    private TextFieldWidget passField;
    private AccountType selectedType = AccountType.OFFLINE;
    private String status = "";
    private boolean busy = false;

    private static final int ACCENT = 0xFF3B5BDB;
    private static final int TEXT = 0xFFC8D0E0;
    private static final int DIM = 0xFF8892A8;
    private static final int MUTED = 0xFF3D4A6A;
    private static final int PANEL = 0xD0101424;

    public AccountsScreen(Screen parent) {
        super(Text.literal("Accounts"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        userField = new TextFieldWidget(textRenderer, 20, 0, width - 40, 18, Text.literal("User"));
        userField.setPlaceholder(Text.literal("Username / email"));
        userField.setMaxLength(64);
        addSelectableChild(userField);

        passField = new TextFieldWidget(textRenderer, 20, 0, width - 40, 18, Text.literal("Pass"));
        passField.setPlaceholder(Text.literal("Password (empty for Offline)"));
        passField.setMaxLength(128);
        // Mask password visually by using a simple render — TextFieldWidget doesn't always expose setRenderTextProvider on all versions
        addSelectableChild(passField);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderUtil.fill(context, 0, 0, width, height, 0xB0080A12);

        RenderUtil.fill(context, 0, 0, width, 32, 0xE00A0C14);
        RenderUtil.drawText(context, textRenderer, "Accounts", 14, 11, TEXT, false);
        RenderUtil.drawText(context, textRenderer, "Active: " + SessionApplier.currentUsername(), 120, 11, 0xFF748FFF, false);

        RenderUtil.drawText(context, textRenderer, "SAVED ACCOUNTS — click Switch to apply session", 16, 42, ACCENT, false);

        int y = 56;
        for (Account acc : AccountManager.getAccounts()) {
            RenderUtil.fill(context, 16, y, width - 16, y + 30, PANEL);
            RenderUtil.drawBorder(context, 16, y, width - 32, 30, 0xFF1E2540);

            SkinHelper.drawHead(context, acc.username, 22, y + 5, 20);
            RenderUtil.drawText(context, textRenderer, acc.username, 48, y + 5, TEXT, false);
            RenderUtil.drawText(context, textRenderer,
                    acc.type.displayName + (acc.active ? " · Active" : ""),
                    48, y + 16, MUTED, false);

            if (!acc.active) {
                RenderUtil.drawText(context, textRenderer, "Switch", width - 58, y + 11, ACCENT, false);
            } else {
                RenderUtil.fill(context, width - 28, y + 12, width - 22, y + 18, 0xFF40C057);
            }
            y += 34;
        }

        y += 6;
        RenderUtil.drawText(context, textRenderer, "LOGIN / ADD", 16, y, ACCENT, false);
        y += 14;

        drawTypeBtn(context, 16, y, "Offline", selectedType == AccountType.OFFLINE);
        drawTypeBtn(context, 70, y, "ely.by", selectedType == AccountType.ELYBY);
        drawTypeBtn(context, 125, y, "LittleSkin", selectedType == AccountType.LITTLESKIN);
        drawTypeBtn(context, 200, y, "Microsoft", selectedType == AccountType.MICROSOFT);

        userField.setY(y + 20);
        passField.setY(y + 42);
        userField.render(context, mouseX, mouseY, delta);
        passField.render(context, mouseX, mouseY, delta);

        String typed = userField.getText().trim();
        if (!typed.isEmpty()) {
            SkinHelper.drawHead(context, typed, width - 48, y + 24, 24);
        }

        int by = y + 68;
        RenderUtil.fill(context, 20, by, width - 20, by + 18, busy ? 0x40333333 : 0x403B5BDB);
        RenderUtil.drawBorder(context, 20, by, width - 40, 18, busy ? MUTED : ACCENT);
        RenderUtil.drawCenteredText(context, textRenderer,
                busy ? "Logging in..." : "Login & Apply Session",
                width / 2, by + 5, busy ? DIM : 0xFF748FFF, false);

        if (!status.isEmpty()) {
            int sc = status.startsWith("OK:") ? 0xFF40C057 : 0xFFFA5252;
            String show = status.startsWith("OK:") ? status.substring(3) : status;
            RenderUtil.drawText(context, textRenderer, show, 20, by + 24, sc, false);
        }

        RenderUtil.drawText(context, textRenderer,
                "Offline: username only  ·  ely.by / LittleSkin: user + password  ·  2FA: password:CODE",
                12, height - 14, MUTED, false);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawTypeBtn(DrawContext context, int x, int y, String label, boolean on) {
        int tw = textRenderer.getWidth(label) + 12;
        RenderUtil.fill(context, x, y, x + tw, y + 14, on ? 0x303B5BDB : PANEL);
        RenderUtil.drawBorder(context, x, y, tw, 14, on ? ACCENT : 0xFF1E2540);
        RenderUtil.drawText(context, textRenderer, label, x + 6, y + 3, on ? 0xFF748FFF : MUTED, false);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        int y = 56;
        for (Account acc : AccountManager.getAccounts()) {
            if (!acc.active && mouseX >= width - 70 && mouseX <= width - 16 && mouseY >= y && mouseY <= y + 30) {
                AccountManager.switchTo(acc);
                status = "OK:Switched to " + acc.username;
                return true;
            }
            y += 34;
        }

        y += 20;
        if (mouseY >= y && mouseY <= y + 14) {
            if (mouseX >= 16 && mouseX < 65) { selectedType = AccountType.OFFLINE; return true; }
            if (mouseX >= 70 && mouseX < 120) { selectedType = AccountType.ELYBY; return true; }
            if (mouseX >= 125 && mouseX < 195) { selectedType = AccountType.LITTLESKIN; return true; }
            if (mouseX >= 200 && mouseX < 270) { selectedType = AccountType.MICROSOFT; return true; }
        }

        int by = y + 68;
        if (!busy && mouseY >= by && mouseY <= by + 18 && mouseX >= 20 && mouseX <= width - 20) {
            String user = userField.getText().trim();
            String pass = passField.getText();
            if (user.isEmpty()) {
                status = "Enter a username";
                return true;
            }
            if (selectedType == AccountType.MICROSOFT) {
                status = "Microsoft needs browser OAuth — use Offline / ely.by / LittleSkin";
                return true;
            }
            if (selectedType != AccountType.OFFLINE && (pass == null || pass.isEmpty())) {
                status = "Password required for " + selectedType.displayName;
                return true;
            }
            busy = true;
            status = "Authenticating...";
            AccountManager.loginAsync(selectedType, user, pass == null ? "" : pass, msg -> {
                busy = false;
                status = msg;
                if (msg.startsWith("OK:")) {
                    passField.setText("");
                }
            });
            return true;
        }

        return userField.mouseClicked(click, doubled)
                || passField.mouseClicked(click, doubled)
                || super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        int keyCode = input.key();
        int scanCode = input.scancode();
        int modifiers = input.modifiers();
        if (keyCode == 256) {
            client.setScreen(parent);
            return true;
        }
        return userField.keyPressed(input)
                || passField.keyPressed(input)
                || super.keyPressed(input);
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharInput input) {
        char chr = (char) input.codepoint();
        int modifiers = input.modifiers();
        return userField.charTyped(input)
                || passField.charTyped(input)
                || super.charTyped(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
