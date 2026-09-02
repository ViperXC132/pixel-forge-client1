package com.pixelforge.module.modules.trainer;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

/** Practical strafe practice: detects A/D direction changes, jump timing and missed inputs. */
public class StrafeTrainerModule extends Module {
    private boolean lastLeft,lastRight,lastForward,lastJump;
    private long lastSwitchMs,lastJumpMs;
    private int switches,jumps,cleanSwitches,lateSwitches;
    private double lastDirection;
    private final Setting<Integer> targetWindow=addSetting(new Setting<>("Switch window ms",140));
    private final Setting<Boolean> requireForward=addSetting(new Setting<>("Require forward",true));
    private final Setting<Boolean> showHud=addSetting(new Setting<>("Practice HUD",true));
    public StrafeTrainerModule(){super("Strafe Trainer","Practice A/D strafe direction changes with timing feedback",Category.TRAINER);}
    @Override public void onEnable(){reset();}
    @Override public void onDisable(){reset();}
    private void reset(){lastLeft=false;lastRight=false;lastForward=false;lastJump=false;lastSwitchMs=0;lastJumpMs=0;switches=0;jumps=0;cleanSwitches=0;lateSwitches=0;lastDirection=0;}
    @Override public void onTick(){
        if(mc==null||mc.player==null)return;
        boolean left=mc.options.leftKey.isPressed();
        boolean right=mc.options.rightKey.isPressed();
        boolean forward=mc.options.forwardKey.isPressed();
        boolean jump=mc.options.jumpKey.isPressed();
        long now=System.currentTimeMillis();
        boolean directionChanged=(left&&!lastLeft)||(right&&!lastRight);
        if(directionChanged){
            double direction=right?1:-1;
            if(lastDirection!=0&&direction!=lastDirection){
                switches++;
                long delta=lastSwitchMs==0?0:now-lastSwitchMs;
                if(delta>0&&delta<=Math.max(40,targetWindow.get()))cleanSwitches++;else if(delta>Math.max(40,targetWindow.get()))lateSwitches++;
            }
            lastDirection=direction;lastSwitchMs=now;
        }
        if(jump&&!lastJump){jumps++;lastJumpMs=now;}
        lastLeft=left;lastRight=right;lastForward=forward;lastJump=jump;
    }
    @Override public void onRender(DrawContext c,float tickDelta){
        if(!isEnabled()||!showHud.get())return;
        int x=8,y=8,w=190,h=58;RenderUtil.fill(c,x,y,x+w,y+h,0xB0101424);RenderUtil.drawBorder(c,x,y,w,h,0xFF6D7CFF);
        RenderUtil.drawText(c,mc.textRenderer,"STRAFE PRACTICE",x+8,y+7,0xFFEAF0FF,false);
        RenderUtil.drawText(c,mc.textRenderer,"Switches: "+switches+"  Clean: "+cleanSwitches,x+8,y+22,0xFF9AA7C0,false);
        RenderUtil.drawText(c,mc.textRenderer,"Late: "+lateSwitches+"  Jumps: "+jumps,x+8,y+37,lateSwitches==0?0xFF5BE58A:0xFFFF6575,false);
    }
    public int getSwitches(){return switches;}
    public int getCleanSwitches(){return cleanSwitches;}
    public int getLateSwitches(){return lateSwitches;}
    public int getJumps(){return jumps;}
    public long getLastSwitchMs(){return lastSwitchMs;}
    public boolean isForwardHeld(){return lastForward;}
    public boolean isJumpHeld(){return lastJump;}
}
