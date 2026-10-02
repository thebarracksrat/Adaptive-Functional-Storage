package com.silentvector.adaptivefunctionalstorage.util;

import com.buuz135.functionalstorage.item.LinkingToolItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public final class LinkingToolNbt {
    private LinkingToolNbt() {
    }

    public static boolean hasController(ItemStack tool) {
        return tool.hasTag() && tool.getTag().contains(LinkingToolItem.NBT_CONTROLLER);
    }

    @Nullable
    public static BlockPos getController(ItemStack tool) {
        if (!hasController(tool)) {
            return null;
        }
        CompoundTag controller = tool.getTag().getCompound(LinkingToolItem.NBT_CONTROLLER);
        if (!controller.contains("X") || !controller.contains("Y") || !controller.contains("Z")) {
            return null;
        }
        return new BlockPos(controller.getInt("X"), controller.getInt("Y"), controller.getInt("Z"));
    }
}
