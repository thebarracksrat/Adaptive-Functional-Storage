package com.silentvector.adaptivefunctionalstorage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.silentvector.adaptivefunctionalstorage.AdaptiveFunctionalStorage;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveGridBlock;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveGridBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class AdaptiveGridRenderer implements BlockEntityRenderer<AdaptiveGridBlockEntity> {
    private static final ResourceLocation SCREEN = ResourceLocation.fromNamespaceAndPath(AdaptiveFunctionalStorage.MOD_ID, "textures/block/grid_screen_emissive.png");
    public AdaptiveGridRenderer(BlockEntityRendererProvider.Context context) { }
    @Override public void render(AdaptiveGridBlockEntity grid, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var state = grid.getBlockState();
        if (state.getValue(AdaptiveGridBlock.POWERED)) EmissiveScreenRenderer.render(pose, buffers, overlay, state.getValue(AdaptiveGridBlock.FACING), SCREEN);
    }
}
