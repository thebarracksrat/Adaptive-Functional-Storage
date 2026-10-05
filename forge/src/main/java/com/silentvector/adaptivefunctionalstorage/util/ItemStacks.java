package com.silentvector.adaptivefunctionalstorage.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class ItemStacks {
    private ItemStacks() {
    }

    public static ItemStack copyWithCount(ItemStack stack, int count) {
        if (stack.isEmpty() || count <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        copy.setCount(count);
        return copy;
    }

    public static CompoundTag save(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        if (!stack.isEmpty()) {
            stack.save(tag);
        }
        return tag;
    }

    public static ItemStack load(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(tag);
    }
}
