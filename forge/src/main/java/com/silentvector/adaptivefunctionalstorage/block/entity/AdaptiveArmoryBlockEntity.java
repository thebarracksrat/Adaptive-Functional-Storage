package com.silentvector.adaptivefunctionalstorage.block.entity;

import com.silentvector.adaptivefunctionalstorage.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;

public final class AdaptiveArmoryBlockEntity extends BlockEntity {
    private BlockPos controllerPos;

    public AdaptiveArmoryBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ADAPTIVE_ARMORY.get(), pos, state); }

    public BlockPos controllerPos() { return controllerPos; }

    public AdaptiveControllerBlockEntity controller() {
        if (level == null || controllerPos == null) return null;
        return level.getBlockEntity(controllerPos) instanceof AdaptiveControllerBlockEntity controller ? controller : null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) tryAutoLink();
    }

    public void tryAutoLink() {
        if (level == null || level.isClientSide || controller() != null) return;

        List<AdaptiveControllerBlockEntity> eligible = new ArrayList<>();
        int range = AdaptiveControllerBlockEntity.RANGE;
        for (BlockPos pos : BlockPos.betweenClosed(
                worldPosition.offset(-range, -range, -range),
                worldPosition.offset(range, range, range))) {
            if (level.getBlockEntity(pos) instanceof AdaptiveControllerBlockEntity controller) {
                eligible.add(controller);
            }
        }
        if (eligible.size() == 1) linkTo(eligible.get(0));
    }

    public boolean linkTo(AdaptiveControllerBlockEntity controller) {
        if (level == null || controller == null) return false;
        controllerPos = controller.getBlockPos().immutable();
        setChanged();
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (controllerPos != null) tag.putLong("controller_pos", controllerPos.asLong());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        controllerPos = tag.contains("controller_pos") ? BlockPos.of(tag.getLong("controller_pos")) : null;
    }
}
