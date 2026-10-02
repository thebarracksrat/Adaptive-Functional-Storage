package com.silentvector.adaptivefunctionalstorage.block;

import com.hrznstudio.titanium.block.RotatableBlock;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.LivingEntity;
import javax.annotation.Nullable;

public final class AdaptiveControllerBlock extends RotatableBlock<AdaptiveControllerBlockEntity> {
    public static final DirectionProperty FACING = FACING_HORIZONTAL;
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public AdaptiveControllerBlock(BlockBehaviour.Properties properties) {
        super("adaptive_matrix", properties, AdaptiveControllerBlockEntity.class);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }
    @Override public RotationType getRotationType() { return RotationType.FOUR_WAY; }
    @Override public BlockEntityType.BlockEntitySupplier<?> getTileEntityFactory() { return AdaptiveControllerBlockEntity::new; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return getTileEntityFactory().create(pos, state);
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) { super.createBlockStateDefinition(builder); builder.add(POWERED); }

    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AdaptiveControllerBlockEntity controller) controller.adoptNearbyDrawers();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (level.getBlockEntity(pos) instanceof AdaptiveControllerBlockEntity controller) {
            if (held.is(com.silentvector.adaptivefunctionalstorage.registry.ModItems.ADAPTIVE_CONFIGURATION_TOOL.get())
                    || held.is(com.buuz135.functionalstorage.FunctionalStorage.LINKING_TOOL.get())) {
                return InteractionResult.SUCCESS;
            }
            if (!level.isClientSide) {
                if (player.isCrouching()) {
                    if (held.is(Items.GLASS)) {
                        player.displayClientMessage(Component.literal("Adaptive layout: " + controller.cycleLayoutMode().label()), true);
                    } else if (held.is(Items.COMPASS)) {
                        player.displayClientMessage(Component.literal("Count priority: " + controller.cycleCountPriority().label()), true);
                    } else if (held.isEmpty()) {
                        player.displayClientMessage(Component.literal("Adaptive sort: " + controller.cycleSortMode().label()), true);
                    } else {
                        player.displayClientMessage(Component.literal("Adaptive sort: " + controller.cycleSortMode().label()), true);
                    }
                } else if (held.isEmpty()) {
                    player.displayClientMessage(Component.literal("Sort: " + controller.sortMode().label() + " | Layout: " + controller.layoutMode().label() + " | Priority: " + controller.countPriority().label() + " | " + (controller.isPowered() ? "ONLINE" : "OFFLINE") + " | " + controller.energyDrawPerTick() + " FE/t | " + controller.energyStorage().getEnergyStored() + " FE"), true);
                } else {
                    player.setItemInHand(hand, controller.insert(held));
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
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
