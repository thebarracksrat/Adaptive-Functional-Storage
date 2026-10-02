package com.silentvector.adaptivefunctionalstorage.client;

import com.silentvector.adaptivefunctionalstorage.AdaptiveFunctionalStorage;
import com.silentvector.adaptivefunctionalstorage.registry.ModBlockEntities;
import com.silentvector.adaptivefunctionalstorage.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = AdaptiveFunctionalStorage.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.ADAPTIVE_DRAWER.get(), AdaptiveDrawerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ADAPTIVE_CONTROLLER.get(), AdaptiveControllerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ADAPTIVE_FLUID_DRAWER.get(), AdaptiveFluidDrawerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ADAPTIVE_GRID.get(), AdaptiveGridRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ADAPTIVE_CRAFTING_GRID.get(), AdaptiveCraftingGridRenderer::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.ADAPTIVE_UPGRADES.get(), AdaptiveUpgradeScreen::new);
            MenuScreens.register(ModMenus.ADAPTIVE_GRID.get(), AdaptiveStorageGridScreen::new);
            MenuScreens.register(ModMenus.ADAPTIVE_CRAFTING_GRID.get(), AdaptiveCraftingGridScreen::new);
        });
    }

    private ClientEvents() {
    }
}
