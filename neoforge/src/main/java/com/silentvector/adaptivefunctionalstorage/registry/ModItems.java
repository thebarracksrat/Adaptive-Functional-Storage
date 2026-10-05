package com.silentvector.adaptivefunctionalstorage.registry;

import com.silentvector.adaptivefunctionalstorage.AdaptiveFunctionalStorage;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.minecraft.world.item.Item;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AdaptiveFunctionalStorage.MOD_ID);
    public static final DeferredItem<Item> ADAPTIVE_CONFIGURATION_TOOL = ITEMS.registerSimpleItem(
            "adaptive_configuration_tool", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> ADAPTIVE_UPGRADE = ITEMS.registerSimpleItem(
            "adaptive_upgrade", new Item.Properties());
    private ModItems() { }
}
