package com.silentvector.adaptivefunctionalstorage.block;

import com.mojang.serialization.MapCodec;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.LivingEntity;
import javax.annotation.Nullable;

public final class AdaptiveControllerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final MapCodec<AdaptiveControllerBlock> CODEC = simpleCodec(AdaptiveControllerBlock::new);
    public AdaptiveControllerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AdaptiveControllerBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, com.silentvector.adaptivefunctionalstorage.registry.ModBlockEntities.ADAPTIVE_CONTROLLER.get(), AdaptiveControllerBlockEntity::serverTick);
    }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) { builder.add(FACING, POWERED); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }

    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AdaptiveControllerBlockEntity controller) controller.adoptNearbyDrawers();
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof AdaptiveControllerBlockEntity controller) {
            if (!level.isClientSide && player.isCrouching()) {
                var mode = controller.cycleSortMode();
                player.displayClientMessage(Component.literal("Adaptive sort: " + mode.label()), true);
            } else if (level.isClientSide && !player.isCrouching()) {
                player.displayClientMessage(Component.literal("Sort: " + controller.sortMode().label() + " | Layout: " + controller.layoutMode().label() + " | Priority: " + controller.countPriority().label() + " | " + (controller.isPowered() ? "ONLINE" : "OFFLINE") + " | " + controller.energyStorage().getEnergyStored() + " FE"), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AdaptiveControllerBlockEntity controller) {
            if (held.is(com.silentvector.adaptivefunctionalstorage.registry.ModItems.ADAPTIVE_CONFIGURATION_TOOL.get())
                    || held.is(com.buuz135.functionalstorage.FunctionalStorage.LINKING_TOOL.get()))
                return ItemInteractionResult.SUCCESS;
            if (player.isCrouching()) {
                if (held.is(Items.GLASS)) {
                    var mode = controller.cycleLayoutMode();
                    player.displayClientMessage(Component.literal("Adaptive layout: " + mode.label()), true);
                } else if (held.is(Items.COMPASS)) {
                    var priority = controller.cycleCountPriority();
                    player.displayClientMessage(Component.literal("Count priority: " + priority.label()), true);
                } else {
                    var mode = controller.cycleSortMode();
                    player.displayClientMessage(Component.literal("Adaptive sort: " + mode.label()), true);
                }
            } else player.setItemInHand(hand, controller.insert(held));
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AdaptiveControllerBlockEntity controller) {
            if (!controller.handoffToDrawers()) {
                player.displayClientMessage(Component.literal("Controller removal blocked: every linked drawer must be loaded so contents can be handed off safely").withStyle(ChatFormatting.RED), true);
                return false;
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }
}
