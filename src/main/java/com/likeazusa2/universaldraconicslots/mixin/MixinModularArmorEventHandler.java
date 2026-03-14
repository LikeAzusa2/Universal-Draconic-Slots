package com.likeazusa2.universaldraconicslots.mixin;

import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.modules.ModuleTypes;
import com.brandon3055.draconicevolution.api.modules.data.SpeedData;
import com.brandon3055.draconicevolution.api.modules.entities.FlightEntity;
import com.brandon3055.draconicevolution.handlers.ModularArmorEventHandler;
import com.brandon3055.draconicevolution.items.equipment.IModularItem;
import com.likeazusa2.universaldraconicslots.host.UDSHostResolver;
import com.likeazusa2.universaldraconicslots.host.UDSModuleContext;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModularArmorEventHandler.class, remap = false)
public abstract class MixinModularArmorEventHandler {
    @Inject(method = "tryTickStack", at = @At("HEAD"), cancellable = true)
    private static void uds$handleUpgradedItems(
            ItemStack stack,
            LivingEntity entity,
            EquipmentSlot slot,
            ModularArmorEventHandler.ArmorAbilities abilities,
            boolean fromEquipmentMod,
            CallbackInfo ci
    ) {
        // 原生 DE 这里只认 IModularItem。我们在这里把“已经改造过的普通物品”接进同一条 tick 链。
        if (stack.isEmpty() || stack.getItem() instanceof IModularItem) {
            return;
        }

        EquipmentSlot resolvedSlot = slot != null ? slot : UDSHostResolver.inferSlot(stack);
        if (!UDSHostResolver.isRuntimeSupported(stack, resolvedSlot)) {
            return;
        }

        try (var host = stack.getCapability(DECapabilities.Host.ITEM)) {
            if (host == null) {
                ci.cancel();
                return;
            }

            // 先跑模块 tick，再把护甲属性汇总进原版 ArmorAbilities，尽量复用 DE 的后续处理。
            host.handleTick(new UDSModuleContext(stack, entity, resolvedSlot));
            if ((resolvedSlot == null || resolvedSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR || fromEquipmentMod) && abilities != null) {
                collectArmorAbilities(stack, host, abilities);
            }
        }

        ci.cancel();
    }

    // 这里复用 DE 自己的 ArmorAbilities 汇总器，避免速度/飞行再额外写一套并和原版属性冲突。
    private static void collectArmorAbilities(ItemStack stack, com.brandon3055.draconicevolution.api.capability.ModuleHost host, ModularArmorEventHandler.ArmorAbilities abilities) {
        SpeedData speedData = host.getModuleData(ModuleTypes.SPEED, new SpeedData(0));
        if (speedData != null) {
            ((MixinArmorAbilities) abilities).uds$addSpeedData(speedData, host);
        }

        FlightEntity flight = host.getEntitiesByType(ModuleTypes.FLIGHT)
                .map(entity -> (FlightEntity) entity)
                .findAny()
                .orElse(null);
        if (flight != null) {
            ((MixinArmorAbilities) abilities).uds$addFlightData(flight, () -> stack.getCapability(CapabilityOP.ITEM));
        }
    }
}
