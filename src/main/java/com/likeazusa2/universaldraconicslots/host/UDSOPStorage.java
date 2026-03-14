package com.likeazusa2.universaldraconicslots.host;

import com.brandon3055.draconicevolution.api.DataComponentAccessor;
import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.modules.lib.ModularOPStorage;
import com.likeazusa2.universaldraconicslots.data.UDSHostUpgradeData;
import net.minecraft.world.item.ItemStack;

public class UDSOPStorage extends ModularOPStorage {
    public UDSOPStorage(ItemStack stack, UDSHostUpgradeData data) {
        super(() -> stack.getCapability(DECapabilities.Host.ITEM), data.opTier().baseCapacity(), data.opTier().baseTransfer(), data.opTier().baseTransfer());
        updateDataAccess(DataComponentAccessor.itemStack(stack));
    }
}
