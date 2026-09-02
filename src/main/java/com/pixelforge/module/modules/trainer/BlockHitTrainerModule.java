package com.pixelforge.module.modules.trainer;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Hand;

/** Block-hit timing coach. It records attack/use alternation and never forces a combat action. */
public class BlockHitTrainerModule extends Module {
    private boolean lastAttack,lastUse;
    private long lastAttackAt,lastUseAt;
    private int attacks,uses,good,missed;
    private final Setting<Integer> targetGap=addSetting(new Setting<>("Target gap ms",120));
    private final Setting<Boolean> showHud=addSetting(new Setting<>("Practice HUD",true));
    public BlockHitTrainerModule(){super("Block-Hit Trainer","Timing trainer for block-hitting",Category.TRAINER);}
    @Override public void onEnable(){lastAttack=false;lastUse=false;lastAttackAt=0;lastUseAt=0;attacks=0;uses=0;good=0;missed=0;}
    @Override public void onTick(){
        if(mc==null||mc.player==null)return;
        boolean attack=mc.options.attackKey.isPressed();
        boolean use=mc.options.useKey.isPressed();
        long now=System.currentTimeMillis();
        if(attack&&!lastAttack){attacks++;lastAttackAt=now;}
        if(use&&!lastUse){
            uses++;lastUseAt=now;
            if(lastAttackAt>0){long gap=now-lastAttackAt;if(gap<=Math.max(30,targetGap.get()))good++;else missed++;}
        }
        lastAttack=attack;lastUse=use;
    }
    @Override public void onRender(DrawContext c,float tickDelta){
        if(!isEnabled()||!showHud.get())return;
        int x=8,y=136,w=225,h=58;RenderUtil.fill(c,x,y,x+w,y+h,0xB0101424);RenderUtil.drawBorder(c,x,y,w,h,0xFF6D7CFF);
        RenderUtil.drawText(c,mc.textRenderer,"BLOCK-HIT PRACTICE",x+8,y+7,0xFFEAF0FF,false);
        RenderUtil.drawText(c,mc.textRenderer,"Attacks: "+attacks+"  Uses: "+uses,x+8,y+22,0xFF9AA7C0,false);
        RenderUtil.drawText(c,mc.textRenderer,"Good: "+good+"  Missed: "+missed,x+8,y+37,missed==0?0xFF5BE58A:0xFFFF6575,false);
    }
    public int getAttacks(){return attacks;}
    public int getUses(){return uses;}
    public int getGood(){return good;}
    public int getMissed(){return missed;}
    public long getLastAttackAt(){return lastAttackAt;}
    public long getLastUseAt(){return lastUseAt;}
}
