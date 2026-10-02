package com.silentvector.adaptivefunctionalstorage.registry;

import com.silentvector.adaptivefunctionalstorage.AdaptiveFunctionalStorage;
import com.silentvector.adaptivefunctionalstorage.menu.AdaptiveUpgradeMenu;
import com.silentvector.adaptivefunctionalstorage.menu.AdaptiveGridMenu;
import com.silentvector.adaptivefunctionalstorage.menu.AdaptiveCraftingGridMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> TYPES = DeferredRegister.create(Registries.MENU, AdaptiveFunctionalStorage.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<AdaptiveUpgradeMenu>> ADAPTIVE_UPGRADES =
            TYPES.register("adaptive_upgrades", () -> IMenuTypeExtension.create((id, inventory, data) -> new AdaptiveUpgradeMenu(id, inventory, data.readBlockPos())));
    public static final DeferredHolder<MenuType<?>, MenuType<AdaptiveGridMenu>> ADAPTIVE_GRID =
            TYPES.register("adaptive_grid", () -> IMenuTypeExtension.create((id, inventory, data) -> new AdaptiveGridMenu(id, inventory, data.readBlockPos())));
    public static final DeferredHolder<MenuType<?>, MenuType<AdaptiveCraftingGridMenu>> ADAPTIVE_CRAFTING_GRID =
            TYPES.register("adaptive_crafting_grid", () -> IMenuTypeExtension.create((id, inventory, data) -> new AdaptiveCraftingGridMenu(id, inventory, data.readBlockPos())));
    private ModMenus() { }
}
