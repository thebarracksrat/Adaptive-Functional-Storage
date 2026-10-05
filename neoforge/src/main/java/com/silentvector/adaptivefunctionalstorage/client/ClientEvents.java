package com.silentvector.adaptivefunctionalstorage.client;

import com.silentvector.adaptivefunctionalstorage.AdaptiveFunctionalStorage;
import com.silentvector.adaptivefunctionalstorage.registry.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.silentvector.adaptivefunctionalstorage.registry.ModMenus;

@EventBusSubscriber(modid = AdaptiveFunctionalStorage.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ADAPTIVE_UPGRADES.get(), AdaptiveUpgradeScreen::new);
        event.register(ModMenus.ADAPTIVE_GRID.get(), AdaptiveStorageGridScreen::new);
        event.register(ModMenus.ADAPTIVE_CRAFTING_GRID.get(), AdaptiveCraftingGridScreen::new);
    }
    private ClientEvents() { }
}
