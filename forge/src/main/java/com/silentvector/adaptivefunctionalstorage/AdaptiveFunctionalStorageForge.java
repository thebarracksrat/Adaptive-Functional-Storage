package com.silentvector.adaptivefunctionalstorage;

import com.silentvector.adaptivefunctionalstorage.event.InteractionEvents;
import com.silentvector.adaptivefunctionalstorage.registry.ModBlockEntities;
import com.silentvector.adaptivefunctionalstorage.registry.ModBlocks;
import com.silentvector.adaptivefunctionalstorage.registry.ModItems;
import com.silentvector.adaptivefunctionalstorage.registry.ModMenus;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod(AdaptiveFunctionalStorage.MOD_ID)
public final class AdaptiveFunctionalStorageForge {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AdaptiveFunctionalStorage.MOD_ID);
    private static final RegistryObject<CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.adaptive_functional_storage"))
            .icon(() -> ModBlocks.ADAPTIVE_DRAWER_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModBlocks.ADAPTIVE_CONTROLLER_ITEM.get());
                output.accept(ModBlocks.ADAPTIVE_DRAWER_ITEM.get());
                output.accept(ModBlocks.ADAPTIVE_OAK_DRAWER_ITEM.get());
                output.accept(ModBlocks.ADAPTIVE_BIRCH_DRAWER_ITEM.get());
                output.accept(ModBlocks.ADAPTIVE_JUNGLE_DRAWER_ITEM.get());
                output.accept(ModBlocks.ADAPTIVE_ACACIA_DRAWER_ITEM.get());
                output.accept(ModBlocks.ADAPTIVE_DARK_OAK_DRAWER_ITEM.get());
                output.accept(ModBlocks.ADAPTIVE_MANGROVE_DRAWER_ITEM.get());
                output.accept(ModBlocks.ADAPTIVE_CHERRY_DRAWER_ITEM.get());
                output.accept(ModBlocks.ADAPTIVE_CRIMSON_DRAWER_ITEM.get());
                output.accept(ModBlocks.ADAPTIVE_WARPED_DRAWER_ITEM.get());
                output.accept(ModBlocks.ADAPTIVE_GRID_ITEM.get());
                output.accept(ModBlocks.ADAPTIVE_CRAFTING_GRID_ITEM.get());
                output.accept(ModBlocks.ADAPTIVE_DEPOSIT_ITEM.get());
                output.accept(ModItems.ADAPTIVE_CONFIGURATION_TOOL.get());
                output.accept(ModItems.ADAPTIVE_UPGRADE.get());
            }).build());

    public AdaptiveFunctionalStorageForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.TYPES.register(modBus);
        ModMenus.TYPES.register(modBus);
        TABS.register(modBus);
        MinecraftForge.EVENT_BUS.addListener(InteractionEvents::onLeftClick);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, true, InteractionEvents::onRightClick);
    }
}
