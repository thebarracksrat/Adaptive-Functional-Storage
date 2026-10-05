package com.silentvector.adaptivefunctionalstorage.block.entity;

import com.silentvector.adaptivefunctionalstorage.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import java.util.ArrayList;
import java.util.List;

public final class AdaptiveFluidDrawerBlockEntity extends BlockEntity {
    private BlockPos controllerPos;
    private final FluidTank tank = new FluidTank(Integer.MAX_VALUE) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public AdaptiveFluidDrawerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADAPTIVE_FLUID_DRAWER.get(), pos, state);
    }

    public FluidTank tank() {
        return tank;
    }

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
        if (eligible.size() == 1) linkTo(eligible.getFirst());
    }

    public boolean linkTo(AdaptiveControllerBlockEntity controller) {
        if (controller == null) return false;
        controllerPos = controller.getBlockPos().immutable();
        setChanged();
        return true;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (controllerPos != null) tag.putLong("controller_pos", controllerPos.asLong());
        tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        controllerPos = tag.contains("controller_pos") ? BlockPos.of(tag.getLong("controller_pos")) : null;
        if (tag.contains("tank")) tank.readFromNBT(registries, tag.getCompound("tank"));
    }
}
