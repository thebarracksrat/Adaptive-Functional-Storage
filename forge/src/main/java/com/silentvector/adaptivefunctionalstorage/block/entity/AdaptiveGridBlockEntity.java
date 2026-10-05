package com.silentvector.adaptivefunctionalstorage.block.entity;

import com.silentvector.adaptivefunctionalstorage.menu.AdaptiveGridMenu;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveGridBlock;
import com.silentvector.adaptivefunctionalstorage.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;

public class AdaptiveGridBlockEntity extends BlockEntity implements MenuProvider {
    private BlockPos controllerPos;
    public AdaptiveGridBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ADAPTIVE_GRID.get(), pos, state); }
    protected AdaptiveGridBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }
    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, AdaptiveGridBlockEntity grid) {
        AdaptiveControllerBlockEntity controller=grid.controller();
        boolean powered=controller!=null&&controller.isPowered();
        if(state.hasProperty(AdaptiveGridBlock.POWERED)&&state.getValue(AdaptiveGridBlock.POWERED)!=powered)
            level.setBlock(pos,state.setValue(AdaptiveGridBlock.POWERED,powered),3);
    }
    public AdaptiveControllerBlockEntity controller() {
        return level != null && controllerPos != null && level.getBlockEntity(controllerPos) instanceof AdaptiveControllerBlockEntity controller ? controller : null;
    }
    @Override public void onLoad() { super.onLoad(); if (level != null && !level.isClientSide) tryAutoLink(); }
    public void tryAutoLink() {
        if (level == null || level.isClientSide || controller() != null) return;
        List<AdaptiveControllerBlockEntity> eligible = new ArrayList<>();
        int range = AdaptiveControllerBlockEntity.RANGE;
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-range, -range, -range), worldPosition.offset(range, range, range)))
            if (level.getBlockEntity(pos) instanceof AdaptiveControllerBlockEntity controller) eligible.add(controller);
        if (eligible.size() == 1) {
            controllerPos = eligible.get(0).getBlockPos().immutable();
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    public boolean linkTo(AdaptiveControllerBlockEntity controller) {
        if (level == null || controller == null) return false;
        controllerPos = controller.getBlockPos().immutable(); setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); return true;
    }
    @Override public Component getDisplayName() { return Component.literal("Adaptive Grid"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new AdaptiveGridMenu(id, inventory, this); }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); if (controllerPos != null) tag.putLong("controller_pos", controllerPos.asLong()); }
    @Override public void load(CompoundTag tag) { super.load(tag); controllerPos = tag.contains("controller_pos") ? BlockPos.of(tag.getLong("controller_pos")) : null; }
    @Override public CompoundTag getUpdateTag() { CompoundTag tag = new CompoundTag(); saveAdditional(tag); return tag; }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) { if (packet.getTag() != null) load(packet.getTag()); }
}
