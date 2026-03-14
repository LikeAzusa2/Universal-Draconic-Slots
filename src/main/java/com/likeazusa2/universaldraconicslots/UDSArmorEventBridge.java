package com.likeazusa2.universaldraconicslots;

import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.modules.ModuleTypes;
import com.brandon3055.draconicevolution.api.modules.entities.ShieldControlEntity;
import com.brandon3055.draconicevolution.api.modules.entities.UndyingEntity;
import com.brandon3055.draconicevolution.init.DEDamage;
import com.brandon3055.draconicevolution.items.equipment.IModularArmor;
import com.likeazusa2.universaldraconicslots.host.UDSEquippedItemHelper;
import com.likeazusa2.universaldraconicslots.host.UDSHostResolver;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class UDSArmorEventBridge {
    private UDSArmorEventBridge() {
    }

    // 这里只补“已改造但不是原生 IModularArmor”的那部分护甲逻辑。
    // 不再因为玩家同时穿了 Curios 里的原生 DE 胸甲就整条短路，
    // 否则改造护甲和原生护甲会互相把对方的护盾桥接一起关掉。
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (event.isCanceled() || entity.level().isClientSide || event.getAmount() <= 0 || event.getSource().is(DEDamage.KILL)) {
            return;
        }

        for (ItemStack stack : UDSEquippedItemHelper.getUpgradedArmorItems(entity)) {
            EquipmentSlot slot = UDSHostResolver.inferSlot(stack);
            if (!shouldBridge(stack, slot)) {
                continue;
            }

            try (var host = stack.getCapability(DECapabilities.Host.ITEM)) {
                if (host == null) {
                    continue;
                }

                boolean blockedByUndying = host.getEntitiesByType(ModuleTypes.UNDYING)
                        .map(entityBase -> (UndyingEntity) entityBase)
                        .anyMatch(undying -> undying.tryBlockDamage(event));
                if (blockedByUndying || event.isCanceled()) {
                    return;
                }
            }
        }

        ItemStack shieldStack = UDSEquippedItemHelper.findShieldSourceStack(entity);
        EquipmentSlot shieldSlot = UDSHostResolver.inferSlot(shieldStack);
        if (!shouldBridge(shieldStack, shieldSlot)) {
            return;
        }

        try (var host = shieldStack.getCapability(DECapabilities.Host.ITEM)) {
            if (host == null) {
                return;
            }

            ShieldControlEntity shield = host.getEntitiesByType(ModuleTypes.SHIELD_CONTROLLER)
                    .map(entityBase -> (ShieldControlEntity) entityBase)
                    .findAny()
                    .orElse(null);
            if (shield != null) {
                shield.tryBlockDamage(event);
            }
        }
    }

    @SubscribeEvent
    public static void onDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || event.getNewDamage() <= 0 || event.getSource().is(DEDamage.KILL)) {
            return;
        }

        for (ItemStack stack : UDSEquippedItemHelper.getUpgradedArmorItems(entity)) {
            EquipmentSlot slot = UDSHostResolver.inferSlot(stack);
            if (!shouldBridge(stack, slot)) {
                continue;
            }

            try (var host = stack.getCapability(DECapabilities.Host.ITEM)) {
                if (host == null) {
                    continue;
                }

                boolean blockedByUndying = host.getEntitiesByType(ModuleTypes.UNDYING)
                        .map(entityBase -> (UndyingEntity) entityBase)
                        .anyMatch(undying -> undying.tryBlockDamage(event));
                if (blockedByUndying || event.getNewDamage() <= 0) {
                    return;
                }
            }
        }

        ItemStack shieldStack = UDSEquippedItemHelper.findShieldSourceStack(entity);
        EquipmentSlot shieldSlot = UDSHostResolver.inferSlot(shieldStack);
        if (!shouldBridge(shieldStack, shieldSlot)) {
            return;
        }

        try (var host = shieldStack.getCapability(DECapabilities.Host.ITEM)) {
            if (host == null) {
                return;
            }

            ShieldControlEntity shield = host.getEntitiesByType(ModuleTypes.SHIELD_CONTROLLER)
                    .map(entityBase -> (ShieldControlEntity) entityBase)
                    .findAny()
                    .orElse(null);
            if (shield != null) {
                shield.tryBlockDamage(event);
            }
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (event.isCanceled() || entity.level().isClientSide) {
            return;
        }

        List<UndyingEntity> undyingEntities = new ArrayList<>();
        for (ItemStack stack : UDSEquippedItemHelper.getUpgradedArmorItems(entity)) {
            EquipmentSlot slot = UDSHostResolver.inferSlot(stack);
            if (!shouldBridge(stack, slot)) {
                continue;
            }

            try (var host = stack.getCapability(DECapabilities.Host.ITEM)) {
                if (host == null) {
                    continue;
                }
                host.getEntitiesByType(ModuleTypes.UNDYING)
                        .map(entityBase -> (UndyingEntity) entityBase)
                        .forEach(undyingEntities::add);
            }
        }

        boolean blockedDeath = undyingEntities.stream()
                .sorted(Comparator.comparingInt((UndyingEntity undying) -> undying.getModule().getModuleTechLevel().index).reversed())
                .anyMatch(undying -> undying.tryBlockDeath(event));
        if (blockedDeath) {
            event.setCanceled(true);
        }
    }

    private static boolean shouldBridge(ItemStack stack, EquipmentSlot slot) {
        return !stack.isEmpty()
                && UDSHostResolver.isRuntimeSupported(stack, slot)
                && !(stack.getItem() instanceof IModularArmor);
    }
}
