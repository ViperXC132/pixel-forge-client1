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
    private TextFieldWidget userField, passField;
    private AccountType selectedType = AccountType.OFFLINE;
    private String status = "";
    private boolean busy;
    private static final int ACCENT = 0xFF3B5BDB, TEXT = 0xFFC8D0E0, DIM = 0xFF8892A8, MUTED = 0xFF3D4A6A, PANEL = 0xD0101424, RED = 0xFFFA5252;

    public AccountsScreen(Screen parent) {
        super(Text.literal("Accounts"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        userField = new TextFieldWidget(textRenderer, 20, 0, width - 40, 20, Text.literal("Username"));
        userField.setPlaceholder(Text.literal("Username / email"));
        userField.setMaxLength(64);
        passField = new TextFieldWidget(textRenderer, 20, 0, width - 40, 20, Text.literal("Password"));
        passField.setPlaceholder(Text.literal("Password (empty for Offline)"));
        passField.setMaxLength(128);
        addDrawableChild(userField);
        addDrawableChild(passField);
        setFocused(userField);
    }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        RenderUtil.fill(c, 0, 0, width, height, 0xFF080A12);
        RenderUtil.fill(c, 0, 0, width, 34, 0xE00A0C14);
        RenderUtil.drawText(c, textRenderer, "Accounts", 16, 11, TEXT, false);
        RenderUtil.drawText(c, textRenderer, "Active: " + SessionApplier.currentUsername(), 118, 11, 0xFF748FFF, false);
        RenderUtil.drawText(c, textRenderer, "SAVED ACCOUNTS", 16, 44, ACCENT, false);

        int y = 58;
        for (Account a : AccountManager.getAccounts()) {
            RenderUtil.fill(c, 16, y, width - 16, y + 32, PANEL);
            RenderUtil.drawBorder(c, 16, y, width - 32, 32, 0xFF1E2540);
            SkinHelper.drawHead(c, a.username, 22, y + 6, 20);
            RenderUtil.drawText(c, textRenderer, a.username, 48, y + 5, TEXT, false);
            RenderUtil.drawText(c, textRenderer, a.type.displayName + (a.active ? " · Active" : ""), 48, y + 17, MUTED, false);

            drawAction(c, width - 140, y + 8, 50, 16, "Delete", RED);
            if (!a.active) {
                drawAction(c, width - 82, y + 8, 58, 16, "Switch", ACCENT);
            } else {
                RenderUtil.fill(c, width - 30, y + 13, width - 24, y + 19, 0xFF40C057);
            }
            y += 36;
        }

        y += 8;
        RenderUtil.drawText(c, textRenderer, "ADD / LOGIN", 16, y, ACCENT, false);
        y += 15;
        drawTypeBtn(c, 16, y, "Offline", selectedType == AccountType.OFFLINE);
        drawTypeBtn(c, 70, y, "ely.by", selectedType == AccountType.ELYBY);
        drawTypeBtn(c, 125, y, "LittleSkin", selectedType == AccountType.LITTLESKIN);
        drawTypeBtn(c, 200, y, "Microsoft", selectedType == AccountType.MICROSOFT);

        int fieldY = y + 21;
        userField.setY(fieldY);
        passField.setY(fieldY + 27);
        userField.render(c, mx, my, d);
        passField.render(c, mx, my, d);
        if (!userField.getText().isBlank()) {
            SkinHelper.drawHead(c, userField.getText().trim(), width - 48, fieldY + 1, 22);
        }

        int by = fieldY + 54;
        drawAction(c, 20, by, width - 40, 20, busy ? "Working..." : "Login & Apply Session", busy ? MUTED : ACCENT);
        if (!status.isEmpty()) {
            RenderUtil.drawText(c, textRenderer, status.startsWith("OK:") ? status.substring(3) : status, 20, by + 27,
                    status.startsWith("OK:") ? 0xFF40C057 : 0xFFFA5252, false);
        }
        RenderUtil.drawText(c, textRenderer, "Delete removes a saved account. Active sessions stay until you switch.", 12, height - 14, MUTED, false);
        super.render(c, mx, my, d);
    }

    private void drawTypeBtn(DrawContext c, int x, int y, String label, boolean on) {
        int w = textRenderer.getWidth(label) + 14;
        RenderUtil.fill(c, x, y, x + w, y + 16, on ? 0x403B5BDB : PANEL);
        RenderUtil.drawBorder(c, x, y, w, 16, on ? ACCENT : 0xFF1E2540);
        RenderUtil.drawText(c, textRenderer, label, x + 7, y + 3, on ? 0xFF748FFF : MUTED, false);
    }

    private void drawAction(DrawContext c, int x, int y, int w, int h, String label, int color) {
        RenderUtil.fill(c, x, y, x + w, y + h, 0x203B5BDB);
        RenderUtil.drawBorder(c, x, y, w, h, color);
        RenderUtil.drawCenteredText(c, textRenderer, label, x + w / 2, y + 5, color, false);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        int y = 58;
        for (Account a : AccountManager.getAccounts()) {
            if (mx >= width - 140 && mx <= width - 90 && my >= y + 8 && my <= y + 24) {
                String name = a.username;
                boolean wasActive = a.active;
                AccountManager.remove(a);
                status = "OK: Removed " + name;
                if (wasActive) {
                    status = "OK: Removed " + name + " (switch to another account)";
                }
                return true;
            }
            if (!a.active && mx >= width - 82 && my >= y && my <= y + 32) {
                AccountManager.switchTo(a);
                status = "OK: Switched to " + a.username;
                return true;
            }
            y += 36;
        }

        y += 8;
        int typeY = y + 15;
        if (my >= typeY && my <= typeY + 16) {
            if (mx >= 16 && mx < 65) selectedType = AccountType.OFFLINE;
            else if (mx >= 70 && mx < 120) selectedType = AccountType.ELYBY;
            else if (mx >= 125 && mx < 195) selectedType = AccountType.LITTLESKIN;
            else if (mx >= 200 && mx < 300) selectedType = AccountType.MICROSOFT;
            else return super.mouseClicked(click, doubled);
            setFocused(userField);
            return true;
        }

        int fieldY = typeY + 21;
        int by = fieldY + 54;
        if (!busy && my >= by && my <= by + 20 && mx >= 20 && mx <= width - 20) {
            if (selectedType == AccountType.MICROSOFT) {
                busy = true;
                status = "Opening Microsoft sign-in...";
                AccountManager.loginMicrosoftAsync(msg -> {
                    busy = false;
                    status = msg;
                });
                return true;
            }
            String user = userField.getText().trim(), pass = passField.getText();
            if (user.isEmpty()) {
                status = "Enter a username";
                setFocused(userField);
                return true;
            }
            if (selectedType != AccountType.OFFLINE && pass.isEmpty()) {
                status = "Password required";
                setFocused(passField);
                return true;
            }
            busy = true;
            status = "Authenticating...";
            AccountManager.loginAsync(selectedType, user, pass, msg -> {
                busy = false;
                status = msg;
                if (msg.startsWith("OK:")) passField.setText("");
            });
            return true;
        }

        if (userField.mouseClicked(click, doubled)) {
            setFocused(userField);
            return true;
        }
        if (passField.mouseClicked(click, doubled)) {
            setFocused(passField);
            return true;
        }
        if (click.button() == 0 && my < 34 && mx < 100) {
            client.setScreen(parent);
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput i) {
        if (i.key() == 256) {
            client.setScreen(parent);
            return true;
        }
        if (userField.isFocused() && userField.keyPressed(i)) return true;
        if (passField.isFocused() && passField.keyPressed(i)) return true;
        return super.keyPressed(i);
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharInput i) {
        if (userField.isFocused() && userField.charTyped(i)) return true;
        if (passField.isFocused() && passField.charTyped(i)) return true;
        return super.charTyped(i);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
