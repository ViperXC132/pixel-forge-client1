package com.pixelforge.module;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.config.ConfigManager;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

/** Base client module with lifecycle, settings and shared runtime behaviors. */
public abstract class Module {
    protected final MinecraftClient mc=MinecraftClient.getInstance();
    private final String name; private final String description; private final Category category; private boolean enabled; private int keybind;
    private final List<Setting<?>> settings=new ArrayList<>();
    private Double previousGamma; private Integer previousFov; private int cleanerTicks; private boolean previousLeft; private int cpsClicks; private long cpsWindow; private boolean previousUse; private boolean previousAttack;
    private int autoEatSlot=-1; private int autoEatPreviousSlot=-1;
    public Module(String name,String description,Category category){this.name=name;this.description=description;this.category=category;this.enabled=false;this.keybind=-1;}
    public void toggle(){setEnabled(!enabled);}
    public void setEnabled(boolean enabled){if(this.enabled==enabled)return;this.enabled=enabled;try{if(enabled)onEnable();else onDisable();}catch(Throwable t){PixelForgeClient.LOGGER.error("Module {} lifecycle failed",name,t);}if(PixelForgeClient.getInstance()!=null&&PixelForgeClient.getInstance().getNotificationManager()!=null)PixelForgeClient.getInstance().getNotificationManager().push(name+(enabled?" enabled":" disabled"),enabled?0x55FF55:0xFF5555);try{ConfigManager.saveModule(this);}catch(Throwable ignored){}}
    public void onEnable(){if(mc==null)return;if(name.equals("Fullbright")){previousGamma=mc.options.getGamma().getValue();mc.options.getGamma().setValue(16.0);}if(name.equals("FovChanger")){previousFov=mc.options.getFov().getValue();mc.options.getFov().setValue(110);}if(name.equals("CpsTrainer")){cpsClicks=0;cpsWindow=System.currentTimeMillis();}}
    public void onDisable(){if(mc==null)return;if(name.equals("Fullbright")&&previousGamma!=null){mc.options.getGamma().setValue(previousGamma);previousGamma=null;}if(name.equals("FovChanger")&&previousFov!=null){mc.options.getFov().setValue(previousFov);previousFov=null;}if(name.equals("ToggleSprint")&&mc.player!=null)mc.player.setSprinting(false);if(name.equals("ToggleSneak")&&mc.player!=null)mc.player.setSneaking(false);stopAutoEat();}
    public void onTick(){
        if(mc==null||mc.player==null)return;
        switch(name){
            case "ToggleSprint" -> {if(!mc.player.isSneaking()&&mc.options.forwardKey.isPressed())mc.player.setSprinting(true);}
            case "ToggleSneak" -> mc.player.setSneaking(true);
            case "AutoRespawn" -> {if(mc.player.isDead())mc.player.requestRespawn();}
            case "MemoryCleaner" -> {if(++cleanerTicks>=200){cleanerTicks=0;System.gc();}}
            case "AutoEat" -> autoEat();
            case "AutoTool" -> autoTool();
            case "FastPlace" -> setItemUseCooldown(0);
            case "Fullbright" -> {if(mc.options.getGamma().getValue()<16.0)mc.options.getGamma().setValue(16.0);}
            case "FovChanger" -> {if(mc.options.getFov().getValue()!=110)mc.options.getFov().setValue(110);}
            case "NoParticles" -> setMinimalParticles();
            case "CpsTrainer" -> tickCpsTrainer();
            case "BlockHitTrainer" -> tickBlockHitTrainer();
            case "WTapTrainer" -> tickWTapTrainer();
            case "StrafeTrainer" -> tickStrafeTrainer();
            case "AimTrainer" -> tickAimTrainer();
            default -> {}
        }
    }
    private boolean isFood(ItemStack stack){return !stack.isEmpty()&&stack.get(DataComponentTypes.FOOD)!=null;}
    private void autoEat(){
        if(mc.interactionManager==null||mc.player==null)return;
        if(autoEatSlot>=0){
            ItemStack active=mc.player.getInventory().getStack(autoEatSlot);
            if(mc.player.isUsingItem())return;
            if(!isFood(active)){stopAutoEat();return;}
            if(mc.player.getHungerManager().getFoodLevel()>=20){stopAutoEat();return;}
        } else if(mc.player.isUsingItem()||mc.player.getHungerManager().getFoodLevel()>=14)return;

        int best=-1;
        for(int i=0;i<9;i++){
            ItemStack s=mc.player.getInventory().getStack(i);
            if(isFood(s)){
                best=i;
                break;
            }
        }
        if(best<0)return;

        autoEatPreviousSlot=mc.player.getInventory().getSelectedSlot();
        autoEatSlot=best;
        mc.player.getInventory().setSelectedSlot(best);
        mc.interactionManager.interactItem(mc.player,Hand.MAIN_HAND);
    }
    private void stopAutoEat(){
        if(mc==null||mc.player==null){autoEatSlot=-1;autoEatPreviousSlot=-1;return;}
        if(autoEatSlot>=0&&autoEatPreviousSlot>=0&&autoEatPreviousSlot<9){
            mc.player.getInventory().setSelectedSlot(autoEatPreviousSlot);
        }
        autoEatSlot=-1;
        autoEatPreviousSlot=-1;
    }
    private void autoTool(){if(mc.world==null||!(mc.crosshairTarget instanceof BlockHitResult hit))return;var state=mc.world.getBlockState(hit.getBlockPos());int best=mc.player.getInventory().getSelectedSlot();float speed=-1f;for(int i=0;i<9;i++){ItemStack s=mc.player.getInventory().getStack(i);if(s.isEmpty())continue;float v=s.getMiningSpeedMultiplier(state);if(v>speed){speed=v;best=i;}}if(best>=0)mc.player.getInventory().setSelectedSlot(best);}
    private void setItemUseCooldown(int value){try{var f=MinecraftClient.class.getDeclaredField("itemUseCooldown");f.setAccessible(true);f.setInt(mc,value);}catch(Throwable ignored){}}
    private void setMinimalParticles(){try{var option=mc.options.getParticles();if(option.getValue()==ParticlesMode.ALL)option.setValue(ParticlesMode.MINIMAL);}catch(Throwable ignored){}}
    private void tickCpsTrainer(){long now=System.currentTimeMillis();boolean left=GLFW.glfwGetMouseButton(mc.getWindow().getHandle(),GLFW.GLFW_MOUSE_BUTTON_LEFT)==GLFW.GLFW_PRESS;if(left&&!previousLeft)cpsClicks++;previousLeft=left;if(cpsWindow==0)cpsWindow=now;if(now-cpsWindow>=1000){cpsWindow=now;cpsClicks=0;}}
    private void tickBlockHitTrainer(){boolean use=mc.options.useKey.isPressed();if(use&&!previousUse&&mc.player.isUsingItem())mc.player.swingHand(Hand.MAIN_HAND);previousUse=use;}
    private void tickWTapTrainer(){if(mc.options.forwardKey.isPressed()&&mc.player.isSprinting()&&mc.player.horizontalCollision)mc.player.setSprinting(false);}
    private void tickStrafeTrainer(){boolean strafe=mc.options.leftKey.isPressed()||mc.options.rightKey.isPressed();if(mc.player.isOnGround()&&mc.options.forwardKey.isPressed()&&strafe)mc.player.setSprinting(mc.player.isSprinting());}
    private void tickAimTrainer(){boolean attack=mc.options.attackKey.isPressed();if(mc.crosshairTarget!=null&&attack&&!previousAttack)mc.player.swingHand(Hand.MAIN_HAND);previousAttack=attack;}
    public void onRender(DrawContext context,float tickDelta){if(category!=Category.TRAINER||!enabled)return;int y=8;String text;if(name.equals("CpsTrainer"))text="CPS Trainer · clicks/s: "+cpsClicks;else if(name.equals("AimTrainer"))text="Aim Trainer · keep your crosshair on target";else if(name.equals("WTapTrainer"))text="W-Tap Trainer · sprint timing active";else if(name.equals("BlockHitTrainer"))text="Block-Hit Trainer · practice block timing";else if(name.equals("StrafeTrainer"))text="Strafe Trainer · A/D + forward";else if(name.equals("KnockbackTrainer"))text="Knockback Trainer · movement feedback active";else if(name.equals("PingSimulator"))text="Ping Simulator · training mode";else text=name+" · training mode";int w=Math.min(320,context.getScaledWindowWidth()-16);RenderUtil.fill(context,8,y,8+w,y+16,0xB0101424);RenderUtil.drawBorder(context,8,y,w,16,0xFF3B5BDB);RenderUtil.drawText(context,mc.textRenderer,text,14,y+4,0xFFC8D0E0,false);}
    public String getName(){return name;}public String getDescription(){return description;}public Category getCategory(){return category;}public boolean isEnabled(){return enabled;}public int getKeybind(){return keybind;}public void setKeybind(int keybind){this.keybind=keybind;}public List<Setting<?>> getSettings(){return settings;}
    protected <T> Setting<T> addSetting(Setting<T> setting){settings.add(setting);return setting;}
    public static class Setting<T>{private final String name;private T value;private final T defaultValue;public Setting(String name,T defaultValue){this.name=name;this.value=defaultValue;this.defaultValue=defaultValue;}public String getName(){return name;}public T get(){return value;}public void set(T value){this.value=value;}public T getDefault(){return defaultValue;}}
}
