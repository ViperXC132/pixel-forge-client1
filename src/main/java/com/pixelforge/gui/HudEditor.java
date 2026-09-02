package com.pixelforge.gui;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.hud.HudRenderer;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

/** Drag-and-drop HUD editor. Every HUD/trainer module gets a movable rectangle. */
public class HudEditor extends Screen {
    private Module dragging;
    private int dragOffsetX,dragOffsetY;
    private final List<Module> hudModules;
    private static final int ACCENT=0xFF3B5BDB,TEXT=0xFFE8ECFF,DIM=0xFF7F8AA4;

    public HudEditor(){
        super(Text.literal("PixelForge HUD Editor"));
        hudModules=PixelForgeClient.getInstance().getModuleManager().getModules().stream()
                .filter(m->m.getCategory()==Category.HUD||m.getCategory()==Category.TRAINER).toList();
    }

    @Override public void render(DrawContext c,int mx,int my,float d){
        RenderUtil.fill(c,0,0,width,height,0xB0080B12);
        RenderUtil.fill(c,0,0,width,34,0xF00A0E18);RenderUtil.drawText(c,textRenderer,"HUD EDITOR",14,11,TEXT,false);
        RenderUtil.drawText(c,textRenderer,"Drag rectangles to position them · click to toggle",130,11,DIM,false);
        for(Module m:hudModules){
            int x=HudRenderer.getX(m.getName()),y=HudRenderer.getY(m.getName());int w=Math.min(260,width-x-8);if(w<120)w=120;
            boolean active=m.isEnabled(), hover=mx>=x&&mx<=x+w&&my>=y&&my<=y+22;
            RenderUtil.fill(c,x,y,x+w,y+22,active?0x503B5BDB:0x30101828);
            RenderUtil.drawBorder(c,x,y,w,22,hover?0xFF748FFF:0xFF293454);
            RenderUtil.drawText(c,textRenderer,m.getName(),x+7,y+7,active?TEXT:DIM,false);
            RenderUtil.drawText(c,textRenderer,active?"ON":"OFF",x+w-30,y+7,active?0xFF55E58A:0xFFFF6666,false);
        }
        RenderUtil.fill(c,12,height-34,112,height-14,0x203B5BDB);RenderUtil.drawBorder(c,12,height-34,100,20,ACCENT);RenderUtil.drawText(c,textRenderer,"Reset layout",28,height-28,0xFF748FFF,false);
        RenderUtil.drawText(c,textRenderer,"ESC close",12,height-12,DIM,false);
        super.render(c,mx,my,d);
    }

    @Override public boolean mouseClicked(Click click,boolean doubled){
        int mx=(int)click.x(),my=(int)click.y();
        if(click.button()==0&&mx>=12&&mx<=112&&my>=height-34&&my<=height-14){HudRenderer.resetLayout();return true;}
        if(click.button()==0){
            for(Module m:hudModules){int x=HudRenderer.getX(m.getName()),y=HudRenderer.getY(m.getName());int w=Math.min(260,width-x-8);if(w<120)w=120;if(mx>=x&&mx<=x+w&&my>=y&&my<=y+22){dragging=m;dragOffsetX=mx-x;dragOffsetY=my-y;m.toggle();return true;}}
        }
        return super.mouseClicked(click,doubled);
    }

    @Override public boolean mouseDragged(Click click,double offsetX,double offsetY){
        if(dragging!=null&&click.button()==0){int x=(int)click.x()-dragOffsetX,y=(int)click.y()-dragOffsetY;HudRenderer.setPosition(dragging.getName(),Math.max(2,Math.min(width-122,x)),Math.max(38,Math.min(height-30,y)));return true;}
        return super.mouseDragged(click,offsetX,offsetY);
    }
    @Override public boolean mouseReleased(Click click){if(click.button()==0){dragging=null;return true;}return super.mouseReleased(click);}
    @Override public boolean keyPressed(net.minecraft.client.input.KeyInput i){if(i.key()==256){close();return true;}return super.keyPressed(i);}
    @Override public boolean shouldPause(){return false;}
}
