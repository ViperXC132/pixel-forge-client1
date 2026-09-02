package com.pixelforge.hud;

public class HudElement {

    private int x;
    private int y;
    private float scale = 1.0f;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public HudElement(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public float getScale() { return scale; }
    public void setScale(float scale) { this.scale = scale; }

    public boolean isDragging() { return dragging; }
    public void startDrag(int mouseX, int mouseY) {
        dragging = true;
        dragOffsetX = mouseX - x;
        dragOffsetY = mouseY - y;
    }

    public void updateDrag(int mouseX, int mouseY) {
        if (dragging) {
            x = mouseX - dragOffsetX;
            y = mouseY - dragOffsetY;
        }
    }

    public void stopDrag() {
        dragging = false;
    }
}
