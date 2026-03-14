package com.likeazusa2.universaldraconicslots.mixin;

import com.brandon3055.draconicevolution.api.modules.ModuleHelper;
import com.likeazusa2.universaldraconicslots.host.UDSEquippedItemHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ModuleHelper.class, remap = false)
public abstract class MixinModuleHelper {
    @Inject(method = "getEquippedHostItems", at = @At("RETURN"), cancellable = true)
    private static void uds$appendUpgradedHostItems(LivingEntity entity, CallbackInfoReturnable<List<ItemStack>> cir) {
        // 把改造物品补进 DE 的“已装备宿主列表”，这样很多按宿主聚合的数据就能直接复用原版流程。
        List<ItemStack> merged = new ArrayList<>(cir.getReturnValue());
        merged.addAll(UDSEquippedItemHelper.getUpgradedEquippedItems(entity));
        cir.setReturnValue(merged);
    }
}
