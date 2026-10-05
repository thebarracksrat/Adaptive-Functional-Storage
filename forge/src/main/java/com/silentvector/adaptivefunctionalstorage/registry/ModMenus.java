package com.silentvector.adaptivefunctionalstorage.registry;

import com.silentvector.adaptivefunctionalstorage.AdaptiveFunctionalStorage;
import com.silentvector.adaptivefunctionalstorage.menu.AdaptiveCraftingGridMenu;
import com.silentvector.adaptivefunctionalstorage.menu.AdaptiveGridMenu;
import com.silentvector.adaptivefunctionalstorage.menu.AdaptiveUpgradeMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, AdaptiveFunctionalStorage.MOD_ID);
    public static final RegistryObject<MenuType<AdaptiveUpgradeMenu>> ADAPTIVE_UPGRADES =
            TYPES.register("adaptive_upgrades", () -> IForgeMenuType.create(
                    (id, inventory, data) -> new AdaptiveUpgradeMenu(id, inventory, data.readBlockPos())));
    public static final RegistryObject<MenuType<AdaptiveGridMenu>> ADAPTIVE_GRID =
            TYPES.register("adaptive_grid", () -> IForgeMenuType.create(
                    (id, inventory, data) -> new AdaptiveGridMenu(id, inventory, data.readBlockPos())));
    public static final RegistryObject<MenuType<AdaptiveCraftingGridMenu>> ADAPTIVE_CRAFTING_GRID =
            TYPES.register("adaptive_crafting_grid", () -> IForgeMenuType.create(
                    (id, inventory, data) -> new AdaptiveCraftingGridMenu(id, inventory, data.readBlockPos())));

    private ModMenus() {
    }
}
