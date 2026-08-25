package com.silentvector.adaptivefunctionalstorage.client;

import com.silentvector.adaptivefunctionalstorage.menu.AdaptiveGridMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class AdaptiveStorageGridScreen extends AdaptiveGridScreen<AdaptiveGridMenu> {
    public AdaptiveStorageGridScreen(AdaptiveGridMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
