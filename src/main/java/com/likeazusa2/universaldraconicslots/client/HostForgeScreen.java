package com.likeazusa2.universaldraconicslots.client;

import com.likeazusa2.universaldraconicslots.machine.HostForgeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class HostForgeScreen extends AbstractContainerScreen<HostForgeMenu> {
    private int feedbackNonce = -1;
    private int feedbackUntilTick = 0;
    private Component feedbackMessage = Component.empty();

    public HostForgeScreen(HostForgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 190;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.universaldraconicslots.host_forge.apply"), button -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, HostForgeMenu.APPLY_BUTTON_ID);
            }
        }).bounds(leftPos + 63, topPos + 87, 50, 16).build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF1A1A1A);
        guiGraphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + 100, 0xFF262626);
        guiGraphics.fill(leftPos + 4, topPos + 104, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFF202020);
        drawSlotFrame(guiGraphics, leftPos + 79, topPos + 11);
        drawSlotFrame(guiGraphics, leftPos + 79, topPos + 41);
        drawSlotFrame(guiGraphics, leftPos + 25, topPos + 41);
        drawSlotFrame(guiGraphics, leftPos + 133, topPos + 41);
        drawSlotFrame(guiGraphics, leftPos + 79, topPos + 71);

        int barHeight = 60;
        int filled = menu.getMaxEnergyStored() <= 0 ? 0 : Math.min(barHeight, (int) ((long) menu.getEnergyStored() * barHeight / menu.getMaxEnergyStored()));
        guiGraphics.fill(leftPos + 154, topPos + 18, leftPos + 162, topPos + 78, 0xFF111111);
        guiGraphics.fill(leftPos + 154, topPos + 78 - filled, leftPos + 162, topPos + 78, 0xFF27AE60);
    }

    private void drawSlotFrame(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 18, 0xFF080808);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
        guiGraphics.fill(x + 2, y + 2, x + 16, y + 16, 0xFF3B3B3B);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, 8, 6, 0xFFFFFF, false);
        guiGraphics.drawString(font, compact(menu.getEnergyStored()) + "/" + compact(menu.getMaxEnergyStored()), 8, 18, 0xE0E0E0, false);
        guiGraphics.drawString(font, Component.translatable("gui.universaldraconicslots.host_forge.tech"), 74, 3, 0xC8C8C8, false);
        guiGraphics.drawString(font, Component.translatable("gui.universaldraconicslots.host_forge.target"), 74, 33, 0xC8C8C8, false);
        guiGraphics.drawString(font, Component.translatable("gui.universaldraconicslots.host_forge.width"), 20, 33, 0xC8C8C8, false);
        guiGraphics.drawString(font, Component.translatable("gui.universaldraconicslots.host_forge.height"), 128, 33, 0xC8C8C8, false);
        guiGraphics.drawString(font, Component.translatable("gui.universaldraconicslots.host_forge.op"), 81, 63, 0xC8C8C8, false);
        guiGraphics.drawString(font, Component.translatable("gui.universaldraconicslots.host_forge.cost_short", compact(menu.getPreviewCost())), 8, 84, 0xE0E0E0, false);
        if (!feedbackMessage.getString().isEmpty() && minecraft != null && minecraft.level != null && minecraft.level.getGameTime() < feedbackUntilTick) {
            guiGraphics.drawCenteredString(font, feedbackMessage, imageWidth / 2, 99, 0xFF8A80);
        }
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFFFFF, false);
    }

    private static String compact(long value) {
        if (value >= 1_000_000_000L) {
            return String.format("%.1fG", value / 1_000_000_000D);
        }
        if (value >= 1_000_000L) {
            return trim(value / 1_000_000D) + "M";
        }
        if (value >= 1_000L) {
            return trim(value / 1_000D) + "k";
        }
        return Long.toString(value);
    }

    private static String trim(double value) {
        String formatted = String.format("%.1f", value);
        return formatted.endsWith(".0") ? formatted.substring(0, formatted.length() - 2) : formatted;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        tickFeedback();
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void tickFeedback() {
        if (minecraft == null || minecraft.level == null) {
            return;
        }
        int currentNonce = menu.getFeedbackNonce();
        if (currentNonce != feedbackNonce) {
            feedbackNonce = currentNonce;
            feedbackMessage = menu.getFeedbackMessage();
            feedbackUntilTick = (int) minecraft.level.getGameTime() + 20;
        }
        if (minecraft.level.getGameTime() >= feedbackUntilTick) {
            feedbackMessage = Component.empty();
        }
    }
}
