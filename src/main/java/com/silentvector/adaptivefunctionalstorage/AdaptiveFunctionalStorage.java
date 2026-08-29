package com.silentvector.adaptivefunctionalstorage;

import com.silentvector.adaptivefunctionalstorage.registry.ModBlockEntities;
import com.silentvector.adaptivefunctionalstorage.registry.ModBlocks;
import com.silentvector.adaptivefunctionalstorage.registry.ModItems;
import com.silentvector.adaptivefunctionalstorage.registry.ModMenus;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import com.silentvector.adaptivefunctionalstorage.event.InteractionEvents;
import net.neoforged.bus.api.EventPriority;

import java.util.function.Supplier;

@Mod(AdaptiveFunctionalStorage.MOD_ID)
public final class AdaptiveFunctionalStorage {
    public static final String MOD_ID = "adaptive_functional_storage";
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    private static final Supplier<CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
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

    public AdaptiveFunctionalStorage(IEventBus modBus) {
        ModItems.ITEMS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.TYPES.register(modBus);
        ModMenus.TYPES.register(modBus);
        TABS.register(modBus);
        modBus.addListener((RegisterCapabilitiesEvent event) -> event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ADAPTIVE_CONTROLLER.get(),
                (controller, side) -> controller.automationHandler()));
        modBus.addListener((RegisterCapabilitiesEvent event) -> event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ADAPTIVE_DEPOSIT.get(),
                (deposit, side) -> deposit.itemHandler()));
        modBus.addListener((RegisterCapabilitiesEvent event) -> event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ADAPTIVE_CONTROLLER.get(),
                (controller, side) -> controller.energyStorage()));
        NeoForge.EVENT_BUS.addListener(InteractionEvents::onLeftClick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, true, InteractionEvents::onRightClick);
    }
}
