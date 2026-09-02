package com.pixelforge.account;

import com.pixelforge.PixelForgeClient;
import com.pixelforge.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/** Applies an account to the live Minecraft session on 1.21.11. */
public final class SessionApplier {
    private SessionApplier() {}

    public static boolean apply(String username, String uuidStr, String accessToken, AccountManager.AccountType type) {
        MinecraftClient client=MinecraftClient.getInstance();
        if(client==null||username==null||username.isBlank())return false;
        try{
            UUID uuid;
            try{
                String normalized=uuidStr==null?"":uuidStr.trim();
                if(normalized.length()==32&&!normalized.contains("-"))normalized=insertDashes(normalized);
                uuid=UUID.fromString(normalized);
            }catch(Exception ignored){uuid=UUID.nameUUIDFromBytes(("OfflinePlayer:"+username).getBytes(StandardCharsets.UTF_8));}
            String token=accessToken==null||accessToken.isBlank()?"0":accessToken;
            Session session=new Session(username,uuid,token,Optional.empty(),Optional.empty());
            ((MinecraftClientAccessor)(Object)client).pixelforge$setSession(session);
            PixelForgeClient.LOGGER.info("Session applied: {} ({})",username,type.displayName);
            return true;
        }catch(Throwable t){PixelForgeClient.LOGGER.error("Failed to apply session for {}",username,t);return false;}
    }

    private static String insertDashes(String flat){return flat.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)","$1-$2-$3-$4-$5");}

    public static String currentUsername(){try{MinecraftClient c=MinecraftClient.getInstance();if(c!=null&&c.getSession()!=null)return c.getSession().getUsername();}catch(Throwable ignored){}return "?";}
}
