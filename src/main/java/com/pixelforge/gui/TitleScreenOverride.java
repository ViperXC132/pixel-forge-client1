package com.pixelforge.gui;

import com.pixelforge.account.AccountManager;
import com.pixelforge.account.SessionApplier;
import com.pixelforge.gui.screens.AccountsScreen;
import com.pixelforge.gui.screens.CrosshairScreen;
import com.pixelforge.gui.screens.ModsScreen;
import com.pixelforge.util.ColorUtil;
import com.pixelforge.util.RenderUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/** PixelForge main menu with custom controls and a real quick-connect server panel. */
public class TitleScreenOverride extends Screen {
    private final List<Star> stars=new ArrayList<>(); private final Random random=new Random();
    private final List<QuickServer> servers=new ArrayList<>(); private final Path serverFile=FabricLoader.getInstance().getConfigDir().resolve("pixelforge/quick-servers.txt");
    private TextFieldWidget nameField,addressField; private boolean addServer; private long lastPingTick;
    private static final int BG=0xFF0E1117,NAV=0xE00A0C14,ACCENT=0xFF3B5BDB,TEXT=0xFFC8D0E0,DIM=0xFF8892A8,MUTED=0xFF3D4A6A,PANEL=0xD0101424,BORDER=0xFF1E2540;

    public TitleScreenOverride(){super(Text.literal("PixelForge"));for(int i=0;i<90;i++)stars.add(new Star(random.nextFloat()*1200,random.nextFloat()*700,random.nextFloat()*0.7f+0.15f,random.nextFloat()*0.5f+0.2f));loadServers();}
    @Override protected void init(){
        nameField=new TextFieldWidget(textRenderer,0,0,130,20,Text.literal("Server name"));nameField.setPlaceholder(Text.literal("Server name"));nameField.setMaxLength(32);
        addressField=new TextFieldWidget(textRenderer,0,0,160,20,Text.literal("Address"));addressField.setPlaceholder(Text.literal("play.example.net"));addressField.setMaxLength(80);
        addDrawableChild(nameField);addDrawableChild(addressField);nameField.visible=false;addressField.visible=false;
    }
    private void loadServers(){try{if(Files.exists(serverFile)){for(String line:Files.readAllLines(serverFile)){String[] p=line.split("\\|",2);if(p.length==2&&!p[0].isBlank()&&!p[1].isBlank())servers.add(new QuickServer(p[0],p[1]));}}}catch(IOException ignored){}if(servers.isEmpty()){servers.add(new QuickServer("Hypixel","mc.hypixel.net"));servers.add(new QuickServer("CubeCraft","play.cubecraft.net"));}for(QuickServer s:servers)ping(s);}
    private void saveServers(){try{Files.createDirectories(serverFile.getParent());List<String> lines=servers.stream().map(s->s.name.replace("|"," ")+"|"+s.address.replace("|"," ")).toList();Files.write(serverFile,lines);}catch(IOException ignored){}}
    private void ping(QuickServer s){if(s.pinging)return;s.pinging=true;CompletableFuture.runAsync(()->{long start=System.nanoTime();try{String host=s.address;int port=25565;if(host.contains(":")){String[] p=host.split(":",2);host=p[0];try{port=Integer.parseInt(p[1]);}catch(NumberFormatException ignored){}}try(Socket socket=new Socket()){socket.connect(new InetSocketAddress(host,port),1800);s.ping=(System.nanoTime()-start)/1_000_000L;s.online=true;}}catch(Exception e){s.ping=-1;s.online=false;}finally{s.pinging=false;}});}
    @Override public void tick(){super.tick();if(++lastPingTick>=100){lastPingTick=0;for(QuickServer s:servers)ping(s);}}

    @Override public void render(DrawContext c,int mx,int my,float d){
        RenderUtil.fill(c,0,0,width,height,BG);for(int gx=0;gx<width;gx+=40)RenderUtil.fill(c,gx,0,gx+1,height,0x05FFFFFF);for(int gy=0;gy<height;gy+=40)RenderUtil.fill(c,0,gy,width,gy+1,0x05FFFFFF);
        for(Star s:stars){s.y+=s.speed*.22f;if(s.y>height){s.y=0;s.x=random.nextFloat()*Math.max(1,width);}RenderUtil.fill(c,(int)s.x,(int)s.y,(int)s.x+1,(int)s.y+1,ColorUtil.rgba(200,210,255,Math.min(255,(int)(s.alpha*220))));}
        RenderUtil.fill(c,0,0,width,32,NAV);RenderUtil.fill(c,0,31,width,32,0x12FFFFFF);RenderUtil.fill(c,14,7,30,23,ACCENT);RenderUtil.fill(c,18,11,26,19,0xFFFFFFFF);RenderUtil.drawText(c,textRenderer,"PixelForge",38,11,TEXT,false);RenderUtil.drawText(c,textRenderer,"1.21.11",108,12,ACCENT,false);
        nav(c,"Home",220,true,mx,my);nav(c,"Mods",270,false,mx,my);nav(c,"Crosshair",315,false,mx,my);nav(c,"Accounts",390,false,mx,my);
        RenderUtil.drawText(c,textRenderer,"Welcome back, "+SessionApplier.currentUsername(),width/2-80,62,0xFF748FFF,false);
        int bx=28,by=height/2-88;customButton(c,bx,by,190,30,"Singleplayer",mx,my);customButton(c,bx,by+38,190,30,"Multiplayer",mx,my);customButton(c,bx,by+76,190,30,"Mod Manager",mx,my);customButton(c,bx,by+114,190,30,"Options",mx,my);customButton(c,bx,by+152,190,30,"Quit",mx,my);
        int px=width-270,py=50;panel(c,px,py,250,132);RenderUtil.drawText(c,textRenderer,"ACCOUNTS",px+12,py+10,ACCENT,false);RenderUtil.drawText(c,textRenderer,"manage",px+192,py+10,MUTED,false);var active=AccountManager.getActive();if(active!=null){RenderUtil.drawText(c,textRenderer,active.username,px+16,py+35,TEXT,false);RenderUtil.drawText(c,textRenderer,active.type.displayName,px+16,py+50,MUTED,false);}else RenderUtil.drawText(c,textRenderer,"No active account",px+16,py+38,DIM,false);RenderUtil.drawText(c,textRenderer,"+ Add account",px+14,py+108,0xFF748FFF,false);
        int qy=py+142;int qh=addServer?190:154;panel(c,px,qy,250,qh);RenderUtil.drawText(c,textRenderer,"QUICK CONNECT",px+12,qy+10,ACCENT,false);int sy=qy+30;int index=0;for(QuickServer s:servers){if(index++>=3)break;drawServer(c,px+12,sy,s,mx,my);sy+=30;}
        if(addServer){nameField.visible=true;addressField.visible=true;nameField.setX(px+12);nameField.setY(qy+105);addressField.setX(px+12);addressField.setY(qy+130);nameField.render(c,mx,my,d);addressField.render(c,mx,my,d);customButton(c,px+182,qy+130,56,20,"Save",mx,my);}else{nameField.visible=false;addressField.visible=false;customButton(c,px+12,qy+122,90,20,"+ Server",mx,my);}
        RenderUtil.fill(c,0,height-22,width,height,NAV);RenderUtil.drawText(c,textRenderer,"PixelForge v1.0.0 · Fabric 1.21.11 · Java 21",12,height-15,MUTED,false);RenderUtil.drawText(c,textRenderer,"Discord · GitHub · Report bug",width-150,height-15,MUTED,false);super.render(c,mx,my,d);
    }
    private void nav(DrawContext c,String label,int x,boolean active,int mx,int my){boolean hover=mx>=x-4&&mx<=x+textRenderer.getWidth(label)+4&&my<32;RenderUtil.drawText(c,textRenderer,label,x,11,active?0xFF748FFF:(hover?TEXT:DIM),false);}
    private void panel(DrawContext c,int x,int y,int w,int h){RenderUtil.fill(c,x,y,x+w,y+h,PANEL);RenderUtil.drawBorder(c,x,y,w,h,BORDER);}
    private void customButton(DrawContext c,int x,int y,int w,int h,String label,int mx,int my){boolean hover=mx>=x&&mx<=x+w&&my>=y&&my<=y+h;RenderUtil.fill(c,x,y,x+w,y+h,hover?0x503B5BDB:0x30101828);RenderUtil.drawBorder(c,x,y,w,h,hover?0xFF748FFF:BORDER);RenderUtil.drawCenteredText(c,textRenderer,label,x+w/2,y+9,hover?TEXT:DIM,false);}
    private void drawServer(DrawContext c,int x,int y,QuickServer s,int mx,int my){boolean hover=mx>=x&&mx<=x+225&&my>=y&&my<=y+25;RenderUtil.fill(c,x,y,x+225,y+25,hover?0x241E2540:0x08101824);RenderUtil.fill(c,x+2,y+8,x+8,y+14,s.online?0xFF40C057:0xFFFA5252);RenderUtil.drawText(c,textRenderer,s.name,x+14,y+2,TEXT,false);RenderUtil.drawText(c,textRenderer,s.address,x+14,y+13,MUTED,false);String ping=s.pinging?"…":(s.online?s.ping+"ms":"off");RenderUtil.drawText(c,textRenderer,ping,x+185,y+7,s.online?0xFF40C057:0xFFFA5252,false);}
    @Override public boolean mouseClicked(Click click,boolean doubled){int x=(int)click.x(),y=(int)click.y();int b=click.button();if(y<32&&b==0){if(x>=270&&x<310){client.setScreen(new ModsScreen(this));return true;}if(x>=315&&x<385){client.setScreen(new CrosshairScreen(this));return true;}if(x>=390&&x<470){client.setScreen(new AccountsScreen(this));return true;}}
        int bx=28,by=height/2-88;if(b==0){if(hit(x,y,bx,by,190,30)){client.setScreen(new SelectWorldScreen(this));return true;}if(hit(x,y,bx,by+38,190,30)){client.setScreen(new MultiplayerScreen(this));return true;}if(hit(x,y,bx,by+76,190,30)){client.setScreen(new ModsScreen(this));return true;}if(hit(x,y,bx,by+114,190,30)){client.setScreen(new OptionsScreen(this,client.options));return true;}if(hit(x,y,bx,by+152,190,30)){client.scheduleStop();return true;}}
        int px=width-270,py=50;if(b==0&&x>=px+140&&y>=py+4&&y<=py+28){client.setScreen(new AccountsScreen(this));return true;}if(b==0&&x>=px+10&&x<=px+120&&y>=py+94&&y<=py+126){client.setScreen(new AccountsScreen(this));return true;}
        int qy=py+142;int index=0;int sy=qy+30;for(QuickServer s:servers){if(index++>=3)break;if(x>=px+12&&x<=px+237&&y>=sy&&y<=sy+25&&b==0){ServerInfo info=new ServerInfo(s.name,s.address,ServerInfo.ServerType.OTHER);ConnectScreen.connect(this,client,ServerAddress.parse(s.address),info,false,null);return true;}sy+=30;}
        if(!addServer&&b==0&&hit(x,y,px+12,qy+122,90,20)){addServer=true;nameField.visible=true;addressField.visible=true;setFocused(nameField);return true;}
        if(addServer&&b==0&&hit(x,y,px+182,qy+130,56,20)){String n=nameField.getText().trim(),a=addressField.getText().trim();if(!n.isEmpty()&&!a.isEmpty()){servers.add(new QuickServer(n,a));saveServers();ping(servers.get(servers.size()-1));addServer=false;nameField.visible=false;addressField.visible=false;}return true;}
        if(addServer&&nameField.mouseClicked(click,doubled)){setFocused(nameField);return true;}if(addServer&&addressField.mouseClicked(click,doubled)){setFocused(addressField);return true;}return super.mouseClicked(click,doubled);}
    private boolean hit(int x,int y,int bx,int by,int w,int h){return x>=bx&&x<=bx+w&&y>=by&&y<=by+h;}
    @Override public boolean keyPressed(net.minecraft.client.input.KeyInput i){if(i.key()==256){if(addServer){addServer=false;nameField.visible=false;addressField.visible=false;setFocused(null);return true;}return true;}if(addServer){if(nameField.isFocused()&&nameField.keyPressed(i))return true;if(addressField.isFocused()&&addressField.keyPressed(i))return true;}return super.keyPressed(i);}
    @Override public boolean charTyped(net.minecraft.client.input.CharInput i){if(addServer){if(nameField.isFocused()&&nameField.charTyped(i))return true;if(addressField.isFocused()&&addressField.charTyped(i))return true;}return super.charTyped(i);}
    @Override public boolean shouldPause(){return false;}
    private static final class QuickServer{final String name,address;volatile long ping=-1;volatile boolean online,pinging;QuickServer(String n,String a){name=n;address=a;}}
    private static final class Star{float x,y,speed,alpha;Star(float x,float y,float speed,float alpha){this.x=x;this.y=y;this.speed=speed;this.alpha=alpha;}}
}
