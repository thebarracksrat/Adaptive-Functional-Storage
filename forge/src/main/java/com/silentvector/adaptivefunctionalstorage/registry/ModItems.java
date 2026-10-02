package com.silentvector.adaptivefunctionalstorage.registry;

import com.silentvector.adaptivefunctionalstorage.AdaptiveFunctionalStorage;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AdaptiveFunctionalStorage.MOD_ID);
    public static final RegistryObject<Item> ADAPTIVE_CONFIGURATION_TOOL = ITEMS.register(
            "adaptive_configuration_tool", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ADAPTIVE_UPGRADE = ITEMS.register(
            "adaptive_upgrade", () -> new Item(new Item.Properties()));

    private ModItems() {
    }
}
