package com.silentvector.adaptivefunctionalstorage.client;

import com.silentvector.adaptivefunctionalstorage.menu.AdaptiveGridMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdaptiveGridScreen<T extends AdaptiveGridMenu> extends AbstractContainerScreen<T> {
    private static final ResourceLocation GRID_TEXTURE = new ResourceLocation("adaptive_functional_storage", "textures/gui/grid.png");
    protected static final ResourceLocation ROW_TEXTURE = new ResourceLocation("adaptive_functional_storage", "textures/gui/grid/row.png");
    private static final ResourceLocation SCROLLBAR_TEXTURE = new ResourceLocation("adaptive_functional_storage", "textures/gui/widget/scrollbar.png");
    private static final ResourceLocation SCROLLBAR_CLICKED_TEXTURE = new ResourceLocation("adaptive_functional_storage", "textures/gui/widget/scrollbar_clicked.png");
    private static final ResourceLocation SCROLLBAR_DISABLED_TEXTURE = new ResourceLocation("adaptive_functional_storage", "textures/gui/widget/scrollbar_disabled.png");
    private static final int COLUMNS = 9;
    private static final int ROWS = 3;
    private EditBox search;
    private int scrollRow;
    private boolean draggingScrollbar;
    private final List<Integer> filtered = new ArrayList<>();

    public AdaptiveGridScreen(T menu, Inventory inventory, Component title) {
        this(menu, inventory, title, 172, 77);
    }

    protected AdaptiveGridScreen(T menu, Inventory inventory, Component title, int screenHeight, int inventoryLabelY) {
        super(menu, inventory, title);
        imageWidth = 193;
        imageHeight = screenHeight;
        inventoryLabelX = 8;
        this.inventoryLabelY = inventoryLabelY;
    }

    @Override protected void init() {
        super.init();
        search = new EditBox(font, leftPos + 95, topPos + 7, 66, 12, Component.literal("Search"));
        search.setBordered(false);
        search.setMaxLength(50);
        search.setResponder(value -> { scrollRow = 0; rebuildFilter(); });
        addRenderableWidget(search);
        rebuildFilter();
    }

    private void rebuildFilter() {
        filtered.clear();
        String needle = search == null ? "" : search.getValue().toLowerCase(Locale.ROOT);
        for (int i = 0; i < AdaptiveGridMenu.MAX_SYNCED_RESOURCES; i++) {
            ItemStack stack = menu.resource(i);
            if (stack.isEmpty()) continue;
            String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
            String id = stack.getItemHolder().unwrapKey().map(key -> key.location().toString()).orElse("");
            if (needle.isEmpty() || name.contains(needle) || id.contains(needle)) filtered.add(i);
        }
        int max = Math.max(0, (filtered.size() - 1) / COLUMNS - ROWS + 1);
        scrollRow = Math.min(scrollRow, max);
    }

    @Override protected void containerTick() { super.containerTick(); rebuildFilter(); }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        graphics.blit(GRID_TEXTURE, x, y, 0, 0, imageWidth, 19, 256, 256);
        for (int row = 0; row < ROWS; row++) {
            int textureY = row == 0 ? 19 : row == ROWS - 1 ? 55 : 37;
            graphics.blit(GRID_TEXTURE, x, y + 19 + row * 18, 0, textureY, imageWidth, 18, 256, 256);
            graphics.blit(ROW_TEXTURE, x + 7, y + 19 + row * 18, 0, 0, 162, 18, 162, 18);
        }
        graphics.blit(GRID_TEXTURE, x, y + 73, 0, 73, imageWidth, 99, 256, 256);
        renderScrollbar(graphics);
    }

    protected void renderScrollbar(GuiGraphics graphics) {
        int max = maxScrollRow();
        ResourceLocation texture = max == 0 ? SCROLLBAR_DISABLED_TEXTURE : draggingScrollbar ? SCROLLBAR_CLICKED_TEXTURE : SCROLLBAR_TEXTURE;
        int travel = 37;
        int offset = max == 0 ? 0 : Math.round((float) scrollRow / max * travel);
        graphics.blit(texture, leftPos + 174, topPos + 20 + offset, 0, 0, 12, 15, 12, 15);
    }

    private int maxScrollRow() { return Math.max(0, (filtered.size() - 1) / COLUMNS - ROWS + 1); }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, headerTitle(), 8, 8, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        if (!menu.online()) graphics.drawCenteredString(font, "OFFLINE", 88, 47, 0xFFFF5555);
    }

    protected Component headerTitle() { return title; }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderResources(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderResources(GuiGraphics graphics, int mouseX, int mouseY) {
        int start = scrollRow * COLUMNS;
        for (int visible = 0; visible < COLUMNS * ROWS && start + visible < filtered.size(); visible++) {
            int index = filtered.get(start + visible);
            ItemStack stack = menu.resource(index);
            int x = leftPos + 8 + visible % COLUMNS * 18;
            int y = topPos + 20 + visible / COLUMNS * 18;
            graphics.renderItem(stack, x, y);
            String amount = abbreviate(menu.amount(index));
            graphics.pose().pushPose();
            graphics.pose().translate(x + 16, y + 10, 200);
            graphics.pose().scale(0.5F, 0.5F, 1.0F);
            graphics.drawString(font, amount, -font.width(amount), 0, 0xFFFFFFFF, true);
            graphics.pose().popPose();
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16)
                graphics.renderTooltip(font, stack, mouseX, mouseY);
        }
    }

    private static String abbreviate(int amount) {
        if (amount >= 1_000_000_000) return compact(amount / 1_000_000_000.0, "B");
        if (amount >= 1_000_000) return compact(amount / 1_000_000.0, "M");
        if (amount >= 1_000) return compact(amount / 1_000.0, "K");
        return Integer.toString(amount);
    }

    private static String compact(double value, String suffix) {
        return (value >= 10 ? String.format(Locale.ROOT, "%.0f", value) : String.format(Locale.ROOT, "%.1f", value)) + suffix;
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= leftPos + 174 && mouseX < leftPos + 186 && mouseY >= topPos + 20 && mouseY < topPos + 72 && maxScrollRow() > 0) {
            draggingScrollbar = true;
            updateScrollbarFromMouse(mouseY);
            return true;
        }
        // Carried stacks take precedence over resource-slot actions.
        if (!menu.getCarried().isEmpty() && insideResourceArea(mouseX, mouseY) && (button == 0 || button == 1)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button == 1 ? AdaptiveGridMenu.INSERT_ONE : AdaptiveGridMenu.INSERT_ALL);
            return true;
        }
        int visible = resourceAt(mouseX, mouseY);
        if (visible >= 0) {
            // Ctrl-click is reserved for crafting actions.
            if (hasControlDown()) return true;
            int id = filtered.get(scrollRow * COLUMNS + visible);
            if (hasShiftDown()) id += AdaptiveGridMenu.EXTRACT_TO_INVENTORY_OFFSET;
            else if (button == 1) id += AdaptiveGridMenu.EXTRACT_HALF_OFFSET;
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (search != null && search.isFocused()) {
            if (keyCode == 256) return super.keyPressed(keyCode, scanCode, modifiers);
            search.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean charTyped(char codePoint, int modifiers) {
        if (search != null && search.isFocused()) {
            search.charTyped(codePoint, modifiers);
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar && button == 0) {
            updateScrollbarFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingScrollbar && button == 0) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateScrollbarFromMouse(double mouseY) {
        double progress = Math.max(0, Math.min(1, (mouseY - (topPos + 27.5)) / 37.0));
        scrollRow = (int) Math.round(progress * maxScrollRow());
    }

    private boolean insideResourceArea(double x, double y) { return x >= leftPos + 7 && x < leftPos + 170 && y >= topPos + 19 && y < topPos + 73; }
    private int resourceAt(double x, double y) {
        if (!insideResourceArea(x, y)) return -1;
        int column = ((int) x - leftPos - 8) / 18, row = ((int) y - topPos - 20) / 18;
        int localX = ((int) x - leftPos - 8) % 18, localY = ((int) y - topPos - 20) % 18;
        int visible = row * COLUMNS + column;
        return localX < 16 && localY < 16 && scrollRow * COLUMNS + visible < filtered.size() ? visible : -1;
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (insideResourceArea(mouseX, mouseY)) {
            scrollRow = Math.max(0, Math.min(maxScrollRow(), scrollRow + (delta < 0 ? 1 : -1)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}
