package com.silentvector.adaptivefunctionalstorage.registry;

import com.silentvector.adaptivefunctionalstorage.AdaptiveFunctionalStorage;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveArmoryBlock;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveControllerBlock;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveCraftingGridBlock;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveDepositBlock;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveDrawerBlock;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveExtenderBlock;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveFluidDrawerBlock;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveGridBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, AdaptiveFunctionalStorage.MOD_ID);

    public static final RegistryObject<AdaptiveControllerBlock> ADAPTIVE_CONTROLLER = BLOCKS.register(
            "adaptive_controller",
            () -> new AdaptiveControllerBlock(BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.WOOD)
                    .lightLevel(state -> state.getValue(AdaptiveControllerBlock.POWERED) ? 7 : 0)));
    public static final RegistryObject<AdaptiveDrawerBlock> ADAPTIVE_DRAWER = BLOCKS.register(
            "adaptive_drawer",
            () -> new AdaptiveDrawerBlock(BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.WOOD)
                    .lightLevel(state -> state.getValue(AdaptiveDrawerBlock.POWERED) ? 5 : 0)));
    public static final RegistryObject<AdaptiveDrawerBlock> ADAPTIVE_OAK_DRAWER = drawer("adaptive_oak_drawer");
    public static final RegistryObject<AdaptiveDrawerBlock> ADAPTIVE_BIRCH_DRAWER = drawer("adaptive_birch_drawer");
    public static final RegistryObject<AdaptiveDrawerBlock> ADAPTIVE_JUNGLE_DRAWER = drawer("adaptive_jungle_drawer");
    public static final RegistryObject<AdaptiveDrawerBlock> ADAPTIVE_ACACIA_DRAWER = drawer("adaptive_acacia_drawer");
    public static final RegistryObject<AdaptiveDrawerBlock> ADAPTIVE_DARK_OAK_DRAWER = drawer("adaptive_dark_oak_drawer");
    public static final RegistryObject<AdaptiveDrawerBlock> ADAPTIVE_MANGROVE_DRAWER = drawer("adaptive_mangrove_drawer");
    public static final RegistryObject<AdaptiveDrawerBlock> ADAPTIVE_CHERRY_DRAWER = drawer("adaptive_cherry_drawer");
    public static final RegistryObject<AdaptiveDrawerBlock> ADAPTIVE_CRIMSON_DRAWER = drawer("adaptive_crimson_drawer");
    public static final RegistryObject<AdaptiveDrawerBlock> ADAPTIVE_WARPED_DRAWER = drawer("adaptive_warped_drawer");
    public static final RegistryObject<AdaptiveGridBlock> ADAPTIVE_GRID = BLOCKS.register(
            "adaptive_grid",
            () -> new AdaptiveGridBlock(BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.METAL)));
    public static final RegistryObject<AdaptiveCraftingGridBlock> ADAPTIVE_CRAFTING_GRID = BLOCKS.register(
            "adaptive_crafting_grid",
            () -> new AdaptiveCraftingGridBlock(BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.METAL)));
    public static final RegistryObject<AdaptiveDepositBlock> ADAPTIVE_DEPOSIT = BLOCKS.register(
            "adaptive_deposit",
            () -> new AdaptiveDepositBlock(BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.METAL)));
    public static final RegistryObject<AdaptiveExtenderBlock> ADAPTIVE_EXTENDER = BLOCKS.register(
            "adaptive_extender",
            () -> new AdaptiveExtenderBlock(BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.METAL)));
    public static final RegistryObject<AdaptiveArmoryBlock> ADAPTIVE_ARMORY = BLOCKS.register(
            "adaptive_armory",
            () -> new AdaptiveArmoryBlock(BlockBehaviour.Properties.of().strength(4.0F).sound(SoundType.METAL)));
    public static final RegistryObject<AdaptiveFluidDrawerBlock> ADAPTIVE_FLUID_DRAWER = BLOCKS.register(
            "adaptive_fluid_drawer",
            () -> new AdaptiveFluidDrawerBlock(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL)));

    public static final RegistryObject<BlockItem> ADAPTIVE_CONTROLLER_ITEM = item("adaptive_controller", ADAPTIVE_CONTROLLER);
    public static final RegistryObject<BlockItem> ADAPTIVE_DRAWER_ITEM = item("adaptive_drawer", ADAPTIVE_DRAWER);
    public static final RegistryObject<BlockItem> ADAPTIVE_OAK_DRAWER_ITEM = item("adaptive_oak_drawer", ADAPTIVE_OAK_DRAWER);
    public static final RegistryObject<BlockItem> ADAPTIVE_BIRCH_DRAWER_ITEM = item("adaptive_birch_drawer", ADAPTIVE_BIRCH_DRAWER);
    public static final RegistryObject<BlockItem> ADAPTIVE_JUNGLE_DRAWER_ITEM = item("adaptive_jungle_drawer", ADAPTIVE_JUNGLE_DRAWER);
    public static final RegistryObject<BlockItem> ADAPTIVE_ACACIA_DRAWER_ITEM = item("adaptive_acacia_drawer", ADAPTIVE_ACACIA_DRAWER);
    public static final RegistryObject<BlockItem> ADAPTIVE_DARK_OAK_DRAWER_ITEM = item("adaptive_dark_oak_drawer", ADAPTIVE_DARK_OAK_DRAWER);
    public static final RegistryObject<BlockItem> ADAPTIVE_MANGROVE_DRAWER_ITEM = item("adaptive_mangrove_drawer", ADAPTIVE_MANGROVE_DRAWER);
    public static final RegistryObject<BlockItem> ADAPTIVE_CHERRY_DRAWER_ITEM = item("adaptive_cherry_drawer", ADAPTIVE_CHERRY_DRAWER);
    public static final RegistryObject<BlockItem> ADAPTIVE_CRIMSON_DRAWER_ITEM = item("adaptive_crimson_drawer", ADAPTIVE_CRIMSON_DRAWER);
    public static final RegistryObject<BlockItem> ADAPTIVE_WARPED_DRAWER_ITEM = item("adaptive_warped_drawer", ADAPTIVE_WARPED_DRAWER);
    public static final RegistryObject<BlockItem> ADAPTIVE_GRID_ITEM = item("adaptive_grid", ADAPTIVE_GRID);
    public static final RegistryObject<BlockItem> ADAPTIVE_CRAFTING_GRID_ITEM = item("adaptive_crafting_grid", ADAPTIVE_CRAFTING_GRID);
    public static final RegistryObject<BlockItem> ADAPTIVE_DEPOSIT_ITEM = item("adaptive_deposit", ADAPTIVE_DEPOSIT);

    private static RegistryObject<AdaptiveDrawerBlock> drawer(String name) {
        return BLOCKS.register(name, () -> new AdaptiveDrawerBlock(BlockBehaviour.Properties.of().strength(2.5F)
                .sound(SoundType.WOOD).lightLevel(state -> state.getValue(AdaptiveDrawerBlock.POWERED) ? 5 : 0)));
    }

    private static RegistryObject<BlockItem> item(String name, RegistryObject<? extends Block> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private ModBlocks() {
    }
}
