package com.pixelforge.module.modules.trainer;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

/** W-tap practice coach. It measures sprint-release and re-press timing without auto-playing for you. */
public class WTapTrainerModule extends Module {
    private boolean lastForward,lastAttack,lastSprint;
    private long sprintReleaseAt,attackAt;
    private int attempts,good,late;
    private final Setting<Integer> idealWindow=addSetting(new Setting<>("Ideal window ms",180));
    private final Setting<Boolean> showHud=addSetting(new Setting<>("Practice HUD",true));
    public WTapTrainerModule(){super("W-Tap Trainer","Helps practice W-tapping and S-tapping",Category.TRAINER);}
    @Override public void onEnable(){lastForward=false;lastAttack=false;lastSprint=false;sprintReleaseAt=0;attackAt=0;attempts=0;good=0;late=0;}
    @Override public void onTick(){
        if(mc==null||mc.player==null)return;
        boolean forward=mc.options.forwardKey.isPressed();
        boolean attack=mc.options.attackKey.isPressed();
        boolean sprint=mc.player.isSprinting();
        long now=System.currentTimeMillis();
        if(lastForward&&!forward){sprintReleaseAt=now;}
        if(attack&&!lastAttack){
            attempts++;attackAt=now;
            if(sprintReleaseAt>0){long delta=now-sprintReleaseAt;if(delta<=Math.max(30,idealWindow.get()))good++;else late++;}
        }
        lastForward=forward;lastAttack=attack;lastSprint=sprint;
    }
    @Override public void onRender(DrawContext c,float tickDelta){
        if(!isEnabled()||!showHud.get())return;
        int x=8,y=72,w=220,h=58;RenderUtil.fill(c,x,y,x+w,y+h,0xB0101424);RenderUtil.drawBorder(c,x,y,w,h,0xFF6D7CFF);
        RenderUtil.drawText(c,mc.textRenderer,"W-TAP PRACTICE",x+8,y+7,0xFFEAF0FF,false);
        RenderUtil.drawText(c,mc.textRenderer,"Attempts: "+attempts+"  Good: "+good,x+8,y+22,0xFF9AA7C0,false);
        RenderUtil.drawText(c,mc.textRenderer,"Late: "+late+"  Window: "+idealWindow.get()+"ms",x+8,y+37,late==0?0xFF5BE58A:0xFFFF6575,false);
    }
    public int getAttempts(){return attempts;}
    public int getGood(){return good;}
    public int getLate(){return late;}
    public long getLastAttackAt(){return attackAt;}
    public boolean wasSprinting(){return lastSprint;}
}
