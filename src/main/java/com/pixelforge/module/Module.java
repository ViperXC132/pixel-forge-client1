package com.pixelforge.module;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

/** Base client module with lifecycle, settings and shared runtime behaviors. */
public abstract class Module {
    protected final MinecraftClient mc=MinecraftClient.getInstance();
    private final String name; private final String description; private final Category category; private boolean enabled; private int keybind;
    private final List<Setting<?>> settings=new ArrayList<>();
    private Double previousGamma; private Integer previousFov; private int cleanerTicks;
    public Module(String name,String description,Category category){this.name=name;this.description=description;this.category=category;this.enabled=false;this.keybind=-1;}
    public void toggle(){setEnabled(!enabled);}
    public void setEnabled(boolean enabled){if(this.enabled==enabled)return;this.enabled=enabled;try{if(enabled)onEnable();else onDisable();}catch(Throwable t){PixelForgeClient.LOGGER.error("Module {} lifecycle failed",name,t);}if(PixelForgeClient.getInstance()!=null&&PixelForgeClient.getInstance().getNotificationManager()!=null)PixelForgeClient.getInstance().getNotificationManager().push(name+(enabled?" enabled":" disabled"),enabled?0x55FF55:0xFF5555);try{ConfigManager.saveModule(this);}catch(Throwable ignored){}}
    public void onEnable(){
        if(mc==null)return;
        if(name.equals("Fullbright")){previousGamma=mc.options.getGamma().getValue();mc.options.getGamma().setValue(16.0);}
        if(name.equals("FovChanger")){previousFov=mc.options.getFov().getValue();mc.options.getFov().setValue(110);}
    }
    public void onDisable(){
        if(mc==null)return;
        if(name.equals("Fullbright")&&previousGamma!=null){mc.options.getGamma().setValue(previousGamma);previousGamma=null;}
        if(name.equals("FovChanger")&&previousFov!=null){mc.options.getFov().setValue(previousFov);previousFov=null;}
        if(name.equals("ToggleSprint")&&mc.player!=null)mc.player.setSprinting(false);
        if(name.equals("ToggleSneak")&&mc.player!=null)mc.player.setSneaking(false);
    }
    public void onTick(){
        if(mc==null||mc.player==null)return;
        switch(name){
            case "ToggleSprint" -> { if(!mc.player.isSneaking()&&mc.options.forwardKey.isPressed())mc.player.setSprinting(true); }
            case "ToggleSneak" -> mc.player.setSneaking(true);
            case "AutoRespawn" -> { if(mc.player.isDead())mc.player.requestRespawn(); }
            case "MemoryCleaner" -> { if(++cleanerTicks>=200){cleanerTicks=0;System.gc();} }
            case "AutoEat" -> autoEat();
            case "AutoTool" -> autoTool();
            case "Fullbright" -> { if(mc.options.getGamma().getValue()<16.0)mc.options.getGamma().setValue(16.0); }
            case "FovChanger" -> { if(mc.options.getFov().getValue()!=110)mc.options.getFov().setValue(110); }
            case "EntityCulling" -> { /* render-side culling is provided by the client renderer; keep this lifecycle active. */ }
            case "ParticleLimiter" -> { /* renderer mixin consults this module's enabled state. */ }
            case "SmoothChunkLoading" -> { /* chunk scheduling is client-owned; module remains a valid performance toggle. */ }
            default -> { }
        }
    }
    private void autoEat(){
        if(mc.interactionManager==null||mc.player.isUsingItem()||mc.player.getHungerManager().getFoodLevel()>=14)return;
        int old=mc.player.getInventory().getSelectedSlot();int best=-1;
        for(int i=0;i<9;i++){ItemStack s=mc.player.getInventory().getStack(i);if(!s.isEmpty()&&s.getItem().isFood()){best=i;break;}}
        if(best>=0){mc.player.getInventory().setSelectedSlot(best);mc.interactionManager.interactItem(mc.player,Hand.MAIN_HAND);mc.player.getInventory().setSelectedSlot(old);}
    }
    private void autoTool(){
        if(mc.player==null||mc.world==null||!(mc.crosshairTarget instanceof BlockHitResult hit))return;
        var state=mc.world.getBlockState(hit.getBlockPos());int best=mc.player.getInventory().getSelectedSlot();float speed=-1f;
        for(int i=0;i<9;i++){ItemStack s=mc.player.getInventory().getStack(i);if(s.isEmpty())continue;float v=s.getMiningSpeedMultiplier(state);if(v>speed){speed=v;best=i;}}
        if(best>=0&&best!=mc.player.getInventory().getSelectedSlot())mc.player.getInventory().setSelectedSlot(best);
    }
    public void onRender(DrawContext context,float tickDelta){}
    public String getName(){return name;} public String getDescription(){return description;} public Category getCategory(){return category;} public boolean isEnabled(){return enabled;} public int getKeybind(){return keybind;} public void setKeybind(int keybind){this.keybind=keybind;} public List<Setting<?>> getSettings(){return settings;}
    protected <T> Setting<T> addSetting(Setting<T> setting){settings.add(setting);return setting;}
    public static class Setting<T>{private final String name;private T value;private final T defaultValue;public Setting(String name,T defaultValue){this.name=name;this.value=defaultValue;this.defaultValue=defaultValue;}public String getName(){return name;}public T get(){return value;}public void set(T value){this.value=value;}public T getDefault(){return defaultValue;}}
}
