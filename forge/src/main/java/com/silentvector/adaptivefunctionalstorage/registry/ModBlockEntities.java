package com.silentvector.adaptivefunctionalstorage.registry;

import com.silentvector.adaptivefunctionalstorage.AdaptiveFunctionalStorage;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveArmoryBlockEntity;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveControllerBlockEntity;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveCraftingGridBlockEntity;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveDepositBlockEntity;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveDrawerBlockEntity;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveExtenderBlockEntity;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveFluidDrawerBlockEntity;
import com.silentvector.adaptivefunctionalstorage.block.entity.AdaptiveGridBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AdaptiveFunctionalStorage.MOD_ID);

    public static final RegistryObject<BlockEntityType<AdaptiveControllerBlockEntity>> ADAPTIVE_CONTROLLER =
            TYPES.register("adaptive_controller", () -> BlockEntityType.Builder.of(AdaptiveControllerBlockEntity::new,
                    ModBlocks.ADAPTIVE_CONTROLLER.get()).build(null));
    public static final RegistryObject<BlockEntityType<AdaptiveDrawerBlockEntity>> ADAPTIVE_DRAWER =
            TYPES.register("adaptive_drawer", () -> BlockEntityType.Builder.of(AdaptiveDrawerBlockEntity::new,
                    ModBlocks.ADAPTIVE_DRAWER.get(), ModBlocks.ADAPTIVE_OAK_DRAWER.get(), ModBlocks.ADAPTIVE_BIRCH_DRAWER.get(),
                    ModBlocks.ADAPTIVE_JUNGLE_DRAWER.get(), ModBlocks.ADAPTIVE_ACACIA_DRAWER.get(), ModBlocks.ADAPTIVE_DARK_OAK_DRAWER.get(),
                    ModBlocks.ADAPTIVE_MANGROVE_DRAWER.get(), ModBlocks.ADAPTIVE_CHERRY_DRAWER.get(), ModBlocks.ADAPTIVE_CRIMSON_DRAWER.get(),
                    ModBlocks.ADAPTIVE_WARPED_DRAWER.get()).build(null));
    public static final RegistryObject<BlockEntityType<AdaptiveGridBlockEntity>> ADAPTIVE_GRID =
            TYPES.register("adaptive_grid", () -> BlockEntityType.Builder.of(AdaptiveGridBlockEntity::new,
                    ModBlocks.ADAPTIVE_GRID.get()).build(null));
    public static final RegistryObject<BlockEntityType<AdaptiveCraftingGridBlockEntity>> ADAPTIVE_CRAFTING_GRID =
            TYPES.register("adaptive_crafting_grid", () -> BlockEntityType.Builder.of(AdaptiveCraftingGridBlockEntity::new,
                    ModBlocks.ADAPTIVE_CRAFTING_GRID.get()).build(null));
    public static final RegistryObject<BlockEntityType<AdaptiveDepositBlockEntity>> ADAPTIVE_DEPOSIT =
            TYPES.register("adaptive_deposit", () -> BlockEntityType.Builder.of(AdaptiveDepositBlockEntity::new,
                    ModBlocks.ADAPTIVE_DEPOSIT.get()).build(null));
    public static final RegistryObject<BlockEntityType<AdaptiveExtenderBlockEntity>> ADAPTIVE_EXTENDER =
            TYPES.register("adaptive_extender", () -> BlockEntityType.Builder.of(AdaptiveExtenderBlockEntity::new,
                    ModBlocks.ADAPTIVE_EXTENDER.get()).build(null));
    public static final RegistryObject<BlockEntityType<AdaptiveArmoryBlockEntity>> ADAPTIVE_ARMORY =
            TYPES.register("adaptive_armory", () -> BlockEntityType.Builder.of(AdaptiveArmoryBlockEntity::new,
                    ModBlocks.ADAPTIVE_ARMORY.get()).build(null));
    public static final RegistryObject<BlockEntityType<AdaptiveFluidDrawerBlockEntity>> ADAPTIVE_FLUID_DRAWER =
            TYPES.register("adaptive_fluid_drawer", () -> BlockEntityType.Builder.of(AdaptiveFluidDrawerBlockEntity::new,
                    ModBlocks.ADAPTIVE_FLUID_DRAWER.get()).build(null));

    private ModBlockEntities() {
    }
}
