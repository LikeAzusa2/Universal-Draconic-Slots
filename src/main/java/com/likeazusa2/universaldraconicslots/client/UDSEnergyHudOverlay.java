package com.likeazusa2.universaldraconicslots.client;

import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.likeazusa2.universaldraconicslots.UniversalDraconicSlots;
import com.likeazusa2.universaldraconicslots.host.UDSEquippedItemHelper;
import com.likeazusa2.universaldraconicslots.host.UDSHostResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = UniversalDraconicSlots.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class UDSEnergyHudOverlay {
    private UDSEnergyHudOverlay() {
    }

    // 在左下角护盾 HUD 上方补一组改造装备/工具的电量显示。
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || !UDSClientConfig.isEnergyHudVisible()) {
            return;
        }

        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : UDSEquippedItemHelper.getHudDisplayItems(minecraft.player)) {
            if (!UDSHostResolver.getUpgradeData(stack).opEnabled()) {
                continue;
            }

            IOPStorage op = stack.getCapability(com.brandon3055.brandonscore.capability.CapabilityOP.ITEM);
            if (op == null || op.getMaxOPStored() <= 0) {
                continue;
            }
            stacks.add(stack);
        }
        if (stacks.isEmpty()) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int x = 8;
        int y = guiGraphics.guiHeight() - 92 - ((stacks.size() - 1) * 18);

        for (ItemStack stack : stacks) {
            IOPStorage op = stack.getCapability(com.brandon3055.brandonscore.capability.CapabilityOP.ITEM);
            if (op == null || op.getMaxOPStored() <= 0) {
                continue;
            }

            int percent = (int) Math.max(0, Math.min(100, Math.round((op.getOPStored() * 100F) / op.getMaxOPStored())));
            guiGraphics.renderItem(stack, x, y);
            guiGraphics.renderItemDecorations(minecraft.font, stack, x, y);
            guiGraphics.drawString(minecraft.font, Component.literal(percent + "%"), x + 20, y + 4, 0xFFFFFF, true);
            y += 18;
        }
    }
}
