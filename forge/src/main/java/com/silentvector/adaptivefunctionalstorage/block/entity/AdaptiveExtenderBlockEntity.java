package com.silentvector.adaptivefunctionalstorage.block.entity;

import com.silentvector.adaptivefunctionalstorage.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class AdaptiveExtenderBlockEntity extends BlockEntity {
    private BlockPos controllerPos;
    public AdaptiveExtenderBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ADAPTIVE_EXTENDER.get(), pos, state); }
    public AdaptiveControllerBlockEntity controller() { return level != null && controllerPos != null && level.getBlockEntity(controllerPos) instanceof AdaptiveControllerBlockEntity controller ? controller : null; }
    @Override public void onLoad() { super.onLoad(); if (level != null && !level.isClientSide) tryAutoLink(); }
    public void tryAutoLink() {
        if (level == null || level.isClientSide || controller() != null) return;
        List<AdaptiveControllerBlockEntity> eligible = new ArrayList<>();
        int range = AdaptiveControllerBlockEntity.RANGE;
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-range, -range, -range), worldPosition.offset(range, range, range)))
            if (level.getBlockEntity(pos) instanceof AdaptiveControllerBlockEntity controller) eligible.add(controller);
        if (eligible.size() == 1) linkTo(eligible.get(0));
    }
    public boolean linkTo(AdaptiveControllerBlockEntity controller) {
        if (level == null || controller == null) return false;
        controllerPos = controller.getBlockPos().immutable(); setChanged(); adoptNearby(); return true;
    }
    public void adoptNearby() {
        AdaptiveControllerBlockEntity controller = controller(); if (controller == null || level == null) return;
        int range = AdaptiveControllerBlockEntity.RANGE;
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-range, -range, -range), worldPosition.offset(range, range, range))) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AdaptiveDrawerBlockEntity drawer) drawer.linkTo(controller);
            else if (be instanceof AdaptiveGridBlockEntity grid) grid.linkTo(controller);
            else if (be instanceof AdaptiveDepositBlockEntity deposit) deposit.linkTo(controller);
            else if (be instanceof AdaptiveArmoryBlockEntity armory) armory.linkTo(controller);
            else if (be instanceof AdaptiveFluidDrawerBlockEntity fluid) fluid.linkTo(controller);
        }
    }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); if (controllerPos != null) tag.putLong("controller_pos", controllerPos.asLong()); }
    @Override public void load(CompoundTag tag) { super.load(tag); controllerPos = tag.contains("controller_pos") ? BlockPos.of(tag.getLong("controller_pos")) : null; }
}
