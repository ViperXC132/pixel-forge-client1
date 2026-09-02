package com.pixelforge.util;

public final class ColorUtil {

    private ColorUtil() {}

    public static int rgba(int r, int g, int b, int a) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    public static int rgb(int r, int g, int b) {
        return rgba(r, g, b, 255);
    }

    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int getBlue(int color) {
        return color & 0xFF;
    }

    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }

    public static int setAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    public static int lerp(int color1, int color2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (getRed(color1) + (getRed(color2) - getRed(color1)) * t);
        int g = (int) (getGreen(color1) + (getGreen(color2) - getGreen(color1)) * t);
        int b = (int) (getBlue(color1) + (getBlue(color2) - getBlue(color1)) * t);
        int a = (int) (getAlpha(color1) + (getAlpha(color2) - getAlpha(color1)) * t);
        return rgba(r, g, b, a);
    }

    public static int rainbow(float speed) {
        float hue = (System.currentTimeMillis() % (long) (speed * 1000)) / (speed * 1000f);
        return java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f) | 0xFF000000;
    }
}
