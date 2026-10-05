package com.silentvector.adaptivefunctionalstorage.menu;

import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.silentvector.adaptivefunctionalstorage.registry.ModBlocks;
import com.silentvector.adaptivefunctionalstorage.registry.ModItems;
import com.silentvector.adaptivefunctionalstorage.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntPredicate;

public final class AdaptiveUpgradeMenu extends AbstractContainerMenu {
    private final Container upgrades;
    private final boolean controllerMode;
    private final IntPredicate mayTake;

    public AdaptiveUpgradeMenu(int id, Inventory playerInventory, net.minecraft.core.BlockPos pos) {
        this(id, playerInventory, new SimpleContainer(4),
                playerInventory.player.level().getBlockState(pos).is(ModBlocks.ADAPTIVE_CONTROLLER.get()),
                ignored -> true);
    }

    public AdaptiveUpgradeMenu(int id, Inventory playerInventory, Container upgrades, boolean controllerMode, IntPredicate mayTake) {
        super(ModMenus.ADAPTIVE_UPGRADES.get(), id);
        this.upgrades = upgrades;
        this.controllerMode = controllerMode;
        this.mayTake = mayTake;
        checkContainerSize(upgrades, 4);
        for (int slot = 0; slot < 4; slot++) {
            addSlot(new Slot(upgrades, slot, 10 + slot * 18, 70) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return controllerMode ? isControllerUpgrade(stack) : isDrawerUpgrade(stack);
                }

                @Override
                public boolean mayPickup(Player player) {
                    return mayTake.test(getSlotIndex());
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 105 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 163));
        }
    }

    public static boolean isDrawerUpgrade(ItemStack stack) {
        return stack.getItem() instanceof StorageUpgradeItem;
    }

    public static boolean isControllerUpgrade(ItemStack stack) {
        return stack.is(ModItems.ADAPTIVE_UPGRADE.get());
    }

    public boolean controllerMode() {
        return controllerMode;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
