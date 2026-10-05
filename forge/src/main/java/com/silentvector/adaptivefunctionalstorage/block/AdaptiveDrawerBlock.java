package com.silentvector.adaptivefunctionalstorage.block;

import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveControllerBlockEntity;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveDrawerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.Containers;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveControllerBlockEntity.DisplayAssignment;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nullable;

import java.util.List;

public final class AdaptiveDrawerBlock extends BaseEntityBlock {
    private static final int MAX_BREAK_DROP_STACKS = 64;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty REGIONS = IntegerProperty.create("regions", 1, 4);
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public AdaptiveDrawerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(REGIONS, 1).setValue(POWERED, false));
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AdaptiveDrawerBlockEntity(pos, state); }
    @Nullable
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, com.silentvector.adaptivefunctionalstorage.registry.ModBlockEntities.ADAPTIVE_DRAWER.get(), AdaptiveDrawerBlockEntity::serverTick);
    }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) { builder.add(FACING, REGIONS, POWERED); }
    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }

    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AdaptiveDrawerBlockEntity drawer) {
            drawer.tryAutoLink();
            AdaptiveControllerBlockEntity controller = drawer.findController();
            if (controller != null) {
                BlockState linkedState = level.getBlockState(pos);
                if (linkedState.getValue(POWERED) != controller.isPowered())
                    level.setBlock(pos, linkedState.setValue(POWERED, controller.isPowered()), 3);
                controller.refreshDrawerLayouts();
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(com.silentvector.adaptivefunctionalstorage.registry.ModItems.ADAPTIVE_CONFIGURATION_TOOL.get())
                || held.is(com.buuz135.functionalstorage.FunctionalStorage.LINKING_TOOL.get())) {
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AdaptiveDrawerBlockEntity drawer) {
            if (player.isCrouching() && held.isEmpty() && isBorder(state, pos, hit)) {
                AdaptiveControllerBlockEntity controller = drawer.findController();
                if (controller != null && controller.layoutMode() == AdaptiveControllerBlockEntity.LayoutMode.MANUAL) {
                    int current = state.getValue(REGIONS);
                    int next = current == 1 ? 2 : current == 2 ? 4 : 1;
                    if (controller.setManualRegions(pos, next)) {
                        player.displayClientMessage(Component.literal("Manual drawer layout: " + (next == 1 ? "1x1" : next == 2 ? "2x1" : "2x2")), true);
                    } else {
                        player.displayClientMessage(Component.literal("Cannot shrink drawer: a removed cell has an item lock, or no unpinned drawer can expand for displaced items").withStyle(ChatFormatting.RED), true);
                    }
                }
            } else if (!held.isEmpty()) {
                AdaptiveControllerBlockEntity controller = drawer.findController();
                if (controller == null) player.displayClientMessage(Component.literal("No Adaptive Controller within 8 blocks"), true);
                else player.setItemInHand(hand, controller.insert(held));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static boolean isBorder(BlockState state, BlockPos pos, BlockHitResult hit) {
        Direction facing = state.getValue(FACING);
        double horizontal = switch (facing) {
            case NORTH -> hit.getLocation().x - pos.getX();
            case SOUTH -> 1D - (hit.getLocation().x - pos.getX());
            case WEST -> 1D - (hit.getLocation().z - pos.getZ());
            case EAST -> hit.getLocation().z - pos.getZ();
            default -> 0.5D;
        };
        double vertical = hit.getLocation().y - pos.getY();
        return horizontal < 0.12D || horizontal > 0.88D || vertical < 0.12D || vertical > 0.88D;
    }

    public static int selectedRegion(BlockState state, BlockPos pos, BlockHitResult hit, int regions) {
        if (regions <= 1) return 0;
        double localY = hit.getLocation().y - pos.getY();
        if (regions == 2) return localY >= 0.5 ? 0 : 1;
        Direction facing = state.getValue(FACING);
        double horizontal = switch (facing) {
            case NORTH -> hit.getLocation().x - pos.getX();
            case SOUTH -> 1D - (hit.getLocation().x - pos.getX());
            case WEST -> 1D - (hit.getLocation().z - pos.getZ());
            case EAST -> hit.getLocation().z - pos.getZ();
            default -> 0.5D;
        };
        int index = (localY >= 0.5 ? 0 : 2) + (horizontal >= 0.5 ? 1 : 0);
        return index < regions ? index : regions - 1;
    }

    @Override public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AdaptiveDrawerBlockEntity drawer) {
            AdaptiveControllerBlockEntity controller = drawer.findController();
            List<DisplayAssignment> overflow = controller == null
                    ? drawer.offlineEntries().stream().map(entry -> new DisplayAssignment(entry.identity(), entry.amount())).toList()
                    : controller.overflowForDrawerRemoval(pos);
            long stackCount = legalStackCount(overflow);
            if (stackCount > MAX_BREAK_DROP_STACKS) {
                player.displayClientMessage(Component.literal("Drawer removal blocked: " + stackCount + " item stacks would enter the world (safety limit " + MAX_BREAK_DROP_STACKS + ")").withStyle(ChatFormatting.RED), true);
                return false;
            }
            if (!overflow.isEmpty()) {
                if (controller == null) drawer.clearOfflineEntries(); else controller.detachOverflow(overflow);
                dropContents(level, pos, overflow);
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    private static long legalStackCount(List<DisplayAssignment> contents) {
        long stacks = 0;
        for (DisplayAssignment entry : contents) {
            long size = Math.max(1, entry.identity().getMaxStackSize());
            stacks += (entry.amount() + size - 1) / size;
            if (stacks > MAX_BREAK_DROP_STACKS) return stacks;
        }
        return stacks;
    }

    private static void dropContents(Level level, BlockPos pos, List<DisplayAssignment> contents) {
        for (DisplayAssignment entry : contents) {
            long remaining = entry.amount();
            int max = Math.max(1, entry.identity().getMaxStackSize());
            while (remaining > 0) {
                int amount = (int) Math.min(max, remaining);
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(entry.identity(), amount));
                remaining -= amount;
            }
        }
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        AdaptiveControllerBlockEntity controller = null;
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof AdaptiveDrawerBlockEntity drawer) controller = drawer.findController();
        super.onRemove(state, level, pos, replacement, moving);
        if (controller != null && !level.isClientSide) controller.unregisterMember(pos);
    }
}
