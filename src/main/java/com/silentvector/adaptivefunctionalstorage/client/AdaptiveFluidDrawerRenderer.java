package com.silentvector.adaptivefunctionalstorage.client;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.client.FluidDrawerRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveFluidDrawerBlock;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveFluidDrawerBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import com.mojang.math.Axis;
import net.minecraft.world.phys.AABB;

public final class AdaptiveFluidDrawerRenderer implements BlockEntityRenderer<AdaptiveFluidDrawerBlockEntity> {
    private static final int MILLIBUCKETS_PER_STEP=1_000;
    private static final int VISUAL_STEPS_TO_FULL=16;
    private static final ControllableDrawerTile.DrawerOptions OPTIONS=new ControllableDrawerTile.DrawerOptions();
    public AdaptiveFluidDrawerRenderer(BlockEntityRendererProvider.Context context){}
    @Override public void render(AdaptiveFluidDrawerBlockEntity drawer,float partialTick,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var fluid=drawer.tank().getFluid();if(fluid.isEmpty())return;
        Direction facing=drawer.getBlockState().getValue(AdaptiveFluidDrawerBlock.FACING);
        if(drawer.getLevel()!=null)light=LevelRenderer.getLightColor(drawer.getLevel(),drawer.getBlockPos().relative(facing));
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-180F));
        switch(facing){
            case NORTH->pose.translate(-1,0,-1);
            case EAST->{pose.translate(0,0,-1);pose.mulPose(Axis.YP.rotationDegrees(-90F));}
            case SOUTH->pose.mulPose(Axis.YP.rotationDegrees(-180F));
            case WEST->{pose.translate(-1,0,0);pose.mulPose(Axis.YP.rotationDegrees(90F));}
            default->{pose.popPose();return;}
        }
        long visibleBuckets=Math.max(1L,((long)fluid.getAmount()+MILLIBUCKETS_PER_STEP-1L)/MILLIBUCKETS_PER_STEP);
        double ratio=Math.min(1D,(double)visibleBuckets/VISUAL_STEPS_TO_FULL);
        AABB bounds=new AABB(.0625,.078125,.0625,.9375,.078125+ratio*.78125,.9375);
        FluidDrawerRenderer.renderFluidStack(pose,buffers,light,overlay,fluid,fluid.getAmount(),drawer.tank().getCapacity(),.007F,OPTIONS,bounds,false,false);
        pose.popPose();
    }
}
