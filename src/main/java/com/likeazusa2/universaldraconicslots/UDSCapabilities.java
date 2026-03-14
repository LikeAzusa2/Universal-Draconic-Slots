package com.likeazusa2.universaldraconicslots;

import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.event.ModularItemInitEvent;
import com.likeazusa2.universaldraconicslots.host.UDSHostFactory;
import com.likeazusa2.universaldraconicslots.host.UDSHostResolver;
import com.likeazusa2.universaldraconicslots.host.UDSOPStorage;
import com.likeazusa2.universaldraconicslots.machine.HostForgeBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class UDSCapabilities {
    private UDSCapabilities() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        Item[] supportedItems = BuiltInRegistries.ITEM.stream()
                .filter(item -> !(item instanceof com.brandon3055.draconicevolution.items.equipment.IModularItem))
                .toArray(Item[]::new);

        event.registerItem(DECapabilities.Host.ITEM, UDSCapabilities::createHost, supportedItems);
        event.registerItem(CapabilityOP.ITEM, UDSCapabilities::createOpStorage, supportedItems);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModContent.HOST_FORGE_BLOCK_ENTITY.get(), (blockEntity, side) -> blockEntity.getEnergyStorage());
        event.registerBlockEntity(CapabilityOP.BLOCK, ModContent.HOST_FORGE_BLOCK_ENTITY.get(), (blockEntity, side) -> blockEntity.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModContent.HOST_FORGE_BLOCK_ENTITY.get(), (blockEntity, side) -> blockEntity.getInventory());
    }

    private static UDSHostFactory.Host createHost(ItemStack stack, Void context) {
        if (!UDSHostResolver.isStackSupported(stack)) {
            return null;
        }

        UDSHostFactory.Host host = UDSHostFactory.create(stack);
        NeoForge.EVENT_BUS.post(new ModularItemInitEvent(stack, host, host));
        return host;
    }

    private static UDSOPStorage createOpStorage(ItemStack stack, Void context) {
        var data = UDSHostResolver.getUpgradeData(stack);
        if (data == null || !data.opEnabled()) {
            return null;
        }
        return new UDSOPStorage(stack, data);
    }
}
