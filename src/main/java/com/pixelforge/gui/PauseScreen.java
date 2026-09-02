package com.pixelforge.gui;

import com.pixelforge.gui.screens.AccountsScreen;
import com.pixelforge.gui.screens.CrosshairScreen;
import com.pixelforge.gui.screens.ModsScreen;
import com.pixelforge.hud.HudEditor;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.text.Text;
import java.nio.file.Path;

/** Clean in-world pause menu replacing the vanilla button wall. */
public class PauseScreen extends Screen {
    private final Screen parent;
    private static final int BG=0xE60A0E17,PANEL=0xF0141A28,ACCENT=0xFF7182FF,TEXT=0xFFEAF0FF,DIM=0xFF8490AA;
    private int left,top,w,h;

    public PauseScreen(Screen parent){super(Text.literal("PixelForge Pause"));this.parent=parent;}

    @Override protected void init(){layout();}
    private void layout(){w=Math.min(560,width-32);h=Math.min(390,height-32);left=(width-w)/2;top=(height-h)/2;}

    @Override public void render(DrawContext c,int mx,int my,float d){
        layout();
        RenderUtil.fill(c,0,0,width,height,0x66000000);
        RenderUtil.fill(c,left,top,left+w,top+h,BG);
        RenderUtil.drawBorder(c,left,top,w,h,0xFF293454);
        RenderUtil.drawText(c,textRenderer,"PIXELFORGE",left+24,top+22,TEXT,false);
        RenderUtil.drawText(c,textRenderer,"PAUSED",left+24,top+40,ACCENT,false);
        RenderUtil.drawText(c,textRenderer,"Client controls",left+w-100,top+28,DIM,false);
        int bw=238,bh=34,gap=10,x1=left+24,x2=left+w-24-bw,y=top+68;
        button(c,x1,y,bw,bh,"Resume",mx,my);button(c,x2,y,bw,bh,"Mods",mx,my);y+=bh+gap;
        button(c,x1,y,bw,bh,"Accounts",mx,my);button(c,x2,y,bw,bh,"Crosshair",mx,my);y+=bh+gap;
        button(c,x1,y,bw,bh,"HUD Editor",mx,my);button(c,x2,y,bw,bh,"Options",mx,my);y+=bh+gap;
        button(c,x1,y,bw,bh,"Resource Packs",mx,my);button(c,x2,y,bw,bh,"ClickGUI",mx,my);y+=bh+gap+8;
        RenderUtil.drawText(c,textRenderer,"Disconnect",left+24,y+8,DIM,false);
        RenderUtil.drawText(c,textRenderer,"ESC  Resume",left+w-112,y+8,DIM,false);
        super.render(c,mx,my,d);
    }

    private void button(DrawContext c,int x,int y,int bw,int bh,String label,int mx,int my){
        boolean hover=mx>=x&&mx<=x+bw&&my>=y&&my<=y+bh;
        RenderUtil.fill(c,x,y,x+bw,y+bh,hover?0x453B5BDB:0x24151D30);
        RenderUtil.drawBorder(c,x,y,bw,bh,hover?ACCENT:0xFF293454);
        RenderUtil.drawCenteredText(c,textRenderer,label,x+bw/2,y+11,hover?TEXT:DIM,false);
    }

    @Override public boolean mouseClicked(net.minecraft.client.gui.Click click,boolean doubled){
        layout();if(click.button()!=0)return super.mouseClicked(click,doubled);
        int bw=238,bh=34,gap=10,x1=left+24,x2=left+w-24-bw,y=top+68;
        if(hit(click,x1,y,bw,bh)){client.setScreen(parent);return true;}if(hit(click,x2,y,bw,bh)){client.setScreen(new ModsScreen(this));return true;}y+=bh+gap;
        if(hit(click,x1,y,bw,bh)){client.setScreen(new AccountsScreen(this));return true;}if(hit(click,x2,y,bw,bh)){client.setScreen(new CrosshairScreen(this));return true;}y+=bh+gap;
        if(hit(click,x1,y,bw,bh)){client.setScreen(new HudEditor());return true;}if(hit(click,x2,y,bw,bh)){client.setScreen(new OptionsScreen(this,client.options));return true;}y+=bh+gap;
        if(hit(click,x1,y,bw,bh)){openResourcePacks();return true;}if(hit(click,x2,y,bw,bh)){client.setScreen(new ClickGui());return true;}y+=bh+gap+8;
        if(click.x()>=left+20&&click.x()<=left+180&&click.y()>=y&&click.y()<=y+30){client.disconnect();return true;}
        return super.mouseClicked(click,doubled);
    }

    private boolean hit(net.minecraft.client.gui.Click c,int x,int y,int w,int h){return c.x()>=x&&c.x()<=x+w&&c.y()>=y&&c.y()<=y+h;}

    private void openResourcePacks(){
        try{
            Path dir=client.getResourcePackDir();
            client.setScreen(new PackScreen(client.getResourcePackManager(),manager->{client.options.refreshResourcePacks(manager);},dir,Text.literal("Resource Packs")));
        }catch(Throwable t){client.setScreen(new Screen(Text.literal("Resource Packs unavailable")){ });}
    }

    @Override public boolean keyPressed(net.minecraft.client.input.KeyInput i){if(i.key()==256){client.setScreen(parent);return true;}return super.keyPressed(i);}
    @Override public boolean shouldPause(){return true;}
}
