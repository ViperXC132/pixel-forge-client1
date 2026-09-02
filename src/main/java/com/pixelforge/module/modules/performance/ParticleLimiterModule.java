package com.pixelforge.module.modules.performance;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import net.minecraft.particle.ParticlesMode;

/** Lightweight particle limiter using Minecraft's supported particle quality setting. */
public class ParticleLimiterModule extends Module {
    private ParticlesMode previous;
    public ParticleLimiterModule(){super("Particle Limiter","Limits particle density to reduce render load",Category.PERFORMANCE);}
    @Override public void onEnable(){
        super.onEnable();
        try{previous=mc.options.getParticles().getValue();mc.options.getParticles().setValue(ParticlesMode.MINIMAL);}catch(Throwable ignored){}
    }
    @Override public void onDisable(){
        try{if(previous!=null)mc.options.getParticles().setValue(previous);previous=null;}catch(Throwable ignored){}
        super.onDisable();
    }
}
