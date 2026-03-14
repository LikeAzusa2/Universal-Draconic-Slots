package com.likeazusa2.universaldraconicslots.host;

import com.brandon3055.draconicevolution.api.modules.lib.StackModuleContext;
import com.brandon3055.draconicevolution.items.equipment.IModularItem;
import com.likeazusa2.universaldraconicslots.ModContent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class UDSModuleContext extends StackModuleContext {
    public UDSModuleContext(ItemStack stack, LivingEntity entity, EquipmentSlot slot) {
        super(stack, entity, slot);
    }

    @Override
    public IModularItem getItem() {
        if (getStack().getItem() instanceof IModularItem modularItem) {
            return modularItem;
        }
        return ModContent.CONTEXT_ADAPTER_ITEM.get();
    }

    @Override
    public boolean isEquipped() {
        return true;
    }
}
