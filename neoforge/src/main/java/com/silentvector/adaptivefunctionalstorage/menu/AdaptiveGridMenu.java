package com.silentvector.adaptivefunctionalstorage.menu;

import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveControllerBlockEntity;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveGridBlockEntity;
import com.silentvector.adaptivefunctionalstorage.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AdaptiveGridMenu extends AbstractContainerMenu {
    public static final int MAX_SYNCED_RESOURCES = 256;
    public static final int EXTRACT_HALF_OFFSET = 256;
    public static final int EXTRACT_TO_INVENTORY_OFFSET = 512;
    public static final int INSERT_ALL = 768;
    public static final int INSERT_ONE = 769;

    private final AdaptiveGridBlockEntity grid;
    private final SimpleContainer resources = new SimpleContainer(MAX_SYNCED_RESOURCES);
    private final ContainerData amounts;
    private final Inventory playerInventory;
    private int networkSyncDelay;

    public AdaptiveGridMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, inventory.player.level().getBlockEntity(pos) instanceof AdaptiveGridBlockEntity found ? found : null);
    }

    public AdaptiveGridMenu(int id, Inventory inventory, AdaptiveGridBlockEntity grid) {
        this(ModMenus.ADAPTIVE_GRID.get(), id, inventory, grid, 90);
    }

    protected AdaptiveGridMenu(net.minecraft.world.inventory.MenuType<?> type, int id, Inventory inventory, AdaptiveGridBlockEntity grid, int playerInventoryY) {
        super(type, id);
        this.grid = grid;
        this.playerInventory = inventory;
        this.amounts = new SimpleContainerData(MAX_SYNCED_RESOURCES);
        for (int i = 0; i < MAX_SYNCED_RESOURCES; i++) {
            final int resourceIndex = i;
            addSlot(new Slot(resources, i, -1000, -1000) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) { return true; }
            @Override public ItemStack remove(int amount) {
                AdaptiveControllerBlockEntity controller = controller();
                if (controller == null) return super.remove(amount);
                ItemStack extracted = controller.extractNetwork(getItem(), amount);
                if (!extracted.isEmpty()) {
                    ItemStack visible = resources.getItem(resourceIndex);
                    visible.shrink(extracted.getCount());
                    resources.setItem(resourceIndex, visible);
                    amounts.set(resourceIndex, Math.max(0, amounts.get(resourceIndex) - extracted.getCount()));
                }
                return extracted;
            }
            @Override public void onTake(Player player, ItemStack taken) {
                AdaptiveControllerBlockEntity controller = controller();
                if (controller != null && !taken.isEmpty()) {
                    ItemStack visible = resources.getItem(resourceIndex);
                    int expectedVisible = Math.min(taken.getMaxStackSize(), amounts.get(resourceIndex));
                    int currentVisible = ItemStack.isSameItemSameComponents(visible, taken) ? visible.getCount() : 0;
                    int externallyRemoved = Math.max(0, expectedVisible - currentVisible);
                    if (externallyRemoved > 0) {
                        ItemStack extracted = controller.extractNetwork(taken, externallyRemoved);
                        amounts.set(resourceIndex, Math.max(0, amounts.get(resourceIndex) - extracted.getCount()));
                        networkSyncDelay = 2;
                    }
                }
                super.onTake(player, taken);
            }
            });
        }
        addDataSlots(amounts);
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, playerInventoryY + row * 18));
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, playerInventoryY + 58));
        refreshSnapshot();
    }

    public ItemStack resource(int index) { return index >= 0 && index < MAX_SYNCED_RESOURCES ? resources.getItem(index) : ItemStack.EMPTY; }
    public int amount(int index) { return index >= 0 && index < MAX_SYNCED_RESOURCES ? amounts.get(index) : 0; }
    public java.util.List<Slot> networkAndPlayerInputSlots() { return java.util.List.copyOf(slots.subList(0, MAX_SYNCED_RESOURCES + 36)); }
    public boolean online() { return controller() != null && controller().isPowered(); }

    protected AdaptiveControllerBlockEntity controller() { return grid == null ? null : grid.controller(); }

    private void refreshSnapshot() {
        AdaptiveControllerBlockEntity controller = controller();
        List<AdaptiveControllerBlockEntity.DisplayAssignment> entries = controller == null ? List.of() : controller.networkGridEntries();
        for (int i = 0; i < MAX_SYNCED_RESOURCES; i++) {
            if (i < entries.size()) {
                ItemStack identity = entries.get(i).identity();
                int craftingVisibleAmount = (int) Math.min(identity.getMaxStackSize(), entries.get(i).amount());
                resources.setItem(i, identity.copyWithCount(craftingVisibleAmount));
                amounts.set(i, (int) Math.min(Integer.MAX_VALUE, entries.get(i).amount()));
            } else {
                resources.setItem(i, ItemStack.EMPTY);
                amounts.set(i, 0);
            }
        }
    }

    @Override public void broadcastChanges() {
        refreshSnapshot();
        super.broadcastChanges();
        if (networkSyncDelay > 0 && --networkSyncDelay == 0) broadcastFullState();
    }

    @Override public boolean clickMenuButton(Player player, int id) {
        AdaptiveControllerBlockEntity controller = controller();
        if (controller == null) return false;
        if (id == INSERT_ALL || id == INSERT_ONE) {
            ItemStack carried = getCarried();
            if (carried.isEmpty()) return false;
            int wanted = id == INSERT_ONE ? 1 : carried.getCount();
            ItemStack offered = carried.copyWithCount(wanted);
            ItemStack remainder = controller.insertNetwork(offered);
            carried.shrink(wanted - remainder.getCount());
            setCarried(carried);
            return true;
        }
        int index = id % EXTRACT_HALF_OFFSET;
        if (index < 0 || index >= MAX_SYNCED_RESOURCES) return false;
        ItemStack identity = resource(index);
        if (identity.isEmpty()) return false;
        int available = amount(index);
        int requested = id >= EXTRACT_HALF_OFFSET && id < EXTRACT_TO_INVENTORY_OFFSET
                ? Math.max(1, (available + 1) / 2) : identity.getMaxStackSize();
        requested = Math.min(requested, identity.getMaxStackSize());
        if (id >= EXTRACT_TO_INVENTORY_OFFSET) {
            ItemStack extracted = controller.extractNetwork(identity, requested);
            if (!player.getInventory().add(extracted)) player.drop(extracted, false);
            return true;
        }
        ItemStack carried = getCarried();
        if (!carried.isEmpty() && !ItemStack.isSameItemSameComponents(carried, identity)) return false;
        int room = carried.isEmpty() ? identity.getMaxStackSize() : carried.getMaxStackSize() - carried.getCount();
        ItemStack extracted = controller.extractNetwork(identity, Math.min(requested, room));
        if (carried.isEmpty()) setCarried(extracted); else carried.grow(extracted.getCount());
        return true;
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < MAX_SYNCED_RESOURCES) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem().copy();
        ItemStack remainder = controller() == null ? slot.getItem() : controller().insertNetwork(slot.getItem());
        slot.set(remainder);
        return remainder.getCount() == original.getCount() ? ItemStack.EMPTY : original;
    }

    @Override public boolean stillValid(Player player) {
        return grid != null && !grid.isRemoved() && player.distanceToSqr(grid.getBlockPos().getCenter()) <= 64.0;
    }
}
