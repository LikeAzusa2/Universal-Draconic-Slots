package com.likeazusa2.universaldraconicslots.mixin;

import com.brandon3055.draconicevolution.client.render.hud.ShieldHudElement;
import com.brandon3055.draconicevolution.items.equipment.IModularArmor;
import com.likeazusa2.universaldraconicslots.host.UDSEquippedItemHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ShieldHudElement.class, remap = false)
public abstract class MixinShieldHudElement {
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/brandon3055/draconicevolution/items/equipment/IModularArmor;getArmor(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack uds$resolveShieldHudStack(LivingEntity entity) {
        // 护盾 HUD 优先保持原版胸甲逻辑；原版找不到时，再退到我们自己的护盾来源。
        ItemStack nativeStack = IModularArmor.getArmor(entity);
        if (!nativeStack.isEmpty()) {
            return nativeStack;
        }
        return UDSEquippedItemHelper.findShieldSourceStack(entity);
    }
}
