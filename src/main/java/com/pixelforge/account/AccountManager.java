package com.pixelforge.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pixelforge.PixelForgeClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class AccountManager {
    public enum AccountType { MICROSOFT("Microsoft"), ELYBY("ely.by"), LITTLESKIN("LittleSkin"), OFFLINE("Offline");
        public final String displayName; AccountType(String displayName){this.displayName=displayName;} }
    public static class Account {
        public String username; public AccountType type; public boolean active; public String uuid;
        public String accessToken; public String refreshToken;
        public Account() {}
        public Account(String username,AccountType type,boolean active,String uuid,String accessToken){this(username,type,active,uuid,accessToken,null);}
        public Account(String username,AccountType type,boolean active,String uuid,String accessToken,String refreshToken){this.username=username;this.type=type;this.active=active;this.uuid=uuid;this.accessToken=accessToken;this.refreshToken=refreshToken;}
    }
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE=FabricLoader.getInstance().getConfigDir().resolve("pixelforge/accounts.json");
    private static final List<Account> ACCOUNTS=new ArrayList<>();
    static { load(); if(ACCOUNTS.isEmpty()){String name="Player";try{if(MinecraftClient.getInstance()!=null&&MinecraftClient.getInstance().getSession()!=null)name=MinecraftClient.getInstance().getSession().getUsername();}catch(Exception ignored){} AuthService.AuthResult r=AuthService.login(AccountType.OFFLINE,name,"");if(r.ok){ACCOUNTS.add(new Account(r.username,AccountType.OFFLINE,true,r.uuid,r.accessToken));save();}} }
    private AccountManager(){}
    public static List<Account> getAccounts(){return Collections.unmodifiableList(ACCOUNTS);}

    public static void loginAsync(AccountType type,String username,String password,Consumer<String> callback){
        CompletableFuture.runAsync(()->{AuthService.AuthResult r=AuthService.login(type,username,password);applyResult(r,callback);});
    }
    public static void loginMicrosoftAsync(Consumer<String> callback){
        AuthService.loginMicrosoftAsync(r->applyResult(r,callback));
    }
    private static void applyResult(AuthService.AuthResult r,Consumer<String> callback){
        MinecraftClient.getInstance().execute(()->{
            if(!r.ok){callback.accept(r.message);return;}
            if(!SessionApplier.apply(r.username,r.uuid,r.accessToken,r.type)){callback.accept("Authentication succeeded but Minecraft rejected the session");return;}
            for(Account a:ACCOUNTS)a.active=false;
            Account existing=null; for(Account a:ACCOUNTS)if(a.username.equalsIgnoreCase(r.username)&&a.type==r.type){existing=a;break;}
            if(existing==null){existing=new Account(r.username,r.type,true,r.uuid,r.accessToken,r.refreshToken);ACCOUNTS.add(existing);}else{existing.active=true;existing.uuid=r.uuid;existing.accessToken=r.accessToken;if(r.refreshToken!=null)existing.refreshToken=r.refreshToken;}
            save(); PixelForgeClient.getInstance().getNotificationManager().push("Logged in as "+r.username,0xFF40C057); callback.accept("OK:"+r.username);
        });
    }
    public static void switchTo(Account account){
        if(account.uuid==null||account.accessToken==null){PixelForgeClient.getInstance().getNotificationManager().push("Re-login required for "+account.username,0xFFFFAA00);return;}
        if(!SessionApplier.apply(account.username,account.uuid,account.accessToken,account.type)){PixelForgeClient.getInstance().getNotificationManager().push("Failed to switch session",0xFFFF5555);return;}
        for(Account a:ACCOUNTS)a.active=(a==account);save();PixelForgeClient.getInstance().getNotificationManager().push("Switched to "+account.username,0xFF3B5BDB);
    }
    public static Account getActive(){return ACCOUNTS.stream().filter(a->a.active).findFirst().orElse(null);}
    private static void load(){try{if(!Files.exists(FILE))return;String json=Files.readString(FILE);Type t=new TypeToken<List<Account>>(){}.getType();List<Account> loaded=GSON.fromJson(json,t);if(loaded!=null){ACCOUNTS.clear();ACCOUNTS.addAll(loaded);}}catch(Exception e){PixelForgeClient.LOGGER.warn("Failed to load accounts",e);}}
    private static void save(){try{Files.createDirectories(FILE.getParent());Files.writeString(FILE,GSON.toJson(ACCOUNTS));}catch(IOException e){PixelForgeClient.LOGGER.error("Failed to save accounts",e);}}
}
