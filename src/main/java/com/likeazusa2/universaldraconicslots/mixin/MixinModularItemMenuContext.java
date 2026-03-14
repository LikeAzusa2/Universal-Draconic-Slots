package com.likeazusa2.universaldraconicslots.mixin;

import com.brandon3055.brandonscore.inventory.PlayerSlot;
import com.brandon3055.draconicevolution.api.modules.lib.ModuleContext;
import com.brandon3055.draconicevolution.inventory.ModularItemMenu;
import com.brandon3055.draconicevolution.items.equipment.IModularItem;
import com.likeazusa2.universaldraconicslots.host.UDSHostResolver;
import com.likeazusa2.universaldraconicslots.host.UDSModuleContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ModularItemMenu.class, remap = false)
public abstract class MixinModularItemMenuContext {
    @Shadow private ItemStack hostStack;
    @Shadow private Player player;
    @Shadow private PlayerSlot slot;

    @Inject(method = "getModuleContext", at = @At("HEAD"), cancellable = true)
    private void uds$replaceContext(CallbackInfoReturnable<ModuleContext> cir) {
        // 普通物品虽然不是 IModularItem，但只要已经改造并且挂了宿主，也要拿到正确的模块上下文。
        if (!(hostStack.getItem() instanceof IModularItem) && UDSHostResolver.isRuntimeSupported(hostStack, slot.getEquipmentSlot())) {
            cir.setReturnValue(new UDSModuleContext(hostStack, player, slot.getEquipmentSlot()));
        }
    }
}
