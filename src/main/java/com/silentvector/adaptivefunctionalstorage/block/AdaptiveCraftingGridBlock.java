package com.silentvector.adaptivefunctionalstorage.block;

import com.mojang.serialization.MapCodec;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveCraftingGridBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;

public final class AdaptiveCraftingGridBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = AdaptiveGridBlock.POWERED;
    public static final MapCodec<AdaptiveCraftingGridBlock> CODEC = simpleCodec(AdaptiveCraftingGridBlock::new);
    public AdaptiveCraftingGridBlock(BlockBehaviour.Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false)); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AdaptiveCraftingGridBlockEntity(pos, state); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) { builder.add(FACING, POWERED); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, com.silentvector.adaptivefunctionalstorage.registry.ModBlockEntities.ADAPTIVE_CRAFTING_GRID.get(),
                (tickLevel, tickPos, tickState, grid) -> com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveGridBlockEntity.serverTick(tickLevel, tickPos, tickState, grid));
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) { super.setPlacedBy(level, pos, state, placer, stack); if (!level.isClientSide && level.getBlockEntity(pos) instanceof AdaptiveCraftingGridBlockEntity grid) { grid.tryAutoLink(); com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveGridBlockEntity.serverTick(level, pos, level.getBlockState(pos), grid); } }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AdaptiveCraftingGridBlockEntity grid) {
            if (grid.controller() == null) player.displayClientMessage(Component.literal("Adaptive Crafting Grid offline"), true); else player.openMenu(grid, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
