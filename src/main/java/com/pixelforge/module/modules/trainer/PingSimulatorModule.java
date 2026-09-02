package com.pixelforge.module.modules.trainer;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;

public class PingSimulatorModule extends Module {

    private int simulatedPing = 50;

    public PingSimulatorModule() {
        super("Ping Simulator", "Simulates higher ping for training (singleplayer)", Category.TRAINER);
    }

    public int getSimulatedPing() { return simulatedPing; }
    public void setSimulatedPing(int simulatedPing) { this.simulatedPing = simulatedPing; }
}
