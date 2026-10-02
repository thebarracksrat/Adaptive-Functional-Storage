package com.silentvector.adaptivefunctionalstorage.client;

import com.silentvector.adaptivefunctionalstorage.menu.AdaptiveCraftingGridMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class AdaptiveCraftingGridScreen extends AdaptiveGridScreen<AdaptiveCraftingGridMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("adaptive_functional_storage", "textures/gui/crafting_grid.png");
    public AdaptiveCraftingGridScreen(AdaptiveCraftingGridMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 229, 134);
    }
    @Override protected void init() {
        imageHeight = 229;
        super.init();
        imageHeight = 229;
        topPos = (height - imageHeight) / 2;
    }
    @Override protected Component headerTitle() { return Component.literal("Crafting Grid"); }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, 19, 256, 256);
        for (int row = 0; row < 3; row++) {
            int textureY = row == 0 ? 19 : row == 2 ? 55 : 37;
            graphics.blit(TEXTURE, x, y + 19 + row * 18, 0, textureY, imageWidth, 18, 256, 256);
            graphics.blit(ROW_TEXTURE, x + 7, y + 19 + row * 18, 0, 0, 162, 18, 162, 18);
        }
        graphics.blit(TEXTURE, x, y + 73, 0, 73, imageWidth, 156, 256, 256);
        renderScrollbar(graphics);
    }
}
