package com.pixelforge.module.modules.visual;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class TimeWeatherModule extends Module {

    public enum TimeMode { VANILLA, DAY, NIGHT, NOON, MIDNIGHT }
    public enum WeatherMode { VANILLA, CLEAR, RAIN, THUNDER }

    private TimeMode timeMode = TimeMode.VANILLA;
    private WeatherMode weatherMode = WeatherMode.VANILLA;

    public TimeWeatherModule() {
        super("Time Weather", "Client-side time and weather changer", Category.VISUAL);
    }

    // Real implementation would use mixins on ClientWorld / WorldRenderer.
    // This module stores the desired state.

    public TimeMode getTimeMode() { return timeMode; }
    public void setTimeMode(TimeMode timeMode) { this.timeMode = timeMode; }
    public WeatherMode getWeatherMode() { return weatherMode; }
    public void setWeatherMode(WeatherMode weatherMode) { this.weatherMode = weatherMode; }
}
