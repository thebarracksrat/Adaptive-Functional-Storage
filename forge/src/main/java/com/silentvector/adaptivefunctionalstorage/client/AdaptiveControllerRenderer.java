package com.silentvector.adaptivefunctionalstorage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.silentvector.adaptivefunctionalstorage.AdaptiveFunctionalStorage;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveControllerBlock;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveControllerBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class AdaptiveControllerRenderer implements BlockEntityRenderer<AdaptiveControllerBlockEntity> {
    private static final ResourceLocation SCREEN=new ResourceLocation(AdaptiveFunctionalStorage.MOD_ID,"textures/block/controller_screen_emissive.png");
    public AdaptiveControllerRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(AdaptiveControllerBlockEntity controller,float partialTick,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var state=controller.getBlockState();
        if(!state.getValue(AdaptiveControllerBlock.POWERED))return;
        EmissiveScreenRenderer.render(pose,buffers,overlay,state.getValue(AdaptiveControllerBlock.FACING),SCREEN);
    }
}
