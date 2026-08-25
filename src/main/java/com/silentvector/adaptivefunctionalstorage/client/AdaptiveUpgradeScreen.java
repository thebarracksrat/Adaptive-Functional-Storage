package com.silentvector.adaptivefunctionalstorage.client;

import com.silentvector.adaptivefunctionalstorage.menu.AdaptiveUpgradeMenu;
import com.hrznstudio.titanium.client.screen.container.BasicContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class AdaptiveUpgradeScreen extends BasicContainerScreen<AdaptiveUpgradeMenu> {
    public AdaptiveUpgradeScreen(AdaptiveUpgradeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        inventoryLabelX = 8;
        inventoryLabelY = 92;
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        Component label = menu.controllerMode()
                ? Component.literal("Controller range")
                : Component.literal("Storage");
        graphics.drawString(font, label, 10, 59, getTitleColor(), false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, getTitleColor(), false);
    }
}
