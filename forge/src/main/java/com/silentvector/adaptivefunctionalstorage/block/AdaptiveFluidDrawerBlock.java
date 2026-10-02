package com.silentvector.adaptivefunctionalstorage.block;

import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveFluidDrawerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidUtil;
import javax.annotation.Nullable;

public final class AdaptiveFluidDrawerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    public AdaptiveFluidDrawerBlock(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new AdaptiveFluidDrawerBlockEntity(p,s);}
    @Override public RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> builder){builder.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
    @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,@Nullable LivingEntity e,ItemStack stack){super.setPlacedBy(l,p,s,e,stack);if(!l.isClientSide&&l.getBlockEntity(p)instanceof AdaptiveFluidDrawerBlockEntity f)f.tryAutoLink();}
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof AdaptiveFluidDrawerBlockEntity fluid) {
            if (FluidUtil.interactWithFluidHandler(player, hand, fluid.tank())) return InteractionResult.sidedSuccess(level.isClientSide);
            var stored = fluid.tank().getFluid();
            player.displayClientMessage(Component.literal(stored.isEmpty() ? "Adaptive Fluid Drawer: empty" : "Adaptive Fluid Drawer: " + stored.getAmount() + " mB " + stored.getDisplayName().getString()), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
