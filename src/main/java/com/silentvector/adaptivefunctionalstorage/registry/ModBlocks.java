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
    public static final DeferredItem<BlockItem> ADAPTIVE_GRID_ITEM = ModItems.ITEMS.register(
            "adaptive_grid", () -> new BlockItem(ADAPTIVE_GRID.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> ADAPTIVE_CRAFTING_GRID_ITEM = ModItems.ITEMS.register(
            "adaptive_crafting_grid", () -> new BlockItem(ADAPTIVE_CRAFTING_GRID.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> ADAPTIVE_DEPOSIT_ITEM = ModItems.ITEMS.register(
            "adaptive_deposit", () -> new BlockItem(ADAPTIVE_DEPOSIT.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> ADAPTIVE_EXTENDER_ITEM = ModItems.ITEMS.register(
            "adaptive_extender", () -> new BlockItem(ADAPTIVE_EXTENDER.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> ADAPTIVE_ARMORY_ITEM = ModItems.ITEMS.register(
            "adaptive_armory", () -> new BlockItem(ADAPTIVE_ARMORY.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> ADAPTIVE_FLUID_DRAWER_ITEM = ModItems.ITEMS.register(
            "adaptive_fluid_drawer", () -> new BlockItem(ADAPTIVE_FLUID_DRAWER.get(), new Item.Properties()));
    private ModBlocks() { }
}
