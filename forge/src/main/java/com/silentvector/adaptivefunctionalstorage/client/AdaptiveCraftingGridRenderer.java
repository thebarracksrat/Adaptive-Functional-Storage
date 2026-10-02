package com.silentvector.adaptivefunctionalstorage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.silentvector.adaptivefunctionalstorage.AdaptiveFunctionalStorage;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveCraftingGridBlock;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveCraftingGridBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class AdaptiveCraftingGridRenderer implements BlockEntityRenderer<AdaptiveCraftingGridBlockEntity> {
    private static final ResourceLocation SCREEN = new ResourceLocation(AdaptiveFunctionalStorage.MOD_ID, "textures/block/crafting_grid_screen_emissive.png");
    public AdaptiveCraftingGridRenderer(BlockEntityRendererProvider.Context context) { }
    @Override public void render(AdaptiveCraftingGridBlockEntity grid, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var state = grid.getBlockState();
        if (state.getValue(AdaptiveCraftingGridBlock.POWERED)) EmissiveScreenRenderer.render(pose, buffers, overlay, state.getValue(AdaptiveCraftingGridBlock.FACING), SCREEN);
    }
}
