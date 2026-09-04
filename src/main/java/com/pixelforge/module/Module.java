package com.pixelforge.module;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.config.ConfigManager;
import com.pixelforge.hud.HudRenderer;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Base client module with lifecycle, settings and shared runtime behaviors. */
public abstract class Module {
    protected final MinecraftClient mc=MinecraftClient.getInstance();
    private final String name; private final String description; private final Category category; private boolean enabled; private int keybind;
    private final List<Setting<?>> settings=new ArrayList<>();
    private Double previousGamma; private Integer previousFov; private int cleanerTicks; private boolean previousLeft; private int cpsClicks; private long cpsWindow; private boolean previousUse; private boolean previousAttack;
    public Module(String name,String description,Category category){this.name=name;this.description=description;this.category=category;this.enabled=false;this.keybind=-1;}
    public void toggle(){setEnabled(!enabled);}
    public void setEnabled(boolean enabled){if(this.enabled==enabled)return;this.enabled=enabled;try{if(enabled)onEnable();else onDisable();}catch(Throwable t){PixelForgeClient.LOGGER.error("Module {} lifecycle failed",name,t);}if(PixelForgeClient.getInstance()!=null&&PixelForgeClient.getInstance().getNotificationManager()!=null)PixelForgeClient.getInstance().getNotificationManager().push(name+(enabled?" enabled":" disabled"),enabled?0x55FF55:0xFF5555);try{ConfigManager.saveModule(this);}catch(Throwable ignored){}}
    private String id(){return name.replaceAll("[^A-Za-z0-9]","").toLowerCase(Locale.ROOT);}
    public void onEnable(){if(mc==null)return;if(id().equals("fullbright")){previousGamma=mc.options.getGamma().getValue();mc.options.getGamma().setValue(16.0);}if(id().equals("fovchanger")){previousFov=mc.options.getFov().getValue();mc.options.getFov().setValue(110);}if(id().equals("cpstrainer")){cpsClicks=0;cpsWindow=System.currentTimeMillis();}}
    public void onDisable(){if(mc==null)return;if(id().equals("fullbright")&&previousGamma!=null){mc.options.getGamma().setValue(previousGamma);previousGamma=null;}if(id().equals("fovchanger")&&previousFov!=null){mc.options.getFov().setValue(previousFov);previousFov=null;}if(id().equals("togglesprint")&&mc.player!=null)mc.player.setSprinting(false);if(id().equals("togglesneak")&&mc.player!=null)mc.player.setSneaking(false);}
    public void onTick(){
        if(mc==null||mc.player==null)return;
        switch(id()){
            case "togglesprint" -> {if(!mc.player.isSneaking()&&mc.options.forwardKey.isPressed())mc.player.setSprinting(true);}
            case "togglesneak" -> mc.player.setSneaking(true);
            case "autorespawn" -> {if(mc.player.isDead())mc.player.requestRespawn();}
            case "memorycleaner" -> {if(++cleanerTicks>=200){cleanerTicks=0;System.gc();}}
            case "autotool" -> autoTool();
            case "fastplace" -> setItemUseCooldown(0);
            case "fullbright" -> {if(mc.options.getGamma().getValue()<16.0)mc.options.getGamma().setValue(16.0);}
            case "fovchanger" -> {if(mc.options.getFov().getValue()!=110)mc.options.getFov().setValue(110);}
            case "noparticles" -> setMinimalParticles();
            case "cpstrainer" -> tickCpsTrainer();
            case "blockhittrainer" -> tickBlockHitTrainer();
            case "wtaptrainer" -> tickWTapTrainer();
            case "strafetrainer" -> tickStrafeTrainer();
            case "aimtrainer" -> tickAimTrainer();
            default -> {}
        }
    }
    private void autoTool(){if(mc.world==null||!(mc.crosshairTarget instanceof BlockHitResult hit))return;var state=mc.world.getBlockState(hit.getBlockPos());int best=mc.player.getInventory().getSelectedSlot();float speed=-1f;for(int i=0;i<9;i++){ItemStack s=mc.player.getInventory().getStack(i);if(s.isEmpty())continue;float v=s.getMiningSpeedMultiplier(state);if(v>speed){speed=v;best=i;}}if(best>=0)mc.player.getInventory().setSelectedSlot(best);}
    private void setItemUseCooldown(int value){try{var f=MinecraftClient.class.getDeclaredField("itemUseCooldown");f.setAccessible(true);f.setInt(mc,value);}catch(Throwable ignored){}}
    private void setMinimalParticles(){try{var option=mc.options.getParticles();if(option.getValue()!=ParticlesMode.MINIMAL)option.setValue(ParticlesMode.MINIMAL);}catch(Throwable ignored){}}
    private void tickCpsTrainer(){long now=System.currentTimeMillis();boolean left=GLFW.glfwGetMouseButton(mc.getWindow().getHandle(),GLFW.GLFW_MOUSE_BUTTON_LEFT)==GLFW.GLFW_PRESS;if(left&&!previousLeft)cpsClicks++;previousLeft=left;if(cpsWindow==0)cpsWindow=now;if(now-cpsWindow>=1000){cpsWindow=now;cpsClicks=0;}}
    private void tickBlockHitTrainer(){boolean use=mc.options.useKey.isPressed();if(use&&!previousUse&&mc.player.isUsingItem())mc.player.swingHand(Hand.MAIN_HAND);previousUse=use;}
    private void tickWTapTrainer(){if(mc.options.forwardKey.isPressed()&&mc.player.isSprinting()&&mc.player.horizontalCollision)mc.player.setSprinting(false);}
    private void tickStrafeTrainer(){boolean strafe=mc.options.leftKey.isPressed()||mc.options.rightKey.isPressed();if(mc.player.isOnGround()&&mc.options.forwardKey.isPressed()&&strafe){if(!mc.player.isSprinting())mc.player.setSprinting(true);}}
    private void tickAimTrainer(){boolean attack=mc.options.attackKey.isPressed();if(mc.crosshairTarget!=null&&attack&&!previousAttack)mc.player.swingHand(Hand.MAIN_HAND);previousAttack=attack;}
    @Override public String toString(){return name;}
    public void onRender(DrawContext context,float tickDelta){
        if(category!=Category.TRAINER||!enabled)return;
        if(mc==null||mc.textRenderer==null)return;
        int x=HudRenderer.getX(name), y=HudRenderer.getY(name);
        String text;
        if(id().equals("cpstrainer"))text="CPS Trainer · "+cpsClicks+"/s";
        else if(id().equals("aimtrainer"))text="Aim Trainer";
        else if(id().equals("wtaptrainer"))text="W-Tap Trainer";
        else if(id().equals("blockhittrainer"))text="Block-Hit Trainer";
        else if(id().equals("strafetrainer"))text="Strafe Trainer";
        else if(id().equals("knockbacktrainer"))text="Knockback Trainer";
        else if(id().equals("pingsimulator"))text="Ping Simulator";
        else text=name;
        RenderUtil.drawHudBox(context,mc.textRenderer,text,x,y,0xFFC8D0E0);
    }
    public String getName(){return name;}public String getDescription(){return description;}public Category getCategory(){return category;}public boolean isEnabled(){return enabled;}public int getKeybind(){return keybind;}public void setKeybind(int keybind){this.keybind=keybind;}public List<Setting<?>> getSettings(){return settings;}
    protected <T> Setting<T> addSetting(Setting<T> setting){settings.add(setting);return setting;}
    public static class Setting<T> {
        private final String name;
        private T value;
        private final T defaultValue;
        private double min = 0;
        private double max = 100;

        public Setting(String name, T defaultValue) {
            this.name = name;
            this.value = defaultValue;
            this.defaultValue = defaultValue;
            if (defaultValue instanceof Number n) {
                double v = Math.abs(n.doubleValue());
                if (v <= 1) { min = 0; max = 1; }
                else if (v <= 10) { min = 0; max = 20; }
                else if (v <= 100) { min = 0; max = 200; }
                else if (v <= 1000) { min = 0; max = 2000; }
                else { min = 0; max = v * 2; }
            }
        }

        public Setting(String name, T defaultValue, double min, double max) {
            this.name = name;
            this.value = defaultValue;
            this.defaultValue = defaultValue;
            this.min = min;
            this.max = max;
        }

        public String getName() { return name; }
        public T get() { return value; }
        public void set(T value) { this.value = value; }
        public T getDefault() { return defaultValue; }
        public double getMin() { return min; }
        public double getMax() { return max; }
        public Setting<T> range(double min, double max) { this.min = min; this.max = max; return this; }
    }
}
