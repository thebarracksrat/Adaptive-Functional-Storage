package com.silentvector.adaptivefunctionalstorage.compat.emi;

import com.silentvector.adaptivefunctionalstorage.menu.AdaptiveCraftingGridMenu;
import com.silentvector.adaptivefunctionalstorage.registry.ModMenus;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import net.minecraft.world.inventory.Slot;

import java.util.List;

@EmiEntrypoint
public final class AdaptiveEmiPlugin implements EmiPlugin {
    @Override public void register(EmiRegistry registry) {
        registry.addRecipeHandler(ModMenus.ADAPTIVE_CRAFTING_GRID.get(), new StandardRecipeHandler<>() {
            @Override public List<Slot> getInputSources(AdaptiveCraftingGridMenu menu) { return menu.networkAndPlayerInputSlots(); }
            @Override public List<Slot> getCraftingSlots(AdaptiveCraftingGridMenu menu) { return menu.emiCraftingSlots(); }
            @Override public Slot getOutputSlot(AdaptiveCraftingGridMenu menu) { return menu.emiOutputSlot(); }
            @Override public boolean supportsRecipe(EmiRecipe recipe) { return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING; }
        });
    }
}
