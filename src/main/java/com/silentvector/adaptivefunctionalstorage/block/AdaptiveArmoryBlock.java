package com.silentvector.adaptivefunctionalstorage.block;

import com.mojang.serialization.MapCodec;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveArmoryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.Containers;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import java.util.List;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveControllerBlockEntity.DisplayAssignment;
import javax.annotation.Nullable;

public final class AdaptiveArmoryBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public static final MapCodec<AdaptiveArmoryBlock> CODEC=simpleCodec(AdaptiveArmoryBlock::new);
    public AdaptiveArmoryBlock(BlockBehaviour.Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new AdaptiveArmoryBlockEntity(pos,state);}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> builder){builder.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,@Nullable LivingEntity placer,ItemStack stack){super.setPlacedBy(level,pos,state,placer,stack);if(!level.isClientSide&&level.getBlockEntity(pos)instanceof AdaptiveArmoryBlockEntity armory)armory.tryAutoLink();}
    @Override public boolean onDestroyedByPlayer(BlockState state,Level level,BlockPos pos,net.minecraft.world.entity.player.Player player,boolean willHarvest,FluidState fluid){
        if(!level.isClientSide&&level.getBlockEntity(pos)instanceof AdaptiveArmoryBlockEntity armory&&armory.controller()!=null){
            var controller=armory.controller();List<DisplayAssignment> overflow=controller.overflowForArmoryRemoval(pos);long stacks=overflow.stream().mapToLong(DisplayAssignment::amount).sum();
            if(stacks>64){player.displayClientMessage(Component.literal("Armory removal blocked: "+stacks+" item stacks would enter the world (safety limit 64)").withStyle(ChatFormatting.RED),true);return false;}
            controller.detachOverflow(overflow);
            for(DisplayAssignment entry:overflow)for(long left=entry.amount();left>0;left--)Containers.dropItemStack(level,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,entry.identity().copyWithCount(1));
        }
        return super.onDestroyedByPlayer(state,level,pos,player,willHarvest,fluid);
    }
}
