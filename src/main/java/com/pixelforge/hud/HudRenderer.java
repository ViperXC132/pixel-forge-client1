package com.pixelforge.hud;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import net.minecraft.client.gui.DrawContext;

public class HudRenderer {
    public void render(DrawContext context,float tickDelta){
        PixelForgeClient client=PixelForgeClient.getInstance();
        if(client==null||client.getModuleManager()==null)return;
        for(Module module:client.getModuleManager().getModules()){
            if(!module.isEnabled())continue;
            if(module.getCategory()!=Category.HUD&&module.getCategory()!=Category.TRAINER)continue;
            try{module.onRender(context,tickDelta);}catch(Throwable t){PixelForgeClient.LOGGER.error("HUD module {} failed; disabling it",module.getName(),t);try{module.setEnabled(false);}catch(Throwable ignored){}}
        }
    }
}
