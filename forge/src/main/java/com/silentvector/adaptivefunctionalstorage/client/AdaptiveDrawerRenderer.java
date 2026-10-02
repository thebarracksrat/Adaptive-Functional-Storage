package com.silentvector.adaptivefunctionalstorage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.buuz135.functionalstorage.client.DrawerRenderer;
import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveDrawerBlock;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveDrawerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveControllerBlockEntity.DisplayAssignment;
import com.silentvector.adaptivefunctionalstorage.AdaptiveFunctionalStorage;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import static com.buuz135.functionalstorage.util.MathUtils.createTransformMatrix;

public final class AdaptiveDrawerRenderer implements BlockEntityRenderer<AdaptiveDrawerBlockEntity> {
    private static final ResourceLocation SCREEN=new ResourceLocation(AdaptiveFunctionalStorage.MOD_ID,"textures/block/drawer_screen_emissive.png");
    private static final ControllableDrawerTile.DrawerOptions DISPLAY_OPTIONS = new ControllableDrawerTile.DrawerOptions();
    public AdaptiveDrawerRenderer(BlockEntityRendererProvider.Context context) { }

    @Override public void render(AdaptiveDrawerBlockEntity drawer, float partialTick, PoseStack pose, MultiBufferSource buffers,
                                 int light, int overlay) {
        if(drawer.getBlockState().getValue(AdaptiveDrawerBlock.POWERED)) {
            int regions=drawer.getBlockState().getValue(AdaptiveDrawerBlock.REGIONS);
            float[][] screens=switch(regions){
                case 1->new float[][]{{1/16F,1/16F,15/16F,15/16F}};
                case 2->new float[][]{{1/16F,8.5F/16F,15/16F,15/16F},{1/16F,1/16F,15/16F,7.5F/16F}};
                default->new float[][]{
                        {1/16F,8.5F/16F,7.5F/16F,15/16F},{8.5F/16F,8.5F/16F,15/16F,15/16F},
                        {1/16F,1/16F,7.5F/16F,7.5F/16F},{8.5F/16F,1/16F,15/16F,7.5F/16F}};
            };
            EmissiveScreenRenderer.render(pose,buffers,overlay,drawer.getBlockState().getValue(AdaptiveDrawerBlock.FACING),SCREEN,screens);
        }
        List<DisplayAssignment> assignments = drawer.displayAssignments();
        if (assignments.isEmpty()) return;
        Direction facing = drawer.getBlockState().getValue(AdaptiveDrawerBlock.FACING);
        if (drawer.getLevel() != null) light = LevelRenderer.getLightColor(drawer.getLevel(), drawer.getBlockPos().relative(facing));
        pose.pushPose();
        pose.mulPoseMatrix(createTransformMatrix(new Vector3f(0), new Vector3f(0, 180, 0), 1));
        switch (facing) {
            case NORTH -> pose.mulPoseMatrix(createTransformMatrix(new Vector3f(-1, 0, 0), new Vector3f(0), 1));
            case EAST -> pose.mulPoseMatrix(createTransformMatrix(new Vector3f(-1, 0, -1), new Vector3f(0, -90, 0), 1));
            case SOUTH -> pose.mulPoseMatrix(createTransformMatrix(new Vector3f(0, 0, -1), new Vector3f(0, 180, 0), 1));
            case WEST -> pose.mulPoseMatrix(createTransformMatrix(new Vector3f(0), new Vector3f(0, 90, 0), 1));
            default -> { pose.popPose(); return; }
        }
        pose.translate(0, 0, -0.5 / 16D);
        int regions = drawer.getBlockState().getValue(AdaptiveDrawerBlock.REGIONS);
        float[][] positions = switch (regions) {
            case 1 -> new float[][]{{0.5F, 0.5F}};
            case 2 -> new float[][]{{0.5F, 0.77F}, {0.5F, 0.27F}};
            default -> new float[][]{{0.75F, 0.77F}, {0.25F, 0.77F}, {0.75F, 0.27F}, {0.25F, 0.27F}};
        };
        int visibleAssignments = Math.min(assignments.size(), positions.length);
        for (int index = 0; index < visibleAssignments; index++) {
            ItemStack displayed = assignments.get(index).identity();
            if (displayed.isEmpty()) continue;
            pose.pushPose();
            pose.mulPoseMatrix(createTransformMatrix(new Vector3f(positions[index][0], positions[index][1], 0.0005F), new Vector3f(0),
                    regions == 1 ? new Vector3f(1) : new Vector3f(.5F, .5F, 1)));
            int amount = (int) Math.min(Integer.MAX_VALUE, assignments.get(index).amount());
            int capacity = drawer.regionCapacity(displayed, assignments.size());
            DrawerRenderer.renderStack(pose, buffers, light, overlay, displayed, amount, capacity,
                    regions == 1 ? 0.015F : 0.02F, DISPLAY_OPTIONS, drawer.getLevel());
            pose.popPose();
        }
        pose.popPose();
    }
}
