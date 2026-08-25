package com.silentvector.adaptivefunctionalstorage.block.entity;

import com.silentvector.adaptivefunctionalstorage.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveControllerBlockEntity.DisplayAssignment;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveDrawerBlock;
import net.minecraft.world.level.Level;

public final class AdaptiveDrawerBlockEntity extends BlockEntity {
    private BlockPos controllerPos;
    private final List<OfflineEntry> offlineEntries = new ArrayList<>();
    private final Map<Integer, ItemStack> manualReservations = new LinkedHashMap<>();
    private int pinnedRegions;
    public AdaptiveDrawerBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ADAPTIVE_DRAWER.get(), pos, state); }
    public static void serverTick(Level level, BlockPos pos, BlockState state, AdaptiveDrawerBlockEntity drawer) {
        AdaptiveControllerBlockEntity controller = drawer.findController();
        boolean powered = controller != null && controller.isPowered();
        if (state.hasProperty(AdaptiveDrawerBlock.POWERED) && state.getValue(AdaptiveDrawerBlock.POWERED) != powered) {
            level.setBlock(pos, state.setValue(AdaptiveDrawerBlock.POWERED, powered), 3);
        }
    }
    public int regionCapacity(ItemStack identity, int regions) { return Integer.MAX_VALUE; }
    public BlockPos controllerPos() { return controllerPos; }

    @Override public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) tryAutoLink();
    }

    public void tryAutoLink() {
        if (level == null || level.isClientSide) return;
        if (findController() != null) return;
        List<AdaptiveControllerBlockEntity> eligible = new ArrayList<>();
        int range = AdaptiveControllerBlockEntity.RANGE;
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-range, -range, -range), worldPosition.offset(range, range, range))) {
            if (level.getBlockEntity(pos) instanceof AdaptiveControllerBlockEntity controller && inRange(pos, controller.range())) eligible.add(controller);
        }
        if (eligible.size() == 1) setController(eligible.getFirst());
    }
    public boolean linkTo(AdaptiveControllerBlockEntity controller) {
        if (level == null || controller == null) return false;
        AdaptiveControllerBlockEntity previous = findController();
        if (previous != null) previous.unregisterMember(worldPosition);
        controllerPos = controller.getBlockPos().immutable();
        controller.registerMember(worldPosition); setChanged(); controller.topologyChanged(); return true;
    }

    private void setController(AdaptiveControllerBlockEntity controller) {
        BlockPos previous = controllerPos;
        controllerPos = controller.getBlockPos().immutable();
        if (!controller.registerMember(worldPosition)) return;
        if (!controller.absorbOffline(offlineEntries)) {
            controller.unregisterMember(worldPosition);
            controllerPos = previous;
            return;
        }
        offlineEntries.clear();
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void becomeOffline(List<DisplayAssignment> contents) {
        offlineEntries.clear();
        contents.stream().filter(entry -> !entry.identity().isEmpty() && entry.amount() > 0)
                .forEach(entry -> offlineEntries.add(new OfflineEntry(entry.identity().copyWithCount(1), entry.amount())));
        controllerPos = null;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public List<OfflineEntry> offlineEntries() { return List.copyOf(offlineEntries); }
    public boolean hasReservation(int region) { return manualReservations.containsKey(region); }
    public ItemStack reservation(int region) { return manualReservations.getOrDefault(region, ItemStack.EMPTY).copy(); }
    public boolean hasReservationAtOrAbove(int regions) { return manualReservations.keySet().stream().anyMatch(index -> index >= regions); }
    public boolean hasIdentityReservationAtOrAbove(int regions) { return manualReservations.entrySet().stream()
            .anyMatch(entry -> entry.getKey() >= regions && !entry.getValue().isEmpty()); }
    public void clearEmptyReservationsAtOrAbove(int regions) {
        boolean changed = manualReservations.entrySet().removeIf(entry -> entry.getKey() >= regions && entry.getValue().isEmpty());
        if (changed) setChangedAndSync();
    }
    public int minimumRegionsForReservations() { return manualReservations.keySet().stream().mapToInt(index -> index + 1).max().orElse(1); }
    public int pinnedRegions() { return pinnedRegions; }
    public int reservationCountWithin(int regions) { return (int) manualReservations.keySet().stream().filter(index -> index < regions).count(); }
    public List<ItemStack> reservedIdentitiesWithin(int regions) { return manualReservations.entrySet().stream()
            .filter(entry -> entry.getKey() < regions && !entry.getValue().isEmpty()).map(entry -> entry.getValue().copy()).toList(); }
    public void pinRegions(int regions) { pinnedRegions = regions; setChangedAndSync(); }
    public void clearPinnedRegions() { pinnedRegions = 0; setChangedAndSync(); }
    public String configureRegion(int region, ItemStack current, ItemStack offhand, boolean reserveEmpty) {
        if (reserveEmpty) {
            AdaptiveControllerBlockEntity controller = findController();
            if (controller != null && !controller.canReserveEmpty(this, region))
                return "Cannot reserve this cell empty: no other display cell can receive its item";
            manualReservations.put(region, ItemStack.EMPTY);
            setChangedAndSync();
            return "Cell reserved empty; displayed item moved to another available cell";
        }
        if (!offhand.isEmpty()) {
            manualReservations.put(region, offhand.copyWithCount(1));
            setChangedAndSync();
            return offhand.getHoverName().getString() + " assigned and locked to this cell";
        }
        if (manualReservations.containsKey(region)) {
            manualReservations.remove(region);
            setChangedAndSync();
            return "Cell unlocked";
        }
        if (!current.isEmpty()) {
            manualReservations.put(region, current.copyWithCount(1));
            setChangedAndSync();
            return current.getHoverName().getString() + " locked to this cell";
        }
        manualReservations.put(region, ItemStack.EMPTY);
        setChangedAndSync();
        return "Empty cell locked";
    }
    private void setChangedAndSync() {
        setChanged();
        AdaptiveControllerBlockEntity controller = findController();
        if (controller != null) controller.topologyChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    public void clearOfflineEntries() { offlineEntries.clear(); setChanged(); }

    public void unlink() {
        AdaptiveControllerBlockEntity controller = findController();
        controllerPos = null;
        setChanged();
        if (controller != null) controller.unregisterMember(worldPosition);
    }

    public AdaptiveControllerBlockEntity findController() {
        if (level == null || controllerPos == null) return null;
        if (level.getBlockEntity(controllerPos) instanceof AdaptiveControllerBlockEntity controller) return controller;
        return null;
    }
    private boolean inRange(BlockPos origin, int range) {
        return Math.abs(origin.getX() - worldPosition.getX()) <= range
                && Math.abs(origin.getY() - worldPosition.getY()) <= range
                && Math.abs(origin.getZ() - worldPosition.getZ()) <= range;
    }
    public List<ItemStack> displayedStacks() {
        AdaptiveControllerBlockEntity controller = findController();
        return controller == null ? List.of() : controller.assignmentsFor(worldPosition);
    }
    public List<DisplayAssignment> displayAssignments() {
        AdaptiveControllerBlockEntity controller = findController();
        return controller == null ? List.of() : controller.displayAssignmentsFor(worldPosition);
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (controllerPos != null) tag.putLong("controller_pos", controllerPos.asLong());
        ListTag offline = new ListTag();
        for (OfflineEntry entry : offlineEntries) {
            CompoundTag stored = new CompoundTag();
            stored.put("identity", entry.identity.saveOptional(registries));
            stored.putLong("amount", entry.amount);
            offline.add(stored);
        }
        tag.put("offline_entries", offline);
        ListTag reservations = new ListTag();
        for (Map.Entry<Integer, ItemStack> entry : manualReservations.entrySet()) {
            CompoundTag stored = new CompoundTag();
            stored.putInt("region", entry.getKey());
            if (entry.getValue().isEmpty()) stored.putBoolean("empty", true);
            else stored.put("identity", entry.getValue().saveOptional(registries));
            reservations.add(stored);
        }
        tag.put("manual_reservations", reservations);
        tag.putInt("pinned_regions", pinnedRegions);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        controllerPos = tag.contains("controller_pos") ? BlockPos.of(tag.getLong("controller_pos")) : null;
        offlineEntries.clear();
        ListTag offline = tag.getList("offline_entries", 10);
        for (int index = 0; index < offline.size(); index++) {
            CompoundTag stored = offline.getCompound(index);
            ItemStack identity = com.buuz135.functionalstorage.util.Utils.deserialize(registries, stored.getCompound("identity"));
            long amount = Math.min(Integer.MAX_VALUE, Math.max(0, stored.getLong("amount")));
            if (!identity.isEmpty() && amount > 0) offlineEntries.add(new OfflineEntry(identity.copyWithCount(1), amount));
        }
        manualReservations.clear();
        pinnedRegions = Math.max(0, Math.min(4, tag.getInt("pinned_regions")));
        ListTag reservations = tag.getList("manual_reservations", 10);
        for (int index = 0; index < reservations.size(); index++) {
            CompoundTag stored = reservations.getCompound(index);
            int region = stored.getInt("region");
            if (region < 0 || region > 3) continue;
            ItemStack identity = stored.getBoolean("empty") ? ItemStack.EMPTY
                    : com.buuz135.functionalstorage.util.Utils.deserialize(registries, stored.getCompound("identity"));
            manualReservations.put(region, identity.isEmpty() ? ItemStack.EMPTY : identity.copyWithCount(1));
        }
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { CompoundTag tag = new CompoundTag(); saveAdditional(tag, registries); return tag; }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) { if (packet.getTag() != null) loadAdditional(packet.getTag(), registries); }
    public record OfflineEntry(ItemStack identity, long amount) { }
}
