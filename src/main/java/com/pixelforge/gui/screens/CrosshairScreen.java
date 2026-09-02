package com.pixelforge.gui.screens;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.modules.visual.CustomCrosshairModule;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Visual crosshair builder with a live preview and drag-to-edit controls. */
public class CrosshairScreen extends Screen {
    private final Screen parent; private int activeSlider=-1;
    private static final int ACCENT=0xFF3B5BDB,TEXT=0xFFE8ECFF,DIM=0xFF8993AC,PANEL=0xE8141928;
    private static final int[] COLORS={0xFFFFFFFF,0xFFFF5555,0xFF55FF55,0xFF5555FF,0xFFFFFF55,0xFFFF55FF,0xFF55FFFF,0xFFFFAA00};
    private static final String[] STYLE_NAMES={"Cross","Dot","Circle","Cross + Dot","Gap","Custom"};
    public CrosshairScreen(Screen parent){super(Text.literal("PixelForge Crosshair Editor"));this.parent=parent;}
    private CustomCrosshairModule mod(){if(PixelForgeClient.getInstance()==null)return null;return PixelForgeClient.getInstance().getModuleManager().getModule(CustomCrosshairModule.class);}
    @Override public void render(DrawContext c,int mx,int my,float d){
        RenderUtil.fill(c,0,0,width,height,0xFF080B12);RenderUtil.fill(c,0,0,width,36,0xF00A0E18);RenderUtil.drawText(c,textRenderer,"CROSSHAIR EDITOR",16,12,TEXT,false);
        CustomCrosshairModule m=mod();if(m==null){RenderUtil.drawText(c,textRenderer,"Custom Crosshair module is unavailable",16,54,0xFFFF6666,false);return;}
        RenderUtil.fill(c,16,50,206,height-16,PANEL);RenderUtil.drawBorder(c,16,50,190,height-66,0xFF1E2540);RenderUtil.drawText(c,textRenderer,"STYLE",28,62,ACCENT,false);
        int sy=78;for(int i=0;i<STYLE_NAMES.length;i++){boolean on=m.getStyle()==CustomCrosshairModule.Style.values()[i],hover=mx>=26&&mx<=196&&my>=sy&&my<sy+24;RenderUtil.fill(c,26,sy,196,sy+22,on?0x403B5BDB:(hover?0x201E2540:0x10101828));RenderUtil.drawBorder(c,26,sy,170,22,on?ACCENT:0xFF1E2540);RenderUtil.drawText(c,textRenderer,STYLE_NAMES[i],36,sy+7,on?0xFF748FFF:DIM,false);sy+=27;}
        int prevX=230,prevY=54,prevW=150;RenderUtil.fill(c,prevX,prevY,prevX+prevW,prevY+150,0xFF0D1220);RenderUtil.drawBorder(c,prevX,prevY,prevW,150,0xFF1E2540);RenderUtil.drawText(c,textRenderer,"LIVE PREVIEW",prevX+12,prevY+10,ACCENT,false);RenderUtil.fill(c,prevX+75,prevY+30,prevX+76,prevY+130,0x16FFFFFF);RenderUtil.fill(c,prevX+25,prevY+79,prevX+125,prevY+80,0x16FFFFFF);m.renderCrosshair(c,prevX+75,prevY+80);
        int px=400,py=54;RenderUtil.fill(c,px,py,width-16,height-16,PANEL);RenderUtil.drawBorder(c,px,py,width-px-16,height-70,0xFF1E2540);RenderUtil.drawText(c,textRenderer,"SHAPE",px+14,py+12,ACCENT,false);
        int row=py+32;row=slider(c,px+14,row,"Size",m.getSize(),1,32,0,mx,my);row=slider(c,px+14,row,"Thickness",m.getThickness(),1,8,1,mx,my);row=slider(c,px+14,row,"Gap",m.getGap(),0,16,2,mx,my);row=slider(c,px+14,row,"Opacity",m.getOpacity(),0,255,3,mx,my);
        RenderUtil.drawText(c,textRenderer,"COLOR",px+14,row+4,DIM,false);int cx=px+72;for(int i=0;i<COLORS.length;i++){int col=COLORS[i];RenderUtil.fill(c,cx,row,cx+18,row+18,col);if((m.getColor()&0xFFFFFF)==(col&0xFFFFFF))RenderUtil.drawBorder(c,cx-2,row-2,22,22,0xFFFFFFFF);cx+=25;}row+=30;
        row=toggle(c,px+14,row,"Outline",m.isOutline());row=toggle(c,px+14,row,"Replace vanilla",m.isReplaceVanilla());row=toggle(c,px+14,row,"Enabled",m.isEnabled());
        if(m.getStyle()==CustomCrosshairModule.Style.CUSTOM){RenderUtil.drawText(c,textRenderer,"CUSTOM ARMS",px+14,row+5,ACCENT,false);row+=24;row=slider(c,px+14,row,"Top",m.getCustomTop(),0,24,4,mx,my);row=slider(c,px+14,row,"Bottom",m.getCustomBottom(),0,24,5,mx,my);row=slider(c,px+14,row,"Left",m.getCustomLeft(),0,24,6,mx,my);row=slider(c,px+14,row,"Right",m.getCustomRight(),0,24,7,mx,my);toggle(c,px+14,row,"Center dot",m.isCustomDot());}
        RenderUtil.drawText(c,textRenderer,"Drag sliders · click styles/colors · ESC back",16,height-12,DIM,false);super.render(c,mx,my,d);
    }
    private int slider(DrawContext c,int x,int y,String label,int value,int min,int max,int id,int mx,int my){RenderUtil.drawText(c,textRenderer,label,x,y,DIM,false);RenderUtil.drawText(c,textRenderer,String.valueOf(value),x+68,y,0xFF748FFF,false);int bx=x+105,bw=Math.max(100,width-x-135);RenderUtil.fill(c,bx,y+2,bx+bw,y+6,0x403B5BDB);int knob=bx+(int)(bw*((value-min)/(double)Math.max(1,max-min)));RenderUtil.fill(c,bx,y+1,knob+1,y+7,ACCENT);RenderUtil.fill(c,knob-3,y,knob+4,y+8,0xFFFFFFFF);return y+26;}
    private int toggle(DrawContext c,int x,int y,String label,boolean on){RenderUtil.drawText(c,textRenderer,label,x,y,DIM,false);RenderUtil.drawText(c,textRenderer,on?"ON":"OFF",x+115,y,on?0xFF55E58A:0xFFFF6666,false);return y+22;}
    private void setSlider(CustomCrosshairModule m,int id,double mouseX){int[] mins={1,1,0,0,0,0,0,0};int[] maxs={32,8,16,255,24,24,24,24};int x=505,bw=Math.max(100,width-535);int v=(int)Math.round(mins[id]+Math.max(0,Math.min(1,(mouseX-x)/(double)Math.max(1,bw)))*(maxs[id]-mins[id]));switch(id){case 0->m.setSize(v);case 1->m.setThickness(v);case 2->m.setGap(v);case 3->m.setOpacity(v);case 4->m.setCustomTop(v);case 5->m.setCustomBottom(v);case 6->m.setCustomLeft(v);case 7->m.setCustomRight(v);}}
    @Override public boolean mouseClicked(Click click,boolean doubled){
        CustomCrosshairModule m=mod();if(m==null)return super.mouseClicked(click,doubled);double mx=click.x(),my=click.y();int b=click.button();
        if(mx>=26&&mx<=196&&my>=78&&my<240&&b==0){int i=(int)((my-78)/27);if(i>=0&&i<6)m.setStyle(CustomCrosshairModule.Style.values()[i]);return true;}
        int px=414,py=86;int[] rows={py,py+26,py+52,py+78};for(int i=0;i<4;i++)if(my>=rows[i]&&my<=rows[i]+14&&mx>=px-5){activeSlider=i;setSlider(m,i,mx);return true;}
        int row=py+104;int cx=472;for(int col:COLORS){if(mx>=cx&&mx<=cx+18&&my>=row&&my<=row+18){m.setColor(col);return true;}cx+=25;}row+=30;
        if(my>=row&&my<=row+18&&mx>=px){m.setOutline(!m.isOutline());return true;}row+=22;if(my>=row&&my<=row+18&&mx>=px){m.setReplaceVanilla(!m.isReplaceVanilla());return true;}row+=22;if(my>=row&&my<=row+18&&mx>=px){m.setEnabled(!m.isEnabled());return true;}
        if(m.getStyle()==CustomCrosshairModule.Style.CUSTOM){row+=22+24;for(int i=4;i<=7;i++){if(my>=row&&my<=row+14&&mx>=px-5){activeSlider=i;setSlider(m,i,mx);return true;}row+=26;}if(my>=row&&my<=row+18&&mx>=px){m.setCustomDot(!m.isCustomDot());return true;}}
        return super.mouseClicked(click,doubled);
    }
    @Override public boolean mouseDragged(Click click,double ox,double oy){if(activeSlider>=0&&click.button()==0){CustomCrosshairModule m=mod();if(m!=null){setSlider(m,activeSlider,click.x());return true;}}return super.mouseDragged(click,ox,oy);}
    @Override public boolean mouseReleased(Click click){if(click.button()==0)activeSlider=-1;return super.mouseReleased(click);}
    @Override public boolean keyPressed(net.minecraft.client.input.KeyInput i){if(i.key()==256){client.setScreen(parent);return true;}return super.keyPressed(i);}
    @Override public boolean shouldPause(){return false;}
}
