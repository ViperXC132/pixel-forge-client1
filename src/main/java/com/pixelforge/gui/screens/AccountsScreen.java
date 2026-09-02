package com.pixelforge.gui.screens;

import com.pixelforge.account.AccountManager;
import com.pixelforge.account.AccountManager.Account;
import com.pixelforge.account.AccountManager.AccountType;
import com.pixelforge.account.SessionApplier;
import com.pixelforge.account.SkinHelper;
import com.pixelforge.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AccountsScreen extends Screen {
    private final Screen parent; private TextFieldWidget userField,passField; private AccountType selectedType=AccountType.OFFLINE;
    private String status=""; private boolean busy=false;
    private static final int ACCENT=0xFF3B5BDB,TEXT=0xFFC8D0E0,DIM=0xFF8892A8,MUTED=0xFF3D4A6A,PANEL=0xD0101424;
    public AccountsScreen(Screen parent){super(Text.literal("Accounts"));this.parent=parent;}
    @Override protected void init(){
        userField=new TextFieldWidget(textRenderer,20,0,width-40,18,Text.literal("User"));userField.setPlaceholder(Text.literal("Username / email"));userField.setMaxLength(64);addSelectableChild(userField);
        passField=new TextFieldWidget(textRenderer,20,0,width-40,18,Text.literal("Pass"));passField.setPlaceholder(Text.literal("Password (empty for Offline)"));passField.setMaxLength(128);addSelectableChild(passField);
    }
    @Override public void render(DrawContext c,int mx,int my,float d){
        RenderUtil.fill(c,0,0,width,height,0xB0080A12);RenderUtil.fill(c,0,0,width,32,0xE00A0C14);RenderUtil.drawText(c,textRenderer,"Accounts",14,11,TEXT,false);RenderUtil.drawText(c,textRenderer,"Active: "+SessionApplier.currentUsername(),120,11,0xFF748FFF,false);
        RenderUtil.drawText(c,textRenderer,"SAVED ACCOUNTS — click Switch to apply session",16,42,ACCENT,false);int y=56;
        for(Account a:AccountManager.getAccounts()){RenderUtil.fill(c,16,y,width-16,y+30,PANEL);RenderUtil.drawBorder(c,16,y,width-32,30,0xFF1E2540);SkinHelper.drawHead(c,a.username,22,y+5,20);RenderUtil.drawText(c,textRenderer,a.username,48,y+5,TEXT,false);RenderUtil.drawText(c,textRenderer,a.type.displayName+(a.active?" · Active":""),48,y+16,MUTED,false);if(!a.active)RenderUtil.drawText(c,textRenderer,"Switch",width-58,y+11,ACCENT,false);else RenderUtil.fill(c,width-28,y+12,width-22,y+18,0xFF40C057);y+=34;}
        y+=6;RenderUtil.drawText(c,textRenderer,"LOGIN / ADD",16,y,ACCENT,false);y+=14;drawTypeBtn(c,16,y,"Offline",selectedType==AccountType.OFFLINE);drawTypeBtn(c,70,y,"ely.by",selectedType==AccountType.ELYBY);drawTypeBtn(c,125,y,"LittleSkin",selectedType==AccountType.LITTLESKIN);drawTypeBtn(c,200,y,"Microsoft",selectedType==AccountType.MICROSOFT);
        userField.setY(y+20);passField.setY(y+42);userField.render(c,mx,my,d);passField.render(c,mx,my,d);String typed=userField.getText().trim();if(!typed.isEmpty())SkinHelper.drawHead(c,typed,width-48,y+24,24);
        int by=y+68;RenderUtil.fill(c,20,by,width-20,by+18,busy?0x40333333:0x403B5BDB);RenderUtil.drawBorder(c,20,by,width-40,18,busy?MUTED:ACCENT);RenderUtil.drawCenteredText(c,textRenderer, busy?"Working...":"Login & Apply Session",width/2,by+5,busy?DIM:0xFF748FFF,false);
        if(!status.isEmpty()){int sc=status.startsWith("OK:")?0xFF40C057:0xFFFA5252;RenderUtil.drawText(c,textRenderer,status.startsWith("OK:")?status.substring(3):status,20,by+24,sc,false);}
        RenderUtil.drawText(c,textRenderer,"Microsoft: click Login to open secure browser/device-code sign-in.",12,height-14,MUTED,false);super.render(c,mx,my,d);
    }
    private void drawTypeBtn(DrawContext c,int x,int y,String label,boolean on){int w=textRenderer.getWidth(label)+12;RenderUtil.fill(c,x,y,x+w,y+14,on?0x303B5BDB:PANEL);RenderUtil.drawBorder(c,x,y,w,14,on?ACCENT:0xFF1E2540);RenderUtil.drawText(c,textRenderer,label,x+6,y+3,on?0xFF748FFF:MUTED,false);}
    @Override public boolean mouseClicked(net.minecraft.client.gui.Click click,boolean doubled){
        double mx=click.x(),my=click.y();int y=56;for(Account a:AccountManager.getAccounts()){if(!a.active&&mx>=width-70&&mx<=width-16&&my>=y&&my<=y+30){AccountManager.switchTo(a);status="OK:Switched to "+a.username;return true;}y+=34;}
        y+=20;if(my>=y&&my<=y+14){if(mx>=16&&mx<65)selectedType=AccountType.OFFLINE;else if(mx>=70&&mx<120)selectedType=AccountType.ELYBY;else if(mx>=125&&mx<195)selectedType=AccountType.LITTLESKIN;else if(mx>=200&&mx<300)selectedType=AccountType.MICROSOFT;else return super.mouseClicked(click,doubled);return true;}
        int by=y+68;if(!busy&&my>=by&&my<=by+18&&mx>=20&&mx<=width-20){
            if(selectedType==AccountType.MICROSOFT){busy=true;status="Opening Microsoft sign-in...";AccountManager.loginMicrosoftAsync(msg->{busy=false;status=msg;});return true;}
            String user=userField.getText().trim(),pass=passField.getText();if(user.isEmpty()){status="Enter a username";return true;}if(selectedType!=AccountType.OFFLINE&&(pass==null||pass.isEmpty())){status="Password required for "+selectedType.displayName;return true;}busy=true;status="Authenticating...";AccountManager.loginAsync(selectedType,user,pass==null?"":pass,msg->{busy=false;status=msg;if(msg.startsWith("OK:"))passField.setText("");});return true;}
        return userField.mouseClicked(click,doubled)||passField.mouseClicked(click,doubled)||super.mouseClicked(click,doubled);
    }
    @Override public boolean keyPressed(net.minecraft.client.input.KeyInput i){if(i.key()==256){client.setScreen(parent);return true;}return userField.keyPressed(i)||passField.keyPressed(i)||super.keyPressed(i);}
    @Override public boolean charTyped(net.minecraft.client.input.CharInput i){return userField.charTyped(i)||passField.charTyped(i)||super.charTyped(i);}
    @Override public boolean shouldPause(){return false;}
}
