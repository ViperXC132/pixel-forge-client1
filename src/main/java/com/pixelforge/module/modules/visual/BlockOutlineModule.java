package com.pixelforge.module.modules.visual;

import com.pixelforge.module.Category;
import com.pixelforge.module.Module;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.Box;

/** Replaces the vanilla block selection outline with a configurable line box. */
public class BlockOutlineModule extends Module {
    private int outlineColor=0xFFFFFFFF;
    private float lineWidth=2.0f;
    private static boolean registered;

    public BlockOutlineModule(){
        super("Block Outline","Custom block selection outline color and thickness",Category.VISUAL);
        registerRenderer();
    }

    private void registerRenderer(){
        if(registered)return;registered=true;
        WorldRenderEvents.BLOCK_OUTLINE.register((context,hitResult,blockState,vertexConsumer)->{
            BlockOutlineModule module=com.pixelforge.PixelForgeClient.getInstance().getModuleManager().getModule(BlockOutlineModule.class);
            if(module==null||!module.isEnabled())return true;
            try{
                Box box=blockState.getOutlineShape(context.world(),hitResult.getBlockPos()).getBoundingBox().offset(hitResult.getBlockPos());
                var matrices=context.matrixStack();
                VertexConsumer lines=context.consumers().getBuffer(RenderLayer.getLines());
                float r=((module.outlineColor>>16)&255)/255f,g=((module.outlineColor>>8)&255)/255f,b=(module.outlineColor&255)/255f,a=((module.outlineColor>>>24)&255)/255f;
                WorldRenderer.drawBox(matrices,lines,box.expand(0.002),r,g,b,a);
                return false;
            }catch(Throwable ignored){return true;}
        });
    }

    public int getOutlineColor(){return outlineColor;}
    public void setOutlineColor(int color){outlineColor=color;}
    public float getLineWidth(){return lineWidth;}
    public void setLineWidth(float width){lineWidth=Math.max(1f,Math.min(6f,width));}
}
