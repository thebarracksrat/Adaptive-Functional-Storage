package com.silentvector.adaptivefunctionalstorage.block.entity;

import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.StorageControllerTile;
import com.buuz135.functionalstorage.inventory.ILockable;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveDrawerBlock;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveControllerBlock;
import com.silentvector.adaptivefunctionalstorage.registry.ModBlockEntities;
import com.silentvector.adaptivefunctionalstorage.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.energy.EnergyStorage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

public final class AdaptiveControllerBlockEntity extends ItemControllableDrawerTile<AdaptiveControllerBlockEntity> {
    public static final int RANGE = 24;
    public static final long MAX_PER_IDENTITY = Integer.MAX_VALUE;
    private final List<BulkEntry> ledger = new ArrayList<>();
    private SortMode sortMode = SortMode.COUNT;
    private LayoutMode layoutMode = LayoutMode.BALANCED;
    private CountPriority countPriority = CountPriority.UP;
    private PlanSnapshot cachedPresentation;
    private List<BlockPos> cachedDrawers;
    private final Set<BlockPos> members = new LinkedHashSet<>();
    private final ControllerEnergyStorage energy = new ControllerEnergyStorage(1_000_000, 1_000_000);
    private boolean powered;
    private BlockPos cachedFsControllerPos;
    private int cachedFsLinkCount = -1;

    private final IItemHandler matrixStorage = createMatrixStorage();

    private final net.minecraftforge.common.util.LazyOptional<IItemHandler> itemOptional =
            net.minecraftforge.common.util.LazyOptional.of(() -> matrixStorage);
    private final net.minecraftforge.common.util.LazyOptional<net.minecraftforge.energy.IEnergyStorage> energyOptional =
            net.minecraftforge.common.util.LazyOptional.of(() -> energy);

    public AdaptiveControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ADAPTIVE_CONTROLLER.get(), ModBlockEntities.ADAPTIVE_CONTROLLER.get(), pos, state);
    }

    @Override public AdaptiveControllerBlockEntity getSelf() { return this; }
    @Override public int getStorageSlotAmount() { return 0; }
    @Override public IItemHandler getStorage() { return matrixStorage; }
    @Override public net.minecraftforge.common.util.LazyOptional<IItemHandler> getOptional() { return itemOptional; }
    @Override public int getBaseSize(int slot) { return 0; }
    @Override public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.literal("Adaptive Matrix");
    }

    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
            int id, net.minecraft.world.entity.player.Inventory inventory, net.minecraft.world.entity.player.Player player) {
        return null;
    }

    @Override
    public <U> net.minecraftforge.common.util.LazyOptional<U> getCapability(
            net.minecraftforge.common.capabilities.Capability<U> cap, @javax.annotation.Nullable net.minecraft.core.Direction side) {
        if (cap == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER) return itemOptional.cast();
        if (cap == net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY) return energyOptional.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemOptional.invalidate();
        energyOptional.invalidate();
    }
    public boolean hasContents() { return !ledger.isEmpty(); }
    public int range() { return RANGE; }
    public SortMode sortMode() { return sortMode; }
    public SortMode cycleSortMode() { sortMode = sortMode.next(); contentsChanged(); return sortMode; }
    public LayoutMode layoutMode() { return layoutMode; }
    public LayoutMode cycleLayoutMode() { layoutMode = layoutMode.next(); contentsChanged(); return layoutMode; }
    public CountPriority countPriority() { return countPriority; }
    public CountPriority cycleCountPriority() {
        countPriority = countPriority.next();
        cachedDrawers = null;
        contentsChanged();
        return countPriority;
    }
    public boolean setManualRegions(BlockPos drawerPos, int regions) {
        if (layoutMode != LayoutMode.MANUAL || level == null || !drawerPositions().contains(drawerPos)) return false;
        if (!(level.getBlockEntity(drawerPos) instanceof AdaptiveDrawerBlockEntity drawer) || drawer.hasIdentityReservationAtOrAbove(regions)) return false;
        drawer.clearEmptyReservationsAtOrAbove(regions);
        while (!manualPresentationFits(drawerPos, regions, null)) {
            AdaptiveDrawerBlockEntity expansionTarget = drawerPositions().stream()
                    .filter(pos -> !pos.equals(drawerPos))
                    .map(level::getBlockEntity)
                    .filter(AdaptiveDrawerBlockEntity.class::isInstance)
                    .map(AdaptiveDrawerBlockEntity.class::cast)
                    .filter(candidate -> candidate.pinnedRegions() == 0)
                    .filter(candidate -> candidate.getBlockState().getValue(AdaptiveDrawerBlock.REGIONS) < 4)
                    .sorted(Comparator.comparingLong(candidate -> candidate.displayedStacks().stream().filter(stack -> !stack.isEmpty()).count()))
                    .findFirst().orElse(null);
            if (expansionTarget == null) return false;
            BlockState expansionState = expansionTarget.getBlockState();
            int current = expansionState.getValue(AdaptiveDrawerBlock.REGIONS);
            level.setBlock(expansionTarget.getBlockPos(), expansionState.setValue(AdaptiveDrawerBlock.REGIONS, current == 1 ? 2 : 4), 3);
            cachedPresentation = null;
        }
        BlockState state = level.getBlockState(drawerPos);
        level.setBlock(drawerPos, state.setValue(AdaptiveDrawerBlock.REGIONS, regions), 3);
        drawer.pinRegions(regions);
        contentsChanged();
        return true;
    }
    public EnergyStorage energyStorage() { return energy; }
    public boolean isPowered() { return powered; }
    public int energyDrawPerTick() {
        if (level == null) return 20;
        long drawers = members.stream()
                .filter(member -> level.getBlockEntity(member) instanceof AdaptiveDrawerBlockEntity drawer
                        && worldPosition.equals(drawer.controllerPos()))
                .count();
        return (int) Math.min(Integer.MAX_VALUE, 20L + 5L * drawers);
    }
    @Override public void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, AdaptiveControllerBlockEntity controller) {
        super.serverTick(level, pos, state, controller);
        int draw = controller.energyDrawPerTick();
        boolean next = controller.energy.getEnergyStored() >= draw;
        if (next) controller.energy.extractEnergy(draw, false);
        boolean powerChanged = controller.powered != next;
        boolean stateChanged = state.hasProperty(AdaptiveControllerBlock.POWERED)
                && state.getValue(AdaptiveControllerBlock.POWERED) != next;
        if (powerChanged) {
            controller.powered = next;
        }
        if (stateChanged) {
            level.setBlock(pos, state.setValue(AdaptiveControllerBlock.POWERED, next), 3);
        }
        if (powerChanged || stateChanged) {
            for (BlockPos drawerPos : controller.drawerPositions()) {
                BlockState drawerState = level.getBlockState(drawerPos);
                if (drawerState.hasProperty(AdaptiveDrawerBlock.POWERED)
                        && drawerState.getValue(AdaptiveDrawerBlock.POWERED) != next)
                    level.setBlock(drawerPos, drawerState.setValue(AdaptiveDrawerBlock.POWERED, next), 3);
            }
            controller.contentsChanged();
        }
    }

    public boolean registerMember(BlockPos pos) {
        boolean changed = members.add(pos.immutable());
        if (changed) { setChanged(); topologyChanged(); }
        return true;
    }

    public void unregisterMember(BlockPos pos) {
        if (members.remove(pos)) { setChanged(); topologyChanged(); }
    }

    public void adoptNearbyDrawers() {
        if (level == null || level.isClientSide) return;
        BlockPos.betweenClosedStream(worldPosition.offset(-RANGE, -RANGE, -RANGE), worldPosition.offset(RANGE, RANGE, RANGE))
                .map(level::getBlockEntity).filter(AdaptiveDrawerBlockEntity.class::isInstance)
                .map(AdaptiveDrawerBlockEntity.class::cast).forEach(AdaptiveDrawerBlockEntity::tryAutoLink);
        BlockPos.betweenClosedStream(worldPosition.offset(-RANGE, -RANGE, -RANGE), worldPosition.offset(RANGE, RANGE, RANGE))
                .map(level::getBlockEntity).filter(AdaptiveGridBlockEntity.class::isInstance)
                .map(AdaptiveGridBlockEntity.class::cast).forEach(AdaptiveGridBlockEntity::tryAutoLink);
        BlockPos.betweenClosedStream(worldPosition.offset(-RANGE, -RANGE, -RANGE), worldPosition.offset(RANGE, RANGE, RANGE))
                .map(level::getBlockEntity).filter(AdaptiveDepositBlockEntity.class::isInstance)
                .map(AdaptiveDepositBlockEntity.class::cast).forEach(AdaptiveDepositBlockEntity::tryAutoLink);
        BlockPos.betweenClosedStream(worldPosition.offset(-RANGE, -RANGE, -RANGE), worldPosition.offset(RANGE, RANGE, RANGE))
                .map(level::getBlockEntity).filter(AdaptiveArmoryBlockEntity.class::isInstance)
                .map(AdaptiveArmoryBlockEntity.class::cast).forEach(AdaptiveArmoryBlockEntity::tryAutoLink);
        BlockPos.betweenClosedStream(worldPosition.offset(-RANGE, -RANGE, -RANGE), worldPosition.offset(RANGE, RANGE, RANGE))
                .map(level::getBlockEntity).filter(AdaptiveFluidDrawerBlockEntity.class::isInstance)
                .map(AdaptiveFluidDrawerBlockEntity.class::cast).forEach(AdaptiveFluidDrawerBlockEntity::tryAutoLink);
    }

    public boolean absorbOffline(List<AdaptiveDrawerBlockEntity.OfflineEntry> entries) {
        if (entries.isEmpty()) return true;
        List<BulkEntry> prospective = new ArrayList<>();
        for (BulkEntry entry : ledger) prospective.add(new BulkEntry(com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(entry.identity, 1), entry.amount));
        for (AdaptiveDrawerBlockEntity.OfflineEntry incoming : entries) {
            BulkEntry match = prospective.stream().filter(entry -> ItemStack.isSameItemSameTags(entry.identity, incoming.identity())).findFirst().orElse(null);
            if (match == null) prospective.add(new BulkEntry(com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(incoming.identity(), 1), incoming.amount()));
            else if (match.amount > MAX_PER_IDENTITY - incoming.amount()) return false;
            else match.amount += incoming.amount();
        }
        if (prospective.size() > drawerPositions().size() * 4) return false;
        ledger.clear();
        ledger.addAll(prospective);
        contentsChanged();
        return true;
    }

    public boolean handoffToDrawers() {
        List<BlockPos> drawers = drawerPositions();
        if (drawers.size() != members.size()) return false;
        PlanSnapshot snapshot = presentationSnapshot();
        for (int index = 0; index < drawers.size(); index++) {
            if (!(level.getBlockEntity(drawers.get(index)) instanceof AdaptiveDrawerBlockEntity drawer)) return false;
            drawer.becomeOffline(snapshot.assignments.get(index));
        }
        ledger.clear();
        members.clear();
        contentsChanged();
        return true;
    }

    public List<DisplayAssignment> overflowForDrawerRemoval(BlockPos drawerPos) {
        int remainingCapacity = Math.max(0, (drawerPositions().size() - 1) * 4);
        int excess = Math.max(0, ledger.size() - remainingCapacity);
        if (excess == 0) return List.of();
        return displayAssignmentsFor(drawerPos).stream()
                .filter(entry -> !entry.identity.isEmpty() && entry.amount > 0)
                .sorted(Comparator.comparingLong(this::legalStackCount))
                .limit(excess).toList();
    }

    public List<DisplayAssignment> overflowForArmoryRemoval(BlockPos armoryPos) {
        long remainingCapacity = Math.max(0L, (long) (armoryPositions().size() - 1) * 16_384L);
        List<BulkEntry> equipment = ledger.stream().filter(entry -> entry.identity.getMaxStackSize() == 1).toList();
        int excess = (int) Math.max(0L, equipment.size() - remainingCapacity);
        if (excess == 0) return List.of();
        return equipment.stream().sorted(Comparator.comparingLong(entry -> entry.amount))
                .limit(excess).map(entry -> new DisplayAssignment(com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(entry.identity, 1), entry.amount)).toList();
    }

    private long legalStackCount(DisplayAssignment assignment) {
        long max = Math.max(1, assignment.identity().getMaxStackSize());
        return (assignment.amount() + max - 1) / max;
    }

    public void detachOverflow(List<DisplayAssignment> overflow) {
        for (DisplayAssignment assignment : overflow) {
            BulkEntry entry = find(assignment.identity());
            if (entry != null) ledger.remove(entry);
        }
        contentsChanged();
    }

    @Override public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) adoptNearbyDrawers();
    }

    public IItemHandler automationHandler() { return matrixStorage; }

    private IItemHandler createMatrixStorage() {
        return new MatrixStorage();
    }

    private final class MatrixStorage implements IItemHandler, ILockable {
            @Override public boolean isLocked() { return true; }
            @Override public int getSlots() { return Math.max(1, drawerPositions().size() * 4); }
            @Override public ItemStack getStackInSlot(int slot) {
                BulkEntry entry = entryAt(slot);
                return entry == null ? ItemStack.EMPTY : com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(entry.identity, (int) Math.min(Integer.MAX_VALUE, entry.amount));
            }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (slot < 0 || slot >= getSlots() || stack.isEmpty()) return stack;
                int accepted = acceptedAmount(stack);
                if (!simulate && accepted > 0) add(stack, accepted);
                return com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(stack, stack.getCount() - accepted);
            }
            @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
                BulkEntry entry = entryAt(slot);
                if (entry == null || amount <= 0) return ItemStack.EMPTY;
                int removed = (int) Math.min(Math.min(amount, entry.identity.getMaxStackSize()), entry.amount);
                if (simulate) return com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(entry.identity, removed);
                return extract(entry.identity, removed);
            }
            @Override public int getSlotLimit(int slot) { return 64; }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return slot >= 0 && slot < getSlots(); }
        }

    private BulkEntry entryAt(int slot) {
        if (slot < 0) return null;
        List<BulkEntry> entries = sortedEntries();
        return slot < entries.size() ? entries.get(slot) : null;
    }

    public ItemStack insert(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (!powered) return stack;
        int accepted = acceptedAmount(stack);
        if (accepted > 0) add(stack, accepted);
        return com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(stack, stack.getCount() - accepted);
    }
    private int acceptedAmount(ItemStack stack) {
        BulkEntry existing = find(stack);
        if (existing == null) {
            boolean armory = stack.getMaxStackSize() == 1;
            long used = ledger.stream().filter(entry -> (entry.identity.getMaxStackSize() == 1) == armory).count();
            long capacity = armory ? (long) armoryPositions().size() * 16_384L : (long) drawerPositions().size() * 4L;
            if (used >= capacity) return 0;
            if (!armory && layoutMode == LayoutMode.MANUAL && !manualPresentationFits(null, 0, stack)) return 0;
        }
        long current = existing == null ? 0 : existing.amount;
        return (int) Math.min(stack.getCount(), Math.max(0, MAX_PER_IDENTITY - current));
    }

    private boolean manualPresentationFits(BlockPos changedDrawer, int changedRegions, ItemStack prospectiveIdentity) {
        if (level == null) return false;
        List<ItemStack> reservedIdentities = new ArrayList<>();
        int totalCells = 0;
        int reservedCells = 0;
        for (BlockPos pos : drawerPositions()) {
            if (!(level.getBlockEntity(pos) instanceof AdaptiveDrawerBlockEntity drawer)) continue;
            int regions = pos.equals(changedDrawer) ? changedRegions : drawer.getBlockState().getValue(AdaptiveDrawerBlock.REGIONS);
            totalCells += regions;
            reservedCells += drawer.reservationCountWithin(regions);
            reservedIdentities.addAll(drawer.reservedIdentitiesWithin(regions));
        }
        long unreservedLive = ledger.stream().filter(entry -> entry.identity.getMaxStackSize() > 1)
                .filter(entry -> reservedIdentities.stream().noneMatch(identity -> ItemStack.isSameItemSameTags(identity, entry.identity))).count();
        if (prospectiveIdentity != null && !prospectiveIdentity.isEmpty()
                && find(prospectiveIdentity) == null
                && reservedIdentities.stream().noneMatch(identity -> ItemStack.isSameItemSameTags(identity, prospectiveIdentity))) unreservedLive++;
        return reservedCells + unreservedLive <= totalCells;
    }
    public boolean canReserveEmpty(AdaptiveDrawerBlockEntity target, int region) {
        if (target.hasReservation(region) && target.reservation(region).isEmpty()) return true;
        if (level == null) return false;
        List<ItemStack> reservedIdentities = new ArrayList<>();
        int totalCells = 0;
        int reservedCells = 0;
        for (BlockPos pos : drawerPositions()) {
            if (!(level.getBlockEntity(pos) instanceof AdaptiveDrawerBlockEntity drawer)) continue;
            int regions = drawer.getBlockState().getValue(AdaptiveDrawerBlock.REGIONS);
            totalCells += regions;
            reservedCells += drawer.reservationCountWithin(regions);
            reservedIdentities.addAll(drawer.reservedIdentitiesWithin(regions));
        }
        long unreservedLive = ledger.stream().filter(entry -> entry.identity.getMaxStackSize() > 1)
                .filter(entry -> reservedIdentities.stream().noneMatch(identity -> ItemStack.isSameItemSameTags(identity, entry.identity))).count();
        if (reservedCells + unreservedLive < totalCells) return true;

        AdaptiveDrawerBlockEntity expansionTarget = drawerPositions().stream()
                .map(level::getBlockEntity)
                .filter(AdaptiveDrawerBlockEntity.class::isInstance)
                .map(AdaptiveDrawerBlockEntity.class::cast)
                .filter(drawer -> drawer.pinnedRegions() == 0)
                .filter(drawer -> drawer.getBlockState().getValue(AdaptiveDrawerBlock.REGIONS) < 4)
                .sorted(Comparator
                        .comparing((AdaptiveDrawerBlockEntity drawer) -> drawer == target)
                        .thenComparingLong(drawer -> drawer.displayedStacks().stream().filter(stack -> !stack.isEmpty()).count()))
                .findFirst().orElse(null);
        if (expansionTarget == null) return false;
        BlockState state = expansionTarget.getBlockState();
        int current = state.getValue(AdaptiveDrawerBlock.REGIONS);
        int expanded = current == 1 ? 2 : 4;
        level.setBlock(expansionTarget.getBlockPos(), state.setValue(AdaptiveDrawerBlock.REGIONS, expanded), 3);
        cachedPresentation = null;
        return true;
    }
    private void add(ItemStack identity, int amount) {
        BulkEntry entry = find(identity);
        if (entry == null) ledger.add(new BulkEntry(com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(identity, 1), amount)); else entry.amount += amount;
        contentsChanged();
    }
    private BulkEntry find(ItemStack identity) { return ledger.stream().filter(entry -> ItemStack.isSameItemSameTags(entry.identity, identity)).findFirst().orElse(null); }
    public long count(ItemStack identity) { BulkEntry entry = find(identity); return entry == null ? 0 : entry.amount; }
    public ItemStack extract(ItemStack identity, int requested) {
        if (!powered) return ItemStack.EMPTY;
        BulkEntry entry = find(identity);
        if (entry == null || requested <= 0) return ItemStack.EMPTY;
        int removed = (int) Math.min(requested, entry.amount);
        entry.amount -= removed;
        ItemStack result = com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(entry.identity, removed);
        if (entry.amount == 0) ledger.remove(entry);
        contentsChanged();
        return result;
    }

    public List<DisplayAssignment> gridEntries() {
        if (!powered) return List.of();
        return sortedEntries().stream().map(entry -> new DisplayAssignment(com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(entry.identity, 1), entry.amount)).toList();
    }

    private List<IItemHandler> networkHandlers() {
        if (level != null && getControllerPos() != null
                && level.getBlockEntity(getControllerPos()) instanceof StorageControllerTile<?> controller) {
            int linked = controller.getConnectedDrawers().getConnectedDrawers().size();
            if (!getControllerPos().equals(cachedFsControllerPos) || linked != cachedFsLinkCount) {
                controller.getConnectedDrawers().rebuild();
                controller.inventoryHandler.invalidateSlots();
                cachedFsControllerPos = getControllerPos().immutable();
                cachedFsLinkCount = controller.getConnectedDrawers().getConnectedDrawers().size();
            }
            List<IItemHandler> handlers = controller.getConnectedDrawers().getConnectedDrawers().stream()
                    .map(BlockPos::of)
                    .map(level::getBlockEntity)
                    .filter(ItemControllableDrawerTile.class::isInstance)
                    .map(ItemControllableDrawerTile.class::cast)
                    .map(ItemControllableDrawerTile::getStorage)
                    .filter(handler -> !(handler instanceof com.buuz135.functionalstorage.inventory.ControllerInventoryHandler))
                    .toList();
            if (handlers.isEmpty() && cachedFsLinkCount > 0) {
                controller.getConnectedDrawers().rebuild();
                controller.inventoryHandler.invalidateSlots();
                handlers = controller.getConnectedDrawers().getConnectedDrawers().stream()
                        .map(BlockPos::of)
                        .map(level::getBlockEntity)
                        .filter(ItemControllableDrawerTile.class::isInstance)
                        .map(ItemControllableDrawerTile.class::cast)
                        .map(ItemControllableDrawerTile::getStorage)
                        .filter(handler -> !(handler instanceof com.buuz135.functionalstorage.inventory.ControllerInventoryHandler))
                        .toList();
            }
            if (!handlers.isEmpty()) return handlers;
        }
        return List.of(matrixStorage);
    }

    public List<DisplayAssignment> networkGridEntries() {
        if (!powered) return List.of();
        List<IItemHandler> handlers = networkHandlers();
        if (handlers.size() == 1 && handlers.get(0) == matrixStorage) return gridEntries();
        List<BulkEntry> entries = new ArrayList<>();
        for (IItemHandler storage : handlers) {
            for (int slot = 0; slot < storage.getSlots(); slot++) {
                ItemStack stack = storage.getStackInSlot(slot);
                if (stack.isEmpty()) continue;
                BulkEntry match = entries.stream()
                        .filter(entry -> ItemStack.isSameItemSameTags(entry.identity, stack)).findFirst().orElse(null);
                if (match == null) entries.add(new BulkEntry(com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(stack, 1), stack.getCount()));
                else match.amount = Math.min(Integer.MAX_VALUE, match.amount + (long) stack.getCount());
            }
        }
        sortEntries(entries);
        return entries.stream().map(entry -> new DisplayAssignment(com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(entry.identity, 1), entry.amount)).toList();
    }

    public ItemStack insertNetwork(ItemStack stack) {
        return insertNetwork(stack, false);
    }

    public ItemStack insertNetwork(ItemStack stack, boolean simulate) {
        if (!powered || stack.isEmpty()) return stack;
        ItemStack remainder = stack.copy();
        List<IItemHandler> handlers = networkHandlers();
        for (IItemHandler storage : handlers) {
            for (int slot = 0; slot < storage.getSlots() && !remainder.isEmpty(); slot++) {
                ItemStack existing = storage.getStackInSlot(slot);
                if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, remainder))
                    remainder = storage.insertItem(slot, remainder, simulate);
            }
        }
        for (IItemHandler storage : handlers) {
            for (int slot = 0; slot < storage.getSlots() && !remainder.isEmpty(); slot++) {
                if (storage.getStackInSlot(slot).isEmpty()) remainder = storage.insertItem(slot, remainder, simulate);
            }
        }
        return remainder;
    }

    public ItemStack extractNetwork(ItemStack identity, int requested) {
        if (!powered || identity.isEmpty() || requested <= 0) return ItemStack.EMPTY;
        ItemStack result = com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(identity, 0);
        int remaining = requested;
        for (IItemHandler storage : networkHandlers()) {
            for (int slot = 0; slot < storage.getSlots() && remaining > 0; slot++) {
                ItemStack existing = storage.getStackInSlot(slot);
                if (existing.isEmpty() || !ItemStack.isSameItemSameTags(existing, identity)) continue;
                ItemStack extracted = storage.extractItem(slot, remaining, false);
                if (!extracted.isEmpty()) {
                    result.grow(extracted.getCount());
                    remaining -= extracted.getCount();
                }
            }
            if (remaining <= 0) break;
        }
        return result;
    }

    public List<ItemStack> assignmentsFor(BlockPos drawerPos) { return displayAssignmentsFor(drawerPos).stream().map(entry -> com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(entry.identity, 1)).toList(); }
    public List<DisplayAssignment> displayAssignmentsFor(BlockPos drawerPos) {
        if (!powered) return List.of();
        PlanSnapshot snapshot = presentationSnapshot();
        int index = snapshot.drawers.indexOf(drawerPos);
        return index < 0 ? List.of() : snapshot.assignments.get(index);
    }
    private PlanSnapshot presentationSnapshot() {
        if (cachedPresentation == null) {
            List<BlockPos> drawers = drawerPositions();
            List<BulkEntry> sorted = sortedEntries().stream().filter(entry -> entry.identity.getMaxStackSize() > 1).toList();
            List<Integer> layouts = new ArrayList<>(layoutSizes(sorted, drawers));
            List<ItemStack> reservedIdentities = new ArrayList<>();
            for (int drawerIndex = 0; drawerIndex < drawers.size(); drawerIndex++) {
                if (!(level.getBlockEntity(drawers.get(drawerIndex)) instanceof AdaptiveDrawerBlockEntity drawer)) continue;
                int pinned = drawer.pinnedRegions();
                layouts.set(drawerIndex, pinned > 0 ? pinned : Math.min(4, Math.max(layouts.get(drawerIndex), drawer.minimumRegionsForReservations())));
                for (int region = 0; region < layouts.get(drawerIndex); region++) {
                    if (drawer.hasReservation(region) && !drawer.reservation(region).isEmpty()) reservedIdentities.add(drawer.reservation(region));
                }
            }
            List<BulkEntry> remaining = sorted.stream().filter(entry -> reservedIdentities.stream()
                    .noneMatch(identity -> ItemStack.isSameItemSameTags(identity, entry.identity))).toList();
            List<List<DisplayAssignment>> assignments = new ArrayList<>();
            int entryIndex = 0;
            for (int drawerIndex = 0; drawerIndex < drawers.size(); drawerIndex++) {
                int regions = layouts.get(drawerIndex);
                AdaptiveDrawerBlockEntity drawer = level.getBlockEntity(drawers.get(drawerIndex)) instanceof AdaptiveDrawerBlockEntity found ? found : null;
                List<DisplayAssignment> face = new ArrayList<>(regions);
                for (int region = 0; region < regions; region++) {
                    if (drawer != null && drawer.hasReservation(region)) {
                        ItemStack identity = drawer.reservation(region);
                        BulkEntry stored = identity.isEmpty() ? null : find(identity);
                        face.add(stored == null ? new DisplayAssignment(ItemStack.EMPTY, 0)
                                : new DisplayAssignment(com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(stored.identity, 1), stored.amount));
                    } else if (entryIndex < remaining.size()) {
                        BulkEntry entry = remaining.get(entryIndex++);
                        face.add(new DisplayAssignment(com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(entry.identity, 1), entry.amount));
                    } else face.add(new DisplayAssignment(ItemStack.EMPTY, 0));
                }
                assignments.add(List.copyOf(face));
            }
            cachedPresentation = new PlanSnapshot(drawers, List.copyOf(assignments));
        }
        return cachedPresentation;
    }

    public boolean canRemoveDrawer(BlockPos drawerPos) {
        long remainingFaces = drawerPositions().stream().filter(pos -> !pos.equals(drawerPos)).count();
        return ledger.size() <= remainingFaces * 4;
    }
    private List<BlockPos> drawerPositions() {
        if (level == null) return List.of();
        if (cachedDrawers != null) return cachedDrawers;
        Comparator<BlockPos> positionOrder = switch (countPriority) {
            case UP -> Comparator.comparingInt((BlockPos p) -> p.getY()).reversed().thenComparingInt(p -> p.getX()).thenComparingInt(p -> p.getZ());
            case DOWN -> Comparator.comparingInt((BlockPos p) -> p.getY()).thenComparingInt(p -> p.getX()).thenComparingInt(p -> p.getZ());
            case NORTH -> Comparator.comparingInt((BlockPos p) -> p.getZ()).thenComparing(Comparator.comparingInt((BlockPos p) -> p.getY()).reversed()).thenComparingInt(p -> p.getX());
            case SOUTH -> Comparator.comparingInt((BlockPos p) -> p.getZ()).reversed().thenComparing(Comparator.comparingInt((BlockPos p) -> p.getY()).reversed()).thenComparingInt(p -> p.getX());
            case EAST -> Comparator.comparingInt((BlockPos p) -> p.getX()).reversed().thenComparing(Comparator.comparingInt((BlockPos p) -> p.getY()).reversed()).thenComparingInt(p -> p.getZ());
            case WEST -> Comparator.comparingInt((BlockPos p) -> p.getX()).thenComparing(Comparator.comparingInt((BlockPos p) -> p.getY()).reversed()).thenComparingInt(p -> p.getZ());
        };
        List<BlockPos> drawers = members.stream()
                .filter(pos -> level.getBlockEntity(pos) instanceof AdaptiveDrawerBlockEntity drawer && worldPosition.equals(drawer.controllerPos()))
                .sorted(positionOrder)
                .toList();
        cachedDrawers = List.copyOf(drawers);
        return cachedDrawers;
    }
    private List<BlockPos> armoryPositions() {
        if (level == null) return List.of();
        return BlockPos.betweenClosedStream(worldPosition.offset(-RANGE, -RANGE, -RANGE), worldPosition.offset(RANGE, RANGE, RANGE))
                .filter(pos -> level.getBlockEntity(pos) instanceof AdaptiveArmoryBlockEntity armory && worldPosition.equals(armory.controllerPos())).map(BlockPos::immutable).toList();
    }
    public void topologyChanged() {
        cachedDrawers = null;
        contentsChanged();
    }
    public void refreshDrawerLayouts() {
        cachedPresentation = null;
        if (level == null || level.isClientSide) return;
        List<BlockPos> drawers = drawerPositions();
        List<Integer> layouts = layoutSizes(sortedEntries().stream().filter(entry -> entry.identity.getMaxStackSize() > 1).toList(), drawers);
        for (int index = 0; index < drawers.size(); index++) {
            int regions = Math.max(1, layouts.get(index));
            BlockPos drawerPos = drawers.get(index);
            if (level.getBlockEntity(drawerPos) instanceof AdaptiveDrawerBlockEntity drawer) {
                int pinned = drawer.pinnedRegions();
                regions = pinned > 0 ? pinned : Math.min(4, Math.max(regions, drawer.minimumRegionsForReservations()));
            }
            BlockState state = level.getBlockState(drawerPos);
            if (state.hasProperty(AdaptiveDrawerBlock.REGIONS) && state.getValue(AdaptiveDrawerBlock.REGIONS) != regions)
                level.setBlock(drawerPos, state.setValue(AdaptiveDrawerBlock.REGIONS, regions), 3);
            else level.sendBlockUpdated(drawerPos, state, state, 3);
        }
    }
    private static List<Integer> layoutSizes(int identityCount, int drawerCount) {
        if (drawerCount == 0) return List.of();
        int represented = Math.min(identityCount, drawerCount * 4);
        if (represented <= drawerCount) {
            List<Integer> sizes = new ArrayList<>();
            for (int index = 0; index < drawerCount; index++) sizes.add(index < represented ? 1 : 0);
            return sizes;
        }

        int bestOnes = 0, bestTwos = 0, bestFours = drawerCount;
        int bestCapacity = drawerCount * 4;
        double average = represented / (double) drawerCount;
        double bestSpread = Double.POSITIVE_INFINITY;
        for (int ones = 0; ones <= drawerCount; ones++) {
            for (int twos = 0; twos <= drawerCount - ones; twos++) {
                int fours = drawerCount - ones - twos;
                int capacity = ones + twos * 2 + fours * 4;
                if (capacity < represented) continue;
                double spread = ones * Math.pow(1 - average, 2)
                        + twos * Math.pow(2 - average, 2)
                        + fours * Math.pow(4 - average, 2);
                if (capacity < bestCapacity || capacity == bestCapacity && spread < bestSpread) {
                    bestCapacity = capacity;
                    bestSpread = spread;
                    bestOnes = ones;
                    bestTwos = twos;
                    bestFours = fours;
                }
            }
        }
        List<Integer> sizes = new ArrayList<>(drawerCount);
        for (int index = 0; index < bestOnes; index++) sizes.add(1);
        for (int index = 0; index < bestTwos; index++) sizes.add(2);
        for (int index = 0; index < bestFours; index++) sizes.add(4);
        return sizes;
    }

    private static List<Integer> minimumSplitLayout(int identityCount, int drawerCount) {
        if (drawerCount == 0) return List.of();
        int represented = Math.min(identityCount, drawerCount * 4);
        if (represented <= drawerCount) return layoutSizes(represented, drawerCount);
        int extraCells = represented - drawerCount;
        int fours = extraCells / 3;
        int remainder = extraCells % 3;
        int ones = drawerCount - fours - (remainder == 0 ? 0 : 1);
        List<Integer> sizes = new ArrayList<>(drawerCount);
        for (int index = 0; index < ones; index++) sizes.add(1);
        if (remainder != 0) sizes.add(remainder + 1);
        for (int index = 0; index < fours; index++) sizes.add(4);
        return sizes;
    }

    private List<Integer> layoutSizes(List<BulkEntry> sorted, List<BlockPos> drawers) {
        int drawerCount = drawers.size();
        if (layoutMode == LayoutMode.MANUAL) {
            return drawers.stream().map(pos -> {
                BlockState state = level.getBlockState(pos);
                if (level.getBlockEntity(pos) instanceof AdaptiveDrawerBlockEntity drawer && drawer.pinnedRegions() > 0) return drawer.pinnedRegions();
                return state.hasProperty(AdaptiveDrawerBlock.REGIONS) ? state.getValue(AdaptiveDrawerBlock.REGIONS) : 1;
            }).toList();
        }
        if (layoutMode == LayoutMode.BALANCED || sortMode != SortMode.COUNT || sorted.isEmpty() || drawerCount == 0)
            return layoutSizes(sorted.size(), drawerCount);

        long largest = sorted.get(0).amount;
        int high = 0;
        int middle = 0;
        int low = 0;
        for (BulkEntry entry : sorted) {
            // Single-stack entries use the densest face.
            if (entry.amount <= Math.max(1, entry.identity.getMaxStackSize())) low++;
            else if (entry.amount * 4 >= largest) high++;
            else if (entry.amount * 20 >= largest) middle++;
            else low++;
        }

        // Promote remainders so divided faces stay full.
        int ones = high + (middle & 1) + (low & 1);
        int twos = middle / 2 + (low % 4) / 2;
        int fours = low / 4;
        int wantedDrawers = ones + twos + fours;
        if (wantedDrawers > drawerCount) return layoutSizes(sorted.size(), drawerCount);

        // Expand dense groups until every usable drawer has a face.
        int targetDrawers = Math.min(drawerCount, sorted.size());
        while (wantedDrawers < targetDrawers) {
            if (fours > 0) {
                fours--;
                twos += 2;
            } else if (twos > 0) {
                twos--;
                ones += 2;
            } else break;
            wantedDrawers++;
        }

        List<Integer> sizes = new ArrayList<>(drawerCount);
        for (int index = 0; index < ones; index++) sizes.add(1);
        for (int index = 0; index < twos; index++) sizes.add(2);
        for (int index = 0; index < fours; index++) sizes.add(4);
        while (sizes.size() < drawerCount) sizes.add(0);
        return sizes;
    }

    private List<BulkEntry> sortedEntries() {
        List<BulkEntry> sorted = new ArrayList<>(ledger);
        sortEntries(sorted);
        return sorted;
    }
    private void sortEntries(List<BulkEntry> sorted) {
        Comparator<BulkEntry> itemId = Comparator.comparing(entry -> BuiltInRegistries.ITEM.getKey(entry.identity.getItem()).toString());
        Comparator<BulkEntry> comparator = switch (sortMode) {
            case COUNT -> Comparator.comparingLong((BulkEntry entry) -> entry.amount).reversed().thenComparing(itemId);
            case MOD -> Comparator.comparing((BulkEntry entry) -> BuiltInRegistries.ITEM.getKey(entry.identity.getItem()).getNamespace()).thenComparing(itemId);
            case ITEM_ID -> itemId;
            case TAG -> Comparator.comparing(this::primaryTag).thenComparing(itemId);
            case CATEGORY -> Comparator.comparing(this::category).thenComparing(itemId);
        };
        sorted.sort(comparator);
    }
    private String primaryTag(BulkEntry entry) { return entry.identity.getItem().builtInRegistryHolder().tags().map(TagKey::location).map(Object::toString).sorted().findFirst().orElse("~untagged"); }
    private String category(BulkEntry entry) {
        if (entry.identity.getItem() instanceof BlockItem) return "block";
        if (entry.identity.getItem().isEdible()) return "food";
        if (entry.identity.isDamageableItem()) return "equipment";
        return "misc";
    }
    private void contentsChanged() {
        setChanged(); cachedPresentation = null;
        if (level != null && !level.isClientSide) { level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); refreshDrawerLayouts(); }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag entries = new ListTag();
        for (BulkEntry entry : ledger) {
            CompoundTag stored = new CompoundTag(); stored.put("identity", com.silentvector.adaptivefunctionalstorage.util.ItemStacks.save(entry.identity)); stored.putLong("amount", entry.amount); entries.add(stored);
        }
        tag.put("bulk_ledger", entries); tag.putString("sort_mode", sortMode.name());
        tag.putString("layout_mode", layoutMode.name()); tag.putString("count_priority", countPriority.name());
        // Names support old saves; ordinals are used by current saves.
        tag.putInt("sort_mode_id", sortMode.ordinal());
        tag.putInt("layout_mode_id", layoutMode.ordinal());
        tag.putInt("count_priority_id", countPriority.ordinal());
        tag.putLongArray("members", members.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putInt("energy", energy.getEnergyStored());
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); ledger.clear(); members.clear();
        if (tag.contains("bulk_ledger")) {
            ListTag entries = tag.getList("bulk_ledger", 10);
            for (int index = 0; index < entries.size(); index++) {
                CompoundTag stored = entries.getCompound(index);
                ItemStack identity = com.silentvector.adaptivefunctionalstorage.util.ItemStacks.load(stored.getCompound("identity"));
                long amount = Math.min(MAX_PER_IDENTITY, Math.max(0, stored.getLong("amount")));
                if (!identity.isEmpty() && amount > 0) ledger.add(new BulkEntry(com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(identity, 1), amount));
            }
        } else if (tag.contains("ledger")) {
            SimpleContainer legacy = new SimpleContainer(4096); legacy.fromTag(tag.getList("ledger", 10));
            for (int slot = 0; slot < legacy.getContainerSize(); slot++) {
                ItemStack stack = legacy.getItem(slot); if (stack.isEmpty()) continue;
                BulkEntry entry = find(stack); if (entry == null) ledger.add(new BulkEntry(com.silentvector.adaptivefunctionalstorage.util.ItemStacks.copyWithCount(stack, 1), stack.getCount())); else entry.amount = Math.min(MAX_PER_IDENTITY, entry.amount + stack.getCount());
            }
        }
        if (tag.contains("sort_mode_id")) sortMode = enumByOrdinal(SortMode.values(), tag.getInt("sort_mode_id"), SortMode.COUNT);
        else try { sortMode = SortMode.valueOf(tag.getString("sort_mode")); } catch (IllegalArgumentException ignored) { sortMode = SortMode.COUNT; }
        if (tag.contains("layout_mode_id")) layoutMode = enumByOrdinal(LayoutMode.values(), tag.getInt("layout_mode_id"), LayoutMode.BALANCED);
        else try {
            String savedLayout = tag.getString("layout_mode");
            layoutMode = "MINIMUM_SPLITS".equals(savedLayout) ? LayoutMode.BALANCED : LayoutMode.valueOf(savedLayout);
        } catch (IllegalArgumentException ignored) { layoutMode = LayoutMode.BALANCED; }
        if (tag.contains("count_priority_id")) countPriority = enumByOrdinal(CountPriority.values(), tag.getInt("count_priority_id"), CountPriority.UP);
        else try { countPriority = CountPriority.valueOf(tag.getString("count_priority")); } catch (IllegalArgumentException ignored) { countPriority = CountPriority.UP; }
        for (long packed : tag.getLongArray("members")) members.add(BlockPos.of(packed));
        energy.setEnergy(tag.getInt("energy")); powered = energy.getEnergyStored() >= 20;
        cachedPresentation = null;
        cachedDrawers = null;
    }
    @Override public CompoundTag getUpdateTag() { CompoundTag tag = new CompoundTag(); saveAdditional(tag); return tag; }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) { if (packet.getTag() != null) load(packet.getTag()); }

    private static <T> T enumByOrdinal(T[] values, int ordinal, T fallback) {
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : fallback;
    }

    public enum SortMode {
        COUNT("Count"), MOD("Mod"), ITEM_ID("Item ID"), TAG("Tag"), CATEGORY("Category");
        private final String label; SortMode(String label) { this.label = label; } public String label() { return label; }
        private SortMode next() { return values()[(ordinal() + 1) % values().length]; }
    }
    public enum LayoutMode {
        BALANCED("Balanced"), PROPORTIONAL("Proportional"), MANUAL("Manual");
        private final String label; LayoutMode(String label) { this.label = label; } public String label() { return label; }
        private LayoutMode next() { return values()[(ordinal() + 1) % values().length]; }
    }
    public enum CountPriority {
        UP("Up"), DOWN("Down"), NORTH("North"), SOUTH("South"), EAST("East"), WEST("West");
        private final String label; CountPriority(String label) { this.label = label; } public String label() { return label; }
        private CountPriority next() { return values()[(ordinal() + 1) % values().length]; }
    }
    private static final class BulkEntry { private final ItemStack identity; private long amount; private BulkEntry(ItemStack identity, long amount) { this.identity = identity; this.amount = amount; } }
    public record DisplayAssignment(ItemStack identity, long amount) { }
    private static final class ControllerEnergyStorage extends EnergyStorage {
        private ControllerEnergyStorage(int capacity, int maxReceive) { super(capacity, maxReceive, 0); }
        private void setEnergy(int amount) { energy = Math.max(0, Math.min(capacity, amount)); }
    }
    private record PlanSnapshot(List<BlockPos> drawers, List<List<DisplayAssignment>> assignments) { }
}
