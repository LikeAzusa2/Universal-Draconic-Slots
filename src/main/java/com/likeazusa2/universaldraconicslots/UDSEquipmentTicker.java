package com.likeazusa2.universaldraconicslots;

import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.api.power.OPStorage;
import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.modules.ModuleTypes;
import com.brandon3055.draconicevolution.api.modules.entities.AutoFireEntity;
import com.brandon3055.draconicevolution.items.equipment.ModularBow;
import com.likeazusa2.universaldraconicslots.host.UDSHostResolver;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class UDSEquipmentTicker {
    private static final long REPAIR_OP_PER_POINT = 4_000L;
    private static final int MAX_REPAIR_PER_TICK = 10;

    private UDSEquipmentTicker() {
    }

    // 这里保留给“原版物品类不会主动调用”的模块逻辑，比如原版弓的自动射击。
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !UDSConfig.ENABLED.get()) {
            return;
        }

        repairEquippedItems(player);
        updateAutoFire(player);
    }

    // 有耐久的改造装备会消耗自身 OP 自动修复。
    private static void repairEquippedItems(ServerPlayer player) {
        if (!UDSConfig.REPAIR_DURABILITY_WITH_OP.get()) {
            return;
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor() && slot != EquipmentSlot.MAINHAND && slot != EquipmentSlot.OFFHAND) {
                continue;
            }

            ItemStack stack = player.getItemBySlot(slot);
            if (!UDSHostResolver.isRuntimeSupported(stack, slot) || !stack.isDamaged()) {
                continue;
            }

            IOPStorage op = stack.getCapability(com.brandon3055.brandonscore.capability.CapabilityOP.ITEM);
            if (op == null) {
                continue;
            }

            int repairPoints = (int) Math.min(
                    Math.min(stack.getDamageValue(), MAX_REPAIR_PER_TICK),
                    op.getOPStored() / REPAIR_OP_PER_POINT
            );
            if (repairPoints <= 0) {
                continue;
            }

            long cost = repairPoints * REPAIR_OP_PER_POINT;
            long paid = extractOp(op, cost);
            int actualRepair = (int) Math.min(repairPoints, paid / REPAIR_OP_PER_POINT);
            if (actualRepair <= 0) {
                continue;
            }

            stack.setDamageValue(Math.max(0, stack.getDamageValue() - actualRepair));
        }
    }

    private static void updateAutoFire(ServerPlayer player) {
        if (!player.isUsingItem()) {
            return;
        }

        // 自动射击只对“正在蓄力中的改造弓/弩”生效，避免误触发其它使用动作。
        ItemStack stack = player.getUseItem();
        EquipmentSlot slot = UDSHostResolver.inferSlot(stack);
        if (!UDSHostResolver.isRuntimeSupported(stack, slot) || !UDSHostResolver.isRangedWeapon(stack)) {
            return;
        }

        try (var host = stack.getCapability(DECapabilities.Host.ITEM)) {
            if (host == null) {
                return;
            }

            AutoFireEntity autoFire = host.getEntitiesByType(ModuleTypes.AUTO_FIRE)
                    .map(entity -> (AutoFireEntity) entity)
                    .findAny()
                    .orElse(null);
            if (autoFire == null || !autoFire.getAutoFireEnabled()) {
                return;
            }

            int useTime = stack.getUseDuration(player) - player.getUseItemRemainingTicks();
            int requiredCharge = Math.max(1, ModularBow.getChargeTicks(stack, player.registryAccess()));
            if (useTime < requiredCharge) {
                return;
            }

            // 这里直接复刻 ModularBow.onUseTick 的关键流程：放箭，然后立刻再次进入蓄力。
            var hand = player.getUsedItemHand();
            player.stopUsingItem();
            stack.releaseUsing(player.level(), player, 0);
            player.startUsingItem(hand);
        }
    }

    private static long extractOp(IOPStorage op, long cost) {
        if (cost <= 0) {
            return 0;
        }
        if (op instanceof OPStorage storage) {
            return Math.abs(storage.modifyEnergyStored(-cost));
        }
        return op.extractOP(cost, false);
    }
}
