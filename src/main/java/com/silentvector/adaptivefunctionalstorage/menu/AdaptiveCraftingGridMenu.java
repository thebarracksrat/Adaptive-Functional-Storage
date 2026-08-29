package com.silentvector.adaptivefunctionalstorage.menu;

import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveCraftingGridBlockEntity;
import com.silentvector.adaptivefunctionalstorage.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public final class AdaptiveCraftingGridMenu extends AdaptiveGridMenu {
    private static final int CRAFT_START = MAX_SYNCED_RESOURCES + 36;
    private final CraftingContainer crafting = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer result = new ResultContainer();
    private final Player player;
    private final java.util.List<Slot> craftingSlots = new java.util.ArrayList<>();
    private boolean refilling;
    private int craftingSyncDelay;

    public AdaptiveCraftingGridMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, inventory.player.level().getBlockEntity(pos) instanceof AdaptiveCraftingGridBlockEntity found ? found : null);
    }

    public AdaptiveCraftingGridMenu(int id, Inventory inventory, AdaptiveCraftingGridBlockEntity grid) {
        super(ModMenus.ADAPTIVE_CRAFTING_GRID.get(), id, inventory, grid, 147);
        this.player = inventory.player;
        for (int row = 0; row < 3; row++) for (int column = 0; column < 3; column++)
            craftingSlots.add(addSlot(new Slot(crafting, column + row * 3, 26 + column * 18, 78 + row * 18)));
        addSlot(new ResultSlot(player, crafting, result, 0, 134, 96) {
            @Override public void onTake(Player player, ItemStack crafted) {
                ItemStack[] template = new ItemStack[crafting.getContainerSize()];
                for (int slot = 0; slot < template.length; slot++)
                    template[slot] = crafting.getItem(slot).copyWithCount(1);
                super.onTake(player, crafted);
                refillFromNetwork(template);
            }
        });
        slotsChanged(crafting);
    }

    private void refillFromNetwork(ItemStack[] template) {
        if (refilling || player.level().isClientSide || controller() == null) return;
        refilling = true;
        try {
            for (int slot = 0; slot < template.length; slot++) {
                if (template[slot].isEmpty() || !crafting.getItem(slot).isEmpty()) continue;
                ItemStack replacement = controller().extractNetwork(template[slot], 1);
                if (!replacement.isEmpty()) crafting.setItem(slot, replacement);
            }
            slotsChanged(crafting);
            // Container clicks suppress remote updates while they run. Waiting through
            // the click's own broadcasts ensures the forced state is sent afterward.
            craftingSyncDelay = 2;
        } finally {
            refilling = false;
        }
    }

    @Override public void broadcastChanges() {
        super.broadcastChanges();
        if (craftingSyncDelay > 0 && --craftingSyncDelay == 0) {
            broadcastFullState();
        }
    }

    public java.util.List<Slot> emiCraftingSlots() { return java.util.List.copyOf(craftingSlots); }
    public Slot emiOutputSlot() { return slots.get(CRAFT_START + 9); }

    @Override public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (!player.level().isClientSide) {
            var input = crafting.asCraftInput();
            var match = player.level().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, player.level());
            ItemStack output = match.map(holder -> {
                result.setRecipeUsed(holder);
                return holder.value().assemble(input, player.level().registryAccess());
            }).orElse(ItemStack.EMPTY);
            result.setItem(0, output);
            broadcastChanges();
        }
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < CRAFT_START) return super.quickMoveStack(player, index);
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem().copy();
        if (index == CRAFT_START + 9) {
            if (!moveItemStackTo(slot.getItem(), MAX_SYNCED_RESOURCES, MAX_SYNCED_RESOURCES + 36, true)) return ItemStack.EMPTY;
            slot.onTake(player, original);
            return original;
        }
        ItemStack remainder = controller() == null ? slot.getItem() : controller().insertNetwork(slot.getItem());
        slot.set(remainder);
        return remainder.getCount() == original.getCount() ? ItemStack.EMPTY : original;
    }

    @Override public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide) return;
        for (int i = 0; i < crafting.getContainerSize(); i++) {
            ItemStack stack = crafting.removeItemNoUpdate(i);
            if (stack.isEmpty()) continue;
            ItemStack remainder = controller() == null ? stack : controller().insertNetwork(stack);
            if (!remainder.isEmpty() && !player.getInventory().add(remainder)) player.drop(remainder, false);
        }
    }
}
