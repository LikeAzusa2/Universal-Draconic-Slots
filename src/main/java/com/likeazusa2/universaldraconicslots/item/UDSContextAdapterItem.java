package com.likeazusa2.universaldraconicslots.item;

import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.draconicevolution.api.capability.ModuleHost;
import com.brandon3055.draconicevolution.api.modules.lib.ModularOPStorage;
import com.brandon3055.draconicevolution.api.modules.lib.ModuleHostImpl;
import com.brandon3055.draconicevolution.items.equipment.IModularItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class UDSContextAdapterItem extends Item implements IModularItem {
    public UDSContextAdapterItem(Properties properties) {
        super(properties);
    }

    @Override
    public TechLevel getTechLevel() {
        return TechLevel.DRACONIC;
    }

    @Override
    public ModuleHostImpl instantiateHost(ItemStack stack) {
        throw new UnsupportedOperationException("UDS context adapter item does not instantiate hosts");
    }

    @Override
    public ModularOPStorage instantiateOPStorage(ItemStack stack, Supplier<ModuleHost> host) {
        throw new UnsupportedOperationException("UDS context adapter item does not provide OP storage");
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }
}
