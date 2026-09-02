package com.pixelforge.gui;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.config.ConfigManager;
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

/** Compact, mouse-friendly PixelForge ClickGUI with real module controls. */
public class ClickGui extends Screen {
    private final List<Category> categories=new ArrayList<>();
    private Category selectedCategory;
    private Module selectedModule;
    private TextFieldWidget searchField;
    private int moduleScroll;
    private boolean waitingForKey;
    private boolean draggingSlider;
    private Module.Setting<?> draggedSetting;
    private static final int BG=0xF20A0D15,PANEL=0xF0141925,ACCENT=0xFF7182FF,TEXT=0xFFEAF0FF,DIM=0xFF8490AA,GREEN=0xFF5BE58A,RED=0xFFFF6B7A;
    private int left,top,cw,ch,side,mid,right;

    public ClickGui(){
        super(Text.literal("PixelForge ClickGUI"));
        for(Category c:Category.values())if(c!=Category.SYSTEM)categories.add(c);
        if(!categories.isEmpty())selectedCategory=categories.get(0);
    }

    @Override protected void init(){
        searchField=new TextFieldWidget(textRenderer,0,0,160,18,Text.literal("Search"));
        searchField.setPlaceholder(Text.literal("Search modules..."));
        searchField.setMaxLength(40);
        addDrawableChild(searchField);
        layout();
    }

    private void layout(){
        cw=Math.min(width-24,Math.max(620,(int)(width*.62)));
        ch=Math.min(height-24,Math.max(360,(int)(height*.62)));
        left=(width-cw)/2; top=(height-ch)/2;
        side=118; mid=230; right=Math.max(180,cw-side-mid);
        if(searchField!=null){searchField.setX(left+side+10);searchField.setY(top+10);searchField.setWidth(mid-20);}
    }

    private List<Module> modules(){
        if(selectedCategory==null)return List.of();
        String q=searchField==null?"":searchField.getText().trim().toLowerCase(Locale.ROOT);
        return PixelForgeClient.getInstance().getModuleManager().getModulesByCategory(selectedCategory).stream()
            .filter(m->q.isEmpty()||m.getName().toLowerCase(Locale.ROOT).contains(q)||m.getDescription().toLowerCase(Locale.ROOT).contains(q)).toList();
    }

    @Override public void render(DrawContext c,int mx,int my,float d){
        layout();
        RenderUtil.fill(c,0,0,width,height,0x88000000);
        panel(c,left,top,cw,ch,BG,0xFF293454);
        RenderUtil.drawText(c,textRenderer,"PIXELFORGE",left+14,top+12,TEXT,false);
        RenderUtil.drawText(c,textRenderer,"RSHIFT",left+cw-56,top+12,DIM,false);
        RenderUtil.fill(c,left,top+34,left+side,top+ch,0xE50B101A);
        int y=top+46;
        for(Category cat:categories){
            boolean on=cat==selectedCategory,hover=mx>=left+8&&mx<=left+side-8&&my>=y&&my<y+27;
            RenderUtil.fill(c,left+8,y,left+side-8,y+27,on?0x553B5BDB:(hover?0x201E2540:0));
            RenderUtil.drawText(c,textRenderer,cat.getDisplayName(),left+18,y+8,on?ACCENT:DIM,false); y+=30;
        }
        RenderUtil.drawText(c,textRenderer,selectedCategory==null?"Modules":selectedCategory.getDisplayName(),left+side+10,top+39,ACCENT,false);
        List<Module> list=modules();
        int rowH=28,listTop=top+52,listBottom=top+ch-34,max=Math.max(0,list.size()*rowH-(listBottom-listTop));
        moduleScroll=Math.max(0,Math.min(moduleScroll,max));
        c.enableScissor(left+side,listTop,left+side+mid,listBottom);
        int yy=listTop-moduleScroll;
        for(Module m:list){
            boolean on=m==selectedModule,hover=mx>=left+side+7&&mx<=left+side+mid-10&&my>=yy&&my<yy+24;
            RenderUtil.fill(c,left+side+7,yy,left+side+mid-10,yy+24,on?0x553B5BDB:(hover?0x201E2540:0x08101824));
            RenderUtil.drawText(c,textRenderer,m.getName(),left+side+15,yy+7,m.isEnabled()?GREEN:DIM,false);
            RenderUtil.drawText(c,textRenderer,m.isEnabled()?"ON":"OFF",left+side+mid-40,yy+7,m.isEnabled()?GREEN:DIM,false);
            yy+=rowH;
        }
        c.disableScissor();
        if(max>0)drawScrollbar(c,listTop,listBottom,max,moduleScroll);

        int rx=left+side+mid;
        RenderUtil.fill(c,rx,top+34,left+cw,top+ch,0xE5121825);
        if(selectedModule==null){
            RenderUtil.drawText(c,textRenderer,"Select a module",rx+16,top+58,DIM,false);
            RenderUtil.drawText(c,textRenderer,"Left click selects. Right click toggles.",rx+16,top+78,DIM,false);
        }else renderSettings(c,rx,mx,my);
        RenderUtil.fill(c,left+10,top+ch-28,left+110,top+ch-8,0x303B5BDB);
        RenderUtil.drawBorder(c,left+10,top+ch-28,100,20,0xFF394A79);
        RenderUtil.drawText(c,textRenderer,"HUD Editor",left+25,top+ch-22,ACCENT,false);
        RenderUtil.drawText(c,textRenderer,"Wheel = scroll · Right click = toggle",left+side+10,top+ch-18,DIM,false);
        super.render(c,mx,my,d);
    }

    private void renderSettings(DrawContext c,int rx,int mx,int my){
        int panelW=left+cw-rx-16;
        RenderUtil.drawText(c,textRenderer,selectedModule.getName(),rx+16,top+48,TEXT,false);
        String desc=selectedModule.getDescription();
        if(desc.length()>42)desc=desc.substring(0,39)+"...";
        RenderUtil.drawText(c,textRenderer,desc,rx+16,top+67,DIM,false);
        button(c,rx+16,top+92,panelW,24,"Enabled",selectedModule.isEnabled());
        String key=waitingForKey?"Press a key...":selectedModule.getKeybind()<0?"Keybind: None":"Keybind: "+selectedModule.getKeybind();
        button(c,rx+16,top+124,panelW,24,key,false);
        int y=top+164;
        for(Module.Setting<?> s:selectedModule.getSettings()){
            Object v=s.get();
            RenderUtil.drawText(c,textRenderer,s.getName(),rx+16,y,DIM,false);
            if(v instanceof Boolean b){
                button(c,left+cw-92,y-7,76,20,b?"ON":"OFF",b);
            }else if(v instanceof Number n){
                drawNumber(c,rx,panelW,y,n);
            }else{
                RenderUtil.drawText(c,textRenderer,String.valueOf(v),left+cw-110,y,ACCENT,false);
            }
            y+=30;
            if(y>top+ch-44)break;
        }
    }

    private void drawNumber(DrawContext c,int rx,int panelW,int y,Number n){
        double min=numberMin(n),max=numberMax(n),value=Math.max(min,Math.min(max,n.doubleValue()));
        int bx=rx+16,bw=Math.max(80,panelW-96);
        RenderUtil.fill(c,bx,y+5,bx+bw,y+8,0x503B5BDB);
        double ratio=(value-min)/Math.max(.0001,max-min);
        int knob=bx+(int)(bw*Math.max(0,Math.min(1,ratio)));
        RenderUtil.fill(c,bx,y+4,knob,y+9,0xFF5368C8);
        RenderUtil.fill(c,knob-4,y,knob+4,y+13,ACCENT);
        RenderUtil.drawText(c,textRenderer,formatNumber(n),left+cw-70,y,ACCENT,false);
    }

    private double numberMin(Number n){return n instanceof Integer||n instanceof Long?0.0:0.0;}
    private double numberMax(Number n){double v=Math.abs(n.doubleValue());if(v<=1)return 1;if(v<=10)return 20;if(v<=100)return 200;if(v<=1000)return 2000;return v*2;}
    private String formatNumber(Number n){if(n instanceof Integer||n instanceof Long)return String.valueOf(n.longValue());return String.format(Locale.ROOT,"%.2f",n.doubleValue());}

    private void button(DrawContext c,int x,int y,int w,int h,String label,boolean on){
        RenderUtil.fill(c,x,y,x+w,y+h,on?0x453B5BDB:0x1C1E2540);
        RenderUtil.drawBorder(c,x,y,w,h,on?ACCENT:0xFF273453);
        RenderUtil.drawText(c,textRenderer,label,x+10,y+7,on?TEXT:DIM,false);
    }

    private void panel(DrawContext c,int x,int y,int w,int h,int fill,int border){RenderUtil.fill(c,x,y,x+w,y+h,fill);RenderUtil.drawBorder(c,x,y,w,h,border);}
    private void drawScrollbar(DrawContext c,int top,int bottom,int max,int scroll){int view=bottom-top;int content=view+max;int thumb=Math.max(18,(int)(view*(view/(double)content)));int ty=top+(int)((view-thumb)*(scroll/(double)max));RenderUtil.fill(c,left+side+mid-7,top,left+side+mid-4,bottom,0x401E2540);RenderUtil.fill(c,left+side+mid-7,ty,left+side+mid-4,ty+thumb,ACCENT);}

    @Override public boolean mouseClicked(net.minecraft.client.gui.Click click,boolean doubled){
        layout(); double x=click.x(),y=click.y(); int b=click.button();
        if(b==0&&x>=left+10&&x<=left+110&&y>=top+ch-28&&y<=top+ch-8){client.setScreen(new HudEditor());return true;}
        int cy=top+46;
        for(Category cat:categories){if(x>=left+8&&x<=left+side-8&&y>=cy&&y<cy+27){selectedCategory=cat;selectedModule=null;moduleScroll=0;return true;}cy+=30;}
        if(x>=left+side&&x<=left+side+mid&&y>=top+52&&y<=top+ch-34){
            List<Module> list=modules();int idx=(int)((y-(top+52)+moduleScroll)/28);
            if(idx>=0&&idx<list.size()){selectedModule=list.get(idx);if(b==1)selectedModule.toggle();return true;}
        }
        int rx=left+side+mid;
        if(selectedModule!=null&&x>=rx+10&&x<=left+cw-10){
            if(y>=top+92&&y<=top+116&&b==0){selectedModule.toggle();return true;}
            if(y>=top+124&&y<=top+150&&b==0){waitingForKey=true;setFocused(null);return true;}
            int sy=top+164;
            for(Module.Setting<?> s:selectedModule.getSettings()){
                Object v=s.get();
                if(v instanceof Boolean&&y>=sy-9&&y<=sy+14&&b==0){setBoolean(s,!((Boolean)v));return true;}
                if(v instanceof Number&&y>=sy-10&&y<=sy+16&&b==0){draggingSlider=true;draggedSetting=s;setNumberFromMouse(s,x,rx);return true;}
                sy+=30;
            }
        }
        if(searchField.mouseClicked(click,doubled)){setFocused(searchField);return true;}
        return super.mouseClicked(click,doubled);
    }

    private void setBoolean(Module.Setting<?> s,boolean value){((Module.Setting<Boolean>)s).set(value);ConfigManager.saveModule(selectedModule);}
    private void setNumberFromMouse(Module.Setting<?> s,double mouseX,int rx){
        Object v=s.get();double min=numberMin((Number)v),max=numberMax((Number)v);int panelW=left+cw-rx-16,bx=rx+16,bw=Math.max(80,panelW-96);double ratio=Math.max(0,Math.min(1,(mouseX-bx)/(double)bw));double nv=min+(max-min)*ratio;
        if(v instanceof Integer)((Module.Setting<Integer>)s).set((int)Math.round(nv));else if(v instanceof Long)((Module.Setting<Long>)s).set(Math.round(nv));else if(v instanceof Float)((Module.Setting<Float>)s).set((float)nv);else if(v instanceof Double)((Module.Setting<Double>)s).set(nv);ConfigManager.saveModule(selectedModule);
    }

    @Override public boolean mouseDragged(net.minecraft.client.gui.Click click,double dx,double dy){
        if(draggingSlider&&draggedSetting!=null&&selectedModule!=null){setNumberFromMouse(draggedSetting,click.x(),left+side+mid);return true;}
        return super.mouseDragged(click,dx,dy);
    }
    @Override public boolean mouseReleased(net.minecraft.client.gui.Click click){draggingSlider=false;draggedSetting=null;return super.mouseReleased(click);}
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){layout();if(mx>=left+side&&mx<=left+side+mid){moduleScroll=Math.max(0,moduleScroll-(int)(vertical*28));return true;}return super.mouseScrolled(mx,my,horizontal,vertical);}
    @Override public boolean keyPressed(net.minecraft.client.input.KeyInput i){
        if(i.key()==256){close();return true;}
        if(waitingForKey&&selectedModule!=null){if(i.key()!=256)selectedModule.setKeybind(i.key());waitingForKey=false;ConfigManager.saveModule(selectedModule);return true;}
        if(searchField!=null&&searchField.isFocused()&&searchField.keyPressed(i))return true;
        return super.keyPressed(i);
    }
    @Override public boolean charTyped(net.minecraft.client.input.CharInput i){if(searchField!=null&&searchField.isFocused()&&searchField.charTyped(i))return true;return super.charTyped(i);}
    @Override public boolean shouldPause(){return false;}
}
