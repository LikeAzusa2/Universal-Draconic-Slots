package com.likeazusa2.universaldraconicslots.mixin;

import com.brandon3055.draconicevolution.items.equipment.IModularArmor;
import com.likeazusa2.universaldraconicslots.host.UDSEquippedItemHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = IModularArmor.class, remap = false)
public interface MixinIModularArmor {
    // 当原版没有找到 modular chestpiece 时，把已改造胸甲补进去，让跳跃、飞行、水下挖掘等逻辑直接复用 DE 原版入口。
    @Inject(method = "getArmor", at = @At("RETURN"), cancellable = true)
    private static void uds$appendUpgradedChestpiece(LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (!cir.getReturnValue().isEmpty()) {
            return;
        }

        ItemStack upgradedChest = UDSEquippedItemHelper.findArmorSourceStack(entity);
        if (!upgradedChest.isEmpty()) {
            cir.setReturnValue(upgradedChest);
        }
    }
}
