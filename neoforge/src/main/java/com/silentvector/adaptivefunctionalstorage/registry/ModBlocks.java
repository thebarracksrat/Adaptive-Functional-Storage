package com.silentvector.adaptivefunctionalstorage.registry;

import com.silentvector.adaptivefunctionalstorage.AdaptiveFunctionalStorage;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveControllerBlock;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveDrawerBlock;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveGridBlock;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveCraftingGridBlock;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveDepositBlock;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveExtenderBlock;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveArmoryBlock;
import com.silentvector.adaptivefunctionalstorage.block.AdaptiveFluidDrawerBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AdaptiveFunctionalStorage.MOD_ID);
    public static final DeferredBlock<AdaptiveControllerBlock> ADAPTIVE_CONTROLLER = BLOCKS.registerBlock(
            "adaptive_controller", AdaptiveControllerBlock::new,
            BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.WOOD)
                    .lightLevel(state -> state.getValue(AdaptiveControllerBlock.POWERED) ? 7 : 0));
    public static final DeferredBlock<AdaptiveDrawerBlock> ADAPTIVE_DRAWER = BLOCKS.registerBlock(
            "adaptive_drawer", AdaptiveDrawerBlock::new,
            BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.WOOD)
                    .lightLevel(state -> state.getValue(AdaptiveDrawerBlock.POWERED) ? 5 : 0));
    public static final DeferredBlock<AdaptiveDrawerBlock> ADAPTIVE_OAK_DRAWER = drawer("adaptive_oak_drawer");
    public static final DeferredBlock<AdaptiveDrawerBlock> ADAPTIVE_BIRCH_DRAWER = drawer("adaptive_birch_drawer");
    public static final DeferredBlock<AdaptiveDrawerBlock> ADAPTIVE_JUNGLE_DRAWER = drawer("adaptive_jungle_drawer");
    public static final DeferredBlock<AdaptiveDrawerBlock> ADAPTIVE_ACACIA_DRAWER = drawer("adaptive_acacia_drawer");
    public static final DeferredBlock<AdaptiveDrawerBlock> ADAPTIVE_DARK_OAK_DRAWER = drawer("adaptive_dark_oak_drawer");
    public static final DeferredBlock<AdaptiveDrawerBlock> ADAPTIVE_MANGROVE_DRAWER = drawer("adaptive_mangrove_drawer");
    public static final DeferredBlock<AdaptiveDrawerBlock> ADAPTIVE_CHERRY_DRAWER = drawer("adaptive_cherry_drawer");
    public static final DeferredBlock<AdaptiveDrawerBlock> ADAPTIVE_CRIMSON_DRAWER = drawer("adaptive_crimson_drawer");
    public static final DeferredBlock<AdaptiveDrawerBlock> ADAPTIVE_WARPED_DRAWER = drawer("adaptive_warped_drawer");
    public static final DeferredBlock<AdaptiveGridBlock> ADAPTIVE_GRID = BLOCKS.registerBlock(
            "adaptive_grid", AdaptiveGridBlock::new,
            BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.METAL));
    public static final DeferredBlock<AdaptiveCraftingGridBlock> ADAPTIVE_CRAFTING_GRID = BLOCKS.registerBlock(
            "adaptive_crafting_grid", AdaptiveCraftingGridBlock::new,
            BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.METAL));
    public static final DeferredBlock<AdaptiveDepositBlock> ADAPTIVE_DEPOSIT = BLOCKS.registerBlock(
            "adaptive_deposit", AdaptiveDepositBlock::new,
            BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.METAL));
    public static final DeferredBlock<AdaptiveExtenderBlock> ADAPTIVE_EXTENDER = BLOCKS.registerBlock(
            "adaptive_extender", AdaptiveExtenderBlock::new,
            BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.METAL));
    public static final DeferredBlock<AdaptiveArmoryBlock> ADAPTIVE_ARMORY = BLOCKS.registerBlock(
            "adaptive_armory", AdaptiveArmoryBlock::new,
            BlockBehaviour.Properties.of().strength(4.0F).sound(SoundType.METAL));
    public static final DeferredBlock<AdaptiveFluidDrawerBlock> ADAPTIVE_FLUID_DRAWER = BLOCKS.registerBlock(
            "adaptive_fluid_drawer", AdaptiveFluidDrawerBlock::new,
            BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL));
    public static final DeferredItem<BlockItem> ADAPTIVE_CONTROLLER_ITEM = ModItems.ITEMS.register(
            "adaptive_controller", () -> new BlockItem(ADAPTIVE_CONTROLLER.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> ADAPTIVE_DRAWER_ITEM = ModItems.ITEMS.register(
            "adaptive_drawer", () -> new BlockItem(ADAPTIVE_DRAWER.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> ADAPTIVE_OAK_DRAWER_ITEM = item("adaptive_oak_drawer", ADAPTIVE_OAK_DRAWER);
    public static final DeferredItem<BlockItem> ADAPTIVE_BIRCH_DRAWER_ITEM = item("adaptive_birch_drawer", ADAPTIVE_BIRCH_DRAWER);
    public static final DeferredItem<BlockItem> ADAPTIVE_JUNGLE_DRAWER_ITEM = item("adaptive_jungle_drawer", ADAPTIVE_JUNGLE_DRAWER);
    public static final DeferredItem<BlockItem> ADAPTIVE_ACACIA_DRAWER_ITEM = item("adaptive_acacia_drawer", ADAPTIVE_ACACIA_DRAWER);
    public static final DeferredItem<BlockItem> ADAPTIVE_DARK_OAK_DRAWER_ITEM = item("adaptive_dark_oak_drawer", ADAPTIVE_DARK_OAK_DRAWER);
    public static final DeferredItem<BlockItem> ADAPTIVE_MANGROVE_DRAWER_ITEM = item("adaptive_mangrove_drawer", ADAPTIVE_MANGROVE_DRAWER);
    public static final DeferredItem<BlockItem> ADAPTIVE_CHERRY_DRAWER_ITEM = item("adaptive_cherry_drawer", ADAPTIVE_CHERRY_DRAWER);
    public static final DeferredItem<BlockItem> ADAPTIVE_CRIMSON_DRAWER_ITEM = item("adaptive_crimson_drawer", ADAPTIVE_CRIMSON_DRAWER);
    public static final DeferredItem<BlockItem> ADAPTIVE_WARPED_DRAWER_ITEM = item("adaptive_warped_drawer", ADAPTIVE_WARPED_DRAWER);
    public static final DeferredItem<BlockItem> ADAPTIVE_GRID_ITEM = ModItems.ITEMS.register(
            "adaptive_grid", () -> new BlockItem(ADAPTIVE_GRID.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> ADAPTIVE_CRAFTING_GRID_ITEM = ModItems.ITEMS.register(
            "adaptive_crafting_grid", () -> new BlockItem(ADAPTIVE_CRAFTING_GRID.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> ADAPTIVE_DEPOSIT_ITEM = ModItems.ITEMS.register(
            "adaptive_deposit", () -> new BlockItem(ADAPTIVE_DEPOSIT.get(), new Item.Properties()));
    private static DeferredBlock<AdaptiveDrawerBlock> drawer(String name) {
        return BLOCKS.registerBlock(name, AdaptiveDrawerBlock::new, BlockBehaviour.Properties.of().strength(2.5F)
                .sound(SoundType.WOOD).lightLevel(state -> state.getValue(AdaptiveDrawerBlock.POWERED) ? 5 : 0));
    }
    private static DeferredItem<BlockItem> item(String name, DeferredBlock<? extends net.minecraft.world.level.block.Block> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
    private ModBlocks() { }
}
