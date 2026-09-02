package com.pixelforge.hud;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import net.minecraft.client.gui.DrawContext;

import java.util.HashMap;
import java.util.Map;

/** Shared HUD layout positions used by the in-game renderer and HUD editor. */
public class HudRenderer {
    private static final Map<String,int[]> POSITIONS=new HashMap<>();
    private static int nextX=8,nextY=8;

    public static int getX(String name){return position(name)[0];}
    public static int getY(String name){return position(name)[1];}
    public static void setPosition(String name,int x,int y){POSITIONS.put(name,new int[]{Math.max(2,x),Math.max(2,y)});}
    public static int[] position(String name){
        return POSITIONS.computeIfAbsent(name,k->{int[] p={nextX,nextY};nextY+=24;if(nextY>180){nextY=8;nextX+=190;}return p;});
    }
    public static void resetLayout(){POSITIONS.clear();nextX=8;nextY=8;}

    public void render(DrawContext context,float tickDelta){
        PixelForgeClient client=PixelForgeClient.getInstance();
        if(client==null||client.getModuleManager()==null)return;
        for(Module module:client.getModuleManager().getModules()){
            if(!module.isEnabled())continue;
            if(module.getCategory()!=Category.HUD&&module.getCategory()!=Category.TRAINER)continue;
            try{module.onRender(context,tickDelta);}catch(Throwable t){PixelForgeClient.LOGGER.error("HUD module {} failed",module.getName(),t);}
        }
    }
}
