package com.silentvector.adaptivefunctionalstorage.event;

import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveControllerBlockEntity;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveDrawerBlockEntity;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveDepositBlockEntity;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveGridBlockEntity;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveExtenderBlockEntity;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveArmoryBlockEntity;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveFluidDrawerBlockEntity;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveDrawerBlock;
import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.item.FSAttachments;
import com.buuz135.functionalstorage.block.tile.StorageControllerTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import net.neoforged.neoforge.items.IItemHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import java.util.List;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import com.silentvector.adaptivefunctionalstorage.registry.ModItems;

public final class InteractionEvents {
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        ItemStack tool=event.getItemStack();
        var blockEntity=event.getLevel().getBlockEntity(event.getPos());
        if (blockEntity instanceof StorageControllerTile<?> fsController
                && event.getHand() == InteractionHand.MAIN_HAND
                && !tool.isEmpty()
                && !tool.is(FunctionalStorage.LINKING_TOOL.get())
                && !tool.is(FunctionalStorage.CONFIGURATION_TOOL.get())) {
            AdaptiveControllerBlockEntity matrix = fsController.getConnectedDrawers().getConnectedDrawers().stream()
                    .map(BlockPos::of).map(event.getLevel()::getBlockEntity)
                    .filter(AdaptiveControllerBlockEntity.class::isInstance)
                    .map(AdaptiveControllerBlockEntity.class::cast).findFirst().orElse(null);
            if (matrix != null) {
                event.setCanceled(true);
                if (!event.getLevel().isClientSide) event.getEntity().setItemInHand(event.getHand(),
                        routeIntoFunctionalNetwork(fsController, matrix, tool));
                return;
            }
        }
        if (tool.is(ModItems.ADAPTIVE_CONFIGURATION_TOOL.get())
                && (blockEntity instanceof AdaptiveControllerBlockEntity || blockEntity instanceof AdaptiveDrawerBlockEntity)) {
            event.setCanceled(true);
            if (event.getLevel().isClientSide) return;
            if (blockEntity instanceof AdaptiveControllerBlockEntity controller) {
                event.getEntity().displayClientMessage(Component.literal("Sort: " + controller.sortMode().label()
                        + " | Layout: " + controller.layoutMode().label() + " | Priority: " + controller.countPriority().label()), true);
            } else if (blockEntity instanceof AdaptiveDrawerBlockEntity drawer) {
                AdaptiveControllerBlockEntity controller = drawer.findController();
                if (controller == null || controller.layoutMode() != AdaptiveControllerBlockEntity.LayoutMode.MANUAL) {
                    event.getEntity().displayClientMessage(Component.literal("Select Manual layout on the Adaptive Controller before configuring drawer cells"), true);
                } else if (AdaptiveDrawerBlock.isBorder(drawer.getBlockState(), drawer.getBlockPos(), event.getHitVec())) {
                    if (event.getEntity().isShiftKeyDown() || event.getEntity().isCrouching()) {
                        drawer.clearPinnedRegions();
                        event.getEntity().displayClientMessage(Component.literal("Drawer cell count returned to automatic layout control"), true);
                    } else {
                        int current = drawer.getBlockState().getValue(AdaptiveDrawerBlock.REGIONS);
                        int next = current == 1 ? 2 : current == 2 ? 4 : 1;
                        if (controller.setManualRegions(drawer.getBlockPos(), next))
                            event.getEntity().displayClientMessage(Component.literal("Drawer layout pinned: " + (next == 1 ? "1x1" : next == 2 ? "2x1" : "2x2")), true);
                        else event.getEntity().displayClientMessage(Component.literal("Cannot shrink drawer: a removed cell has an item lock, or no unpinned drawer can expand for displaced items"), true);
                    }
                } else {
                    int regions = drawer.getBlockState().getValue(AdaptiveDrawerBlock.REGIONS);
                    int region = AdaptiveDrawerBlock.selectedRegion(drawer.getBlockState(), drawer.getBlockPos(), event.getHitVec(), regions);
                    List<ItemStack> displays = drawer.displayedStacks();
                    ItemStack current = region < displays.size() ? displays.get(region) : ItemStack.EMPTY;
                    boolean reserveEmpty = event.getEntity().isShiftKeyDown() || event.getEntity().isCrouching();
                    String result = drawer.configureRegion(region, current, event.getEntity().getOffhandItem(), reserveEmpty);
                    event.getEntity().displayClientMessage(Component.literal(result), true);
                }
            }
            return;
        }
        if (blockEntity instanceof AdaptiveControllerBlockEntity controller
                && event.getHand() == InteractionHand.MAIN_HAND
                && (tool.isEmpty() || tool.is(Items.GLASS) || tool.is(Items.COMPASS))) {
            boolean sneaking = event.getEntity().isShiftKeyDown();
            if ((!sneaking && tool.isEmpty()) || sneaking) {
                event.setCanceled(true);
                if (event.getLevel().isClientSide) return;
                if (!sneaking) {
                    event.getEntity().displayClientMessage(Component.literal("Sort: " + controller.sortMode().label()
                            + " | Layout: " + controller.layoutMode().label() + " | Priority: " + controller.countPriority().label()
                            + " | " + (controller.isPowered() ? "ONLINE" : "OFFLINE") + " | " + controller.energyDrawPerTick() + " FE/t | " + controller.energyStorage().getEnergyStored() + " FE"), true);
                } else if (tool.is(Items.GLASS)) {
                    event.getEntity().displayClientMessage(Component.literal("Adaptive layout: " + controller.cycleLayoutMode().label()), true);
                } else if (tool.is(Items.COMPASS)) {
                    event.getEntity().displayClientMessage(Component.literal("Count priority: " + controller.cycleCountPriority().label()), true);
                } else {
                    event.getEntity().displayClientMessage(Component.literal("Adaptive sort: " + controller.cycleSortMode().label()), true);
                }
                return;
            }
        }
        if(!tool.is(FunctionalStorage.LINKING_TOOL.get()))return;
        boolean adaptiveTarget=blockEntity instanceof AdaptiveDrawerBlockEntity || blockEntity instanceof AdaptiveGridBlockEntity
                || blockEntity instanceof AdaptiveDepositBlockEntity || blockEntity instanceof AdaptiveExtenderBlockEntity
                || blockEntity instanceof AdaptiveArmoryBlockEntity || blockEntity instanceof AdaptiveFluidDrawerBlockEntity;
        if(!adaptiveTarget)return;
        event.setCanceled(true);
        if(event.getLevel().isClientSide)return;
        if(!tool.has(FSAttachments.CONTROLLER)){
            event.getEntity().displayClientMessage(net.minecraft.network.chat.Component.literal("Configure the Functional Storage Linking Tool on an Adaptive Matrix first"),true);
            return;
        }
        BlockPos controllerPos=tool.get(FSAttachments.CONTROLLER);
        if(!(event.getLevel().getBlockEntity(controllerPos) instanceof AdaptiveControllerBlockEntity controller)){
            event.getEntity().displayClientMessage(net.minecraft.network.chat.Component.literal("Selected Adaptive Controller is unavailable"),true);
            return;
        }
        boolean linked=blockEntity instanceof AdaptiveDrawerBlockEntity drawer ? drawer.linkTo(controller)
                : blockEntity instanceof AdaptiveGridBlockEntity grid ? grid.linkTo(controller)
                : blockEntity instanceof AdaptiveDepositBlockEntity deposit ? deposit.linkTo(controller)
                : blockEntity instanceof AdaptiveExtenderBlockEntity extender ? extender.linkTo(controller)
                : blockEntity instanceof AdaptiveArmoryBlockEntity armory ? armory.linkTo(controller)
                : blockEntity instanceof AdaptiveFluidDrawerBlockEntity fluid && fluid.linkTo(controller);
        event.getEntity().displayClientMessage(net.minecraft.network.chat.Component.literal(linked ? "Block linked to Adaptive Controller" : "This block cannot be linked"),true);
    }

    private static ItemStack routeIntoFunctionalNetwork(StorageControllerTile<?> controller,
                                                         AdaptiveControllerBlockEntity matrix,
                                                         ItemStack offered) {
        ItemStack remainder = offered;
        List<IItemHandler> ordinary = controller.getConnectedDrawers().getConnectedDrawers().stream()
                .map(BlockPos::of).map(controller.getLevel()::getBlockEntity)
                .filter(ItemControllableDrawerTile.class::isInstance)
                .filter(entity -> !(entity instanceof AdaptiveControllerBlockEntity))
                .map(ItemControllableDrawerTile.class::cast).map(ItemControllableDrawerTile::getStorage).toList();
        for (IItemHandler handler : ordinary) {
            for (int slot = 0; slot < handler.getSlots() && !remainder.isEmpty(); slot++) {
                ItemStack existing = handler.getStackInSlot(slot);
                if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, remainder))
                    remainder = handler.insertItem(slot, remainder, false);
            }
        }
        if (!remainder.isEmpty()) remainder = matrix.insert(remainder);
        for (IItemHandler handler : ordinary) {
            for (int slot = 0; slot < handler.getSlots() && !remainder.isEmpty(); slot++) {
                if (handler.getStackInSlot(slot).isEmpty()) remainder = handler.insertItem(slot, remainder, false);
            }
        }
        return remainder;
    }

    public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof AdaptiveDrawerBlockEntity drawer)) return;
        HitResult picked = event.getEntity().pick(6, 0, false);
        if (!(picked instanceof BlockHitResult hit)) return;
        if (event.getEntity().getMainHandItem().is(ModItems.ADAPTIVE_CONFIGURATION_TOOL.get())) {
            event.setCanceled(true);
            if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START || event.getLevel().isClientSide) return;
            AdaptiveControllerBlockEntity controller = drawer.findController();
            if (controller == null || controller.layoutMode() != AdaptiveControllerBlockEntity.LayoutMode.MANUAL) {
                event.getEntity().displayClientMessage(Component.literal("Select Manual layout before editing drawer cells"), true);
            } else if (AdaptiveDrawerBlock.isBorder(drawer.getBlockState(), drawer.getBlockPos(), hit)) {
                event.getEntity().displayClientMessage(Component.literal("Use the Adaptive Configuration Tool on the border to change drawer shape"), true);
            } else if (event.getEntity().isShiftKeyDown() || event.getEntity().isCrouching()) {
                int regions = drawer.getBlockState().getValue(AdaptiveDrawerBlock.REGIONS);
                int region = AdaptiveDrawerBlock.selectedRegion(drawer.getBlockState(), drawer.getBlockPos(), hit, regions);
                List<ItemStack> displays = drawer.displayedStacks();
                ItemStack current = region < displays.size() ? displays.get(region) : ItemStack.EMPTY;
                event.getEntity().displayClientMessage(Component.literal(drawer.configureRegion(region, current, ItemStack.EMPTY, true)), true);
            } else event.getEntity().displayClientMessage(Component.literal("Right-click to lock; sneak-left-click to reserve this cell empty"), true);
            return;
        }
        List<ItemStack> displays = drawer.displayedStacks();
        if (AdaptiveDrawerBlock.isBorder(drawer.getBlockState(), drawer.getBlockPos(), hit)) return;
        if (drawer.findController() == null || displays.isEmpty()) return;
        event.setCanceled(true);
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START || event.getLevel().isClientSide) return;
        AdaptiveControllerBlockEntity controller = drawer.findController();
        int region = AdaptiveDrawerBlock.selectedRegion(drawer.getBlockState(), drawer.getBlockPos(), hit,
                drawer.getBlockState().getValue(AdaptiveDrawerBlock.REGIONS));
        ItemStack identity = region >= displays.size() ? ItemStack.EMPTY : displays.get(region);
        if (controller != null && !identity.isEmpty()) {
            int amount = event.getEntity().isCrouching() ? identity.getMaxStackSize() : 1;
            event.getEntity().getInventory().placeItemBackInInventory(controller.extract(identity, amount));
        }
    }
    private InteractionEvents() { }
}
