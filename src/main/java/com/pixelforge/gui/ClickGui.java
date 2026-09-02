package com.pixelforge.gui;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** PixelForge sidebar ClickGUI: category sidebar, scrollable modules and live settings pane. */
public class ClickGui extends Screen {
    private final List<Category> categories = new ArrayList<>();
    private Category selectedCategory;
    private Module selectedModule;
    private TextFieldWidget searchField;
    private int moduleScroll;
    private boolean waitingForKey;

    private static final int BG=0xE8080B12, PANEL=0xE8141928, HEADER=0xF0121726, ACCENT=0xFF3B5BDB;
    private static final int TEXT=0xFFE8ECFF, DIM=0xFF8993AC, GREEN=0xFF55E58A, RED=0xFFFF6666;

    public ClickGui(){
        super(Text.literal("PixelForge ClickGUI"));
        for(Category c:Category.values()) if(c!=Category.SYSTEM) categories.add(c);
        if(!categories.isEmpty()) selectedCategory=categories.get(0);
    }

    @Override protected void init(){
        searchField=new TextFieldWidget(textRenderer,116,10,220,20,Text.literal("Search modules"));
        searchField.setPlaceholder(Text.literal("Search modules...")); searchField.setMaxLength(40);
        addDrawableChild(searchField); setFocused(searchField);
    }

    private List<Module> modules(){
        if(selectedCategory==null) return List.of();
        String q=searchField==null?"":searchField.getText().trim().toLowerCase(Locale.ROOT);
        return PixelForgeClient.getInstance().getModuleManager().getModulesByCategory(selectedCategory).stream()
                .filter(m->q.isEmpty()||m.getName().toLowerCase(Locale.ROOT).contains(q)).toList();
    }

    @Override public void render(DrawContext c,int mx,int my,float d){
        RenderUtil.fill(c,0,0,width,height,BG);
        // Sidebar
        RenderUtil.fill(c,0,0,102,height,0xF00B0F18); RenderUtil.fill(c,0,0,3,height,ACCENT);
        RenderUtil.drawText(c,textRenderer,"PIXELFORGE",14,12,TEXT,false);
        int cy=40;
        for(Category cat:categories){
            boolean on=cat==selectedCategory, hover=mx>=8&&mx<=94&&my>=cy&&my<cy+26;
            RenderUtil.fill(c,8,cy,94,cy+26,on?0x403B5BDB:(hover?0x241E2540:0x001E2540));
            RenderUtil.drawText(c,textRenderer,cat.getDisplayName(),18,cy+8,on?0xFF748FFF:DIM,false); cy+=30;
        }
        // Module column
        RenderUtil.fill(c,106,6,350,height-6,PANEL); RenderUtil.drawBorder(c,106,6,244,height-12,0xFF1E2540);
        RenderUtil.drawText(c,textRenderer,selectedCategory==null?"Modules":selectedCategory.getDisplayName(),116,36,ACCENT,false);
        searchField.setX(116); searchField.setY(10); searchField.setWidth(220); searchField.render(c,mx,my,d);
        List<Module> list=modules(); int rowH=28, top=50, bottom=height-18;
        int max=Math.max(0,list.size()*rowH-(bottom-top)); moduleScroll=Math.max(0,Math.min(moduleScroll,max));
        c.enableScissor(106,50,350,bottom);
        int y=top-moduleScroll;
        for(Module m:list){
            boolean on=m==selectedModule, hover=mx>=112&&mx<344&&my>=y&&my<y+24;
            RenderUtil.fill(c,112,y,344,y+24,on?0x403B5BDB:(hover?0x201E2540:0x08101824));
            RenderUtil.drawText(c,textRenderer,m.getName(),120,y+7,m.isEnabled()?GREEN:DIM,false);
            RenderUtil.drawText(c,textRenderer,m.isEnabled()?"ON":"OFF",315,y+7,m.isEnabled()?GREEN:DIM,false);
            y+=rowH;
        }
        c.disableScissor();
        if(max>0){int trackH=bottom-top; int thumbH=Math.max(18,(int)(trackH*(trackH/(double)(trackH+max))));int ty=top+(int)((trackH-thumbH)*(moduleScroll/(double)max));RenderUtil.fill(c,344,top,348,bottom,0x301E2540);RenderUtil.fill(c,344,ty,348,ty+thumbH,ACCENT);}
        // Settings column
        RenderUtil.fill(c,358,6,width-6,height-6,PANEL); RenderUtil.drawBorder(c,358,6,width-364,height-12,0xFF1E2540);
        if(selectedModule==null){RenderUtil.drawText(c,textRenderer,"Select a module",374,40,DIM,false);RenderUtil.drawText(c,textRenderer,"Click a module to edit it.",374,58,DIM,false);}
        else renderSettings(c,mx,my);
        RenderUtil.drawText(c,textRenderer,"RShift / ESC close  ·  scroll modules  ·  click module for settings",10,height-12,DIM,false);
        super.render(c,mx,my,d);
    }

    private void renderSettings(DrawContext c,int mx,int my){
        RenderUtil.drawText(c,textRenderer,selectedModule.getName(),374,22,TEXT,false);
        RenderUtil.drawText(c,textRenderer,selectedModule.getDescription(),374,40,DIM,false);
        RenderUtil.fill(c,374,64,width-20,92,selectedModule.isEnabled()?0x303B5BDB:0x18141B28);
        RenderUtil.drawBorder(c,374,64,width-394,28,selectedModule.isEnabled()?ACCENT:0xFF1E2540);
        RenderUtil.drawText(c,textRenderer,"Enabled",386,74,TEXT,false);
        RenderUtil.drawText(c,textRenderer,selectedModule.isEnabled()?"ON":"OFF",width-70,74,selectedModule.isEnabled()?GREEN:RED,false);
        RenderUtil.drawText(c,textRenderer,"KEYBIND",374,114,ACCENT,false);
        RenderUtil.drawText(c,textRenderer,waitingForKey?"Press a key...":(selectedModule.getKeybind()<0?"None":("Key #"+selectedModule.getKeybind())),374,132,waitingForKey?0xFFFFAA55:DIM,false);
        RenderUtil.fill(c,374,146,width-20,170,0x18141B28); RenderUtil.drawBorder(c,374,146,width-394,24,0xFF1E2540);
        RenderUtil.drawText(c,textRenderer,"Set keybind",386,154,0xFF748FFF,false);
        int row=188;
        if(selectedModule.getSettings().isEmpty()){
            RenderUtil.drawText(c,textRenderer,"No extra settings for this module.",374,row,DIM,false);
        }else{
            for(Module.Setting<?> s:selectedModule.getSettings()){
                RenderUtil.drawText(c,textRenderer,s.getName(),374,row,DIM,false);
                RenderUtil.drawText(c,textRenderer,String.valueOf(s.get()),width-110,row,0xFF748FFF,false); row+=22;
            }
        }
    }

    @Override public boolean mouseClicked(net.minecraft.client.gui.Click click,boolean doubled){
        double x=click.x(),y=click.y(); int b=click.button();
        if(y<34&&x>=102&&x<345){return true;}
        int cy=40;
        for(Category cat:categories){if(x>=8&&x<=94&&y>=cy&&y<cy+26){if(b==0){selectedCategory=cat;moduleScroll=0;selectedModule=null;setFocused(searchField);}return true;}cy+=30;}
        if(x>=112&&x<=344&&y>=50&&y<=height-18){
            List<Module> list=modules(); int rowH=28, top=50; int idx=(int)((y-top+moduleScroll)/rowH);
            if(idx>=0&&idx<list.size()){Module m=list.get(idx);selectedModule=m;if(b==0)m.toggle();else if(b==1)waitingForKey=false;return true;}
        }
        if(selectedModule!=null&&x>=374&&x<=width-20){
            if(y>=64&&y<=92){if(b==0)selectedModule.toggle();return true;}
            if(y>=146&&y<=170){if(b==0){waitingForKey=true;setFocused(null);}return true;}
        }
        if(searchField.mouseClicked(click,doubled)){setFocused(searchField);return true;}
        return super.mouseClicked(click,doubled);
    }

    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){
        if(mx>=106&&mx<=350){moduleScroll-=vertical*28; moduleScroll=Math.max(0,moduleScroll); return true;}
        return super.mouseScrolled(mx,my,horizontal,vertical);
    }

    @Override public boolean keyPressed(net.minecraft.client.input.KeyInput i){
        if(i.key()==256||i.key()==344){close();return true;}
        if(waitingForKey&&selectedModule!=null){selectedModule.setKeybind(i.key());waitingForKey=false;return true;}
        if(searchField.isFocused()&&searchField.keyPressed(i))return true;
        return super.keyPressed(i);
    }
    @Override public boolean charTyped(net.minecraft.client.input.CharInput i){
        if(searchField.isFocused()&&searchField.charTyped(i))return true;
        return super.charTyped(i);
    }
    @Override public boolean shouldPause(){return false;}
}
