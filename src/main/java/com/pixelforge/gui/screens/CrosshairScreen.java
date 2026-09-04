package com.pixelforge.gui.screens;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.modules.visual.CustomCrosshairModule;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Monochrome, live crosshair editor matching the rest of PixelForge. */
public final class CrosshairScreen extends Screen {
    private final Screen parent;
    private int activeSlider = -1;
    private static final int TEXT = 0xFFFFFFFF, DIM = 0xFF8A8A8A;
    private static final int[] COLORS = {0xFFFFFFFF,0xFFFF5555,0xFF55FF55,0xFF5555FF,0xFFFFFF55,0xFFFF55FF,0xFF55FFFF,0xFFFFAA00};
    private static final String[] STYLE_NAMES = {"Cross","Dot","Circle","Cross + Dot","Gap","Custom"};

    public CrosshairScreen(Screen parent) { super(Text.literal("Crosshair Editor")); this.parent = parent; }
    private CustomCrosshairModule mod() { return PixelForgeClient.getInstance() == null ? null : PixelForgeClient.getInstance().getModuleManager().getModule(CustomCrosshairModule.class); }

    @Override
    public void render(DrawContext c, int mx, int my, float d) {
        RenderUtil.fill(c, 0, 0, width, height, 0x78000000);
        RenderUtil.drawRoundedPanel(c, 14, 14, width - 28, height - 28, 0xE0101010, 0x55FFFFFF);
        RenderUtil.drawText(c, textRenderer, "CROSSHAIR", 28, 28, TEXT, false);
        CustomCrosshairModule m = mod();
        if (m == null) { RenderUtil.drawText(c, textRenderer, "Custom Crosshair is unavailable", 28, 58, 0xFFFF6666, false); return; }

        int left = 28, top = 54;
        RenderUtil.drawText(c, textRenderer, "STYLE", left, top, DIM, false);
        int sy = top + 18;
        for (int i = 0; i < STYLE_NAMES.length; i++) {
            boolean on = m.getStyle() == CustomCrosshairModule.Style.values()[i], hover = mx >= left && mx <= left + 170 && my >= sy && my < sy + 24;
            RenderUtil.fill(c, left, sy, left + 170, sy + 22, on ? 0x38FFFFFF : (hover ? 0x20FFFFFF : 0x10FFFFFF));
            RenderUtil.drawBorder(c, left, sy, 170, 22, on ? 0xAAFFFFFF : 0x35FFFFFF);
            RenderUtil.drawText(c, textRenderer, STYLE_NAMES[i], left + 10, sy + 7, TEXT, false);
            sy += 27;
        }

        int prevX = 220, prevY = 54;
        RenderUtil.fill(c, prevX, prevY, prevX + 150, prevY + 150, 0xE0101010);
        RenderUtil.drawBorder(c, prevX, prevY, 150, 150, 0x45FFFFFF);
        RenderUtil.drawText(c, textRenderer, "LIVE PREVIEW", prevX + 12, prevY + 10, DIM, false);
        RenderUtil.fill(c, prevX + 75, prevY + 30, prevX + 76, prevY + 130, 0x20FFFFFF);
        RenderUtil.fill(c, prevX + 25, prevY + 79, prevX + 125, prevY + 80, 0x20FFFFFF);
        m.renderCrosshair(c, prevX + 75, prevY + 80);

        int px = 390, py = 54;
        RenderUtil.drawText(c, textRenderer, "SHAPE", px, py, DIM, false);
        int row = py + 18;
        row = slider(c, px, row, "Size", m.getSize(), 0, 32, 0, mx);
        row = slider(c, px, row, "Thickness", m.getThickness(), 1, 8, 1, mx);
        row = slider(c, px, row, "Gap", m.getGap(), 0, 16, 2, mx);
        row = slider(c, px, row, "Opacity", m.getOpacity(), 0, 255, 3, mx);
        RenderUtil.drawText(c, textRenderer, "COLOR", px, row + 2, DIM, false);
        int cx = px + 58;
        for (int col : COLORS) { RenderUtil.fill(c, cx, row, cx + 18, row + 18, col); if ((m.getColor() & 0xFFFFFF) == (col & 0xFFFFFF)) RenderUtil.drawBorder(c, cx - 2, row - 2, 22, 22, 0xFFFFFFFF); cx += 25; }
        row += 30;
        row = toggle(c, px, row, "Outline", m.isOutline());
        row = toggle(c, px, row, "Replace vanilla", m.isReplaceVanilla());
        toggle(c, px, row, "Enabled", m.isEnabled());
        RenderUtil.drawText(c, textRenderer, "ESC  •  Back", 28, height - 26, DIM, false);
        super.render(c, mx, my, d);
    }

    private int slider(DrawContext c,int x,int y,String label,int value,int min,int max,int id,int mx){RenderUtil.drawText(c,textRenderer,label,x,y,DIM,false);RenderUtil.drawText(c,textRenderer,String.valueOf(value),x+72,y,TEXT,false);int bx=x+108,bw=Math.max(120,width-x-150);RenderUtil.fill(c,bx,y+3,bx+bw,y+6,0x40FFFFFF);int knob=bx+(int)(bw*((value-min)/(double)Math.max(1,max-min)));RenderUtil.fill(c,bx,y+3,knob,y+6,0xFFFFFFFF);RenderUtil.fill(c,knob-3,y,knob+3,y+9,0xFFFFFFFF);return y+28;}
    private int toggle(DrawContext c,int x,int y,String label,boolean on){RenderUtil.drawText(c,textRenderer,label,x,y,DIM,false);RenderUtil.drawText(c,textRenderer,on?"ON":"OFF",x+120,y,TEXT,false);return y+24;}
    private void setSlider(CustomCrosshairModule m,int id,double mouseX){int[] mins={0,1,0,0,0,0,0,0};int[] maxs={32,8,16,255,24,24,24,24};int x=498,bw=Math.max(120,width-x-150);int v=(int)Math.round(mins[id]+Math.max(0,Math.min(1,(mouseX-x)/(double)Math.max(1,bw)))*(maxs[id]-mins[id]));switch(id){case 0->m.setSize(v);case 1->m.setThickness(v);case 2->m.setGap(v);case 3->m.setOpacity(v);case 4->m.setCustomTop(v);case 5->m.setCustomBottom(v);case 6->m.setCustomLeft(v);case 7->m.setCustomRight(v);}}
    @Override public boolean mouseClicked(Click click,boolean doubled){CustomCrosshairModule m=mod();if(m==null)return super.mouseClicked(click,doubled);double mx=click.x(),my=click.y();if(click.button()!=0)return super.mouseClicked(click,doubled);if(mx>=28&&mx<=198&&my>=72&&my<234){int i=(int)((my-72)/27);if(i>=0&&i<6)m.setStyle(CustomCrosshairModule.Style.values()[i]);return true;}int[] rows={72,100,128,156};for(int i=0;i<4;i++)if(my>=rows[i]&&my<=rows[i]+16&&mx>=498){activeSlider=i;setSlider(m,i,mx);return true;}int row=184,cx=448;for(int col:COLORS){if(mx>=cx&&mx<=cx+18&&my>=row&&my<=row+18){m.setColor(col);return true;}cx+=25;}row+=30;if(my>=row&&my<=row+18&&mx>=390){m.setOutline(!m.isOutline());return true;}row+=24;if(my>=row&&my<=row+18&&mx>=390){m.setReplaceVanilla(!m.isReplaceVanilla());return true;}row+=24;if(my>=row&&my<=row+18&&mx>=390){m.setEnabled(!m.isEnabled());return true;}return super.mouseClicked(click,doubled);}
    @Override public boolean mouseDragged(Click click,double ox,double oy){if(activeSlider>=0&&click.button()==0){CustomCrosshairModule m=mod();if(m!=null){setSlider(m,activeSlider,click.x());return true;}}return super.mouseDragged(click,ox,oy);}
    @Override public boolean mouseReleased(Click click){if(click.button()==0)activeSlider=-1;return super.mouseReleased(click);}
    @Override public boolean keyPressed(net.minecraft.client.input.KeyInput i){if(i.key()==256){client.setScreen(parent);return true;}return super.keyPressed(i);}
    @Override public boolean shouldPause(){return false;}
}
