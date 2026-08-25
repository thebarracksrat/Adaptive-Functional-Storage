package com.silentvector.adaptivefunctionalstorage.block.entity;

import com.silentvector.adaptivefunctionalstorage.menu.AdaptiveCraftingGridMenu;
import com.silentvector.adaptivefunctionalstorage.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public final class AdaptiveCraftingGridBlockEntity extends AdaptiveGridBlockEntity {
    public AdaptiveCraftingGridBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.ADAPTIVE_CRAFTING_GRID.get(), pos, state); }
    @Override public Component getDisplayName() { return Component.literal("Adaptive Crafting Grid"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new AdaptiveCraftingGridMenu(id, inventory, this); }
}
