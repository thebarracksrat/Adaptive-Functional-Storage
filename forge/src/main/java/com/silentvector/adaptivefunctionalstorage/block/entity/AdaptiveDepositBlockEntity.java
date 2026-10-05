package com.silentvector.adaptivefunctionalstorage.block.entity;

import com.silentvector.adaptivefunctionalstorage.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public final class AdaptiveDepositBlockEntity extends BlockEntity {
    private BlockPos controllerPos;
    private final IItemHandler handler = new IItemHandler() {
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            AdaptiveControllerBlockEntity controller = controller();
            if (slot != 0 || controller == null || stack.isEmpty()) return stack;
            return controller.insertNetwork(stack, simulate);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return 64; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot == 0; }
    };

    public AdaptiveDepositBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ADAPTIVE_DEPOSIT.get(), pos, state); }
    public AdaptiveControllerBlockEntity controller() { return level != null && controllerPos != null && level.getBlockEntity(controllerPos) instanceof AdaptiveControllerBlockEntity c ? c : null; }
    public IItemHandler itemHandler() { return handler; }
    private final net.minecraftforge.common.util.LazyOptional<IItemHandler> itemOptional =
            net.minecraftforge.common.util.LazyOptional.of(() -> handler);

    @Override
    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(
            net.minecraftforge.common.capabilities.Capability<T> cap, @javax.annotation.Nullable net.minecraft.core.Direction side) {
        if (cap == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER) return itemOptional.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemOptional.invalidate();
    }
    @Override public void onLoad() { super.onLoad(); if (level != null && !level.isClientSide) tryAutoLink(); }
    public void tryAutoLink() {
        if (level == null || level.isClientSide || controller() != null) return;
        List<AdaptiveControllerBlockEntity> eligible = new ArrayList<>();
        int range = AdaptiveControllerBlockEntity.RANGE;
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-range, -range, -range), worldPosition.offset(range, range, range)))
            if (level.getBlockEntity(pos) instanceof AdaptiveControllerBlockEntity controller) eligible.add(controller);
        if (eligible.size() == 1) { controllerPos = eligible.get(0).getBlockPos().immutable(); setChanged(); }
    }
    public boolean linkTo(AdaptiveControllerBlockEntity controller) { if (level == null || controller == null) return false; controllerPos = controller.getBlockPos().immutable(); setChanged(); return true; }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); if (controllerPos != null) tag.putLong("controller_pos", controllerPos.asLong()); }
    @Override public void load(CompoundTag tag) { super.load(tag); controllerPos = tag.contains("controller_pos") ? BlockPos.of(tag.getLong("controller_pos")) : null; }
}
