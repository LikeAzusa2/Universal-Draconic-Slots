package com.likeazusa2.universaldraconicslots;

import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.modules.ModuleTypes;
import com.brandon3055.draconicevolution.api.modules.data.AOEData;
import com.brandon3055.draconicevolution.api.modules.data.DamageData;
import com.brandon3055.draconicevolution.api.modules.data.ProjectileData;
import com.brandon3055.draconicevolution.init.EquipCfg;
import com.brandon3055.draconicevolution.items.equipment.ModularBow;
import com.likeazusa2.universaldraconicslots.host.UDSHostResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class UDSHandheldEventBridge {
    private UDSHandheldEventBridge() {
    }

    // 工具类的范围挖掘原版只认 IModularMiningTool，这里给改造后的普通工具补一个等价入口。
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = player.getMainHandItem();
        if (!UDSHostResolver.isRuntimeSupported(stack, EquipmentSlot.MAINHAND)
                || !UDSHostResolver.isMiningTool(stack)
                || stack.getCapability(CapabilityOP.ITEM) == null) {
            return;
        }

        int aoe = getMiningAOE(stack);
        if (aoe <= 0) {
            return;
        }

        if (breakAOEBlocks(player, stack, event.getPos(), aoe)) {
            event.setCanceled(true);
        }
    }

    // 近战 AOE 原版走 IModularMelee，这里把改造武器也接进同类效果里。
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!(event.getTarget() instanceof LivingEntity target)) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (!UDSHostResolver.isRuntimeSupported(stack, EquipmentSlot.MAINHAND)
                || !UDSHostResolver.isMeleeWeapon(stack)
                || player.getAttackStrengthScale(0.5F) <= 0.9F) {
            return;
        }

        float aoe = getAttackAOE(stack);
        if (aoe <= 0) {
            return;
        }

        float baseDamage = (float) Math.max(1D, player.getAttributeValue(Attributes.ATTACK_DAMAGE));
        for (LivingEntity nearby : player.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(aoe, 0.25D, aoe))) {
            if (nearby == player || nearby == target || player.isAlliedTo(nearby) || nearby.distanceTo(target) > aoe) {
                continue;
            }

            float healthBefore = nearby.getHealth();
            if (nearby.hurt(player.damageSources().playerAttack(player), baseDamage)) {
                nearby.knockback(0.4D, Math.sin(Math.toRadians(player.getYRot())), -Math.cos(Math.toRadians(player.getYRot())));
                consumeAttackEnergy(player, stack, baseDamage);
                if (healthBefore - nearby.getHealth() > 2F && player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR, nearby.getX(), nearby.getY(0.5D), nearby.getZ(), Math.max(1, Math.round(baseDamage * 0.5F)), 0.1D, 0D, 0.1D, 0.2D);
                }
            }
        }
    }

    // 纯额外伤害模块和范围攻击分开处理，避免一次攻击里把两类效果混成一团。
    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof Player player)
                || player.level().isClientSide
                || event.getSource().is(DamageTypeTags.IS_PROJECTILE)
                || event.getNewDamage() <= 0) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (!UDSHostResolver.isRuntimeSupported(stack, EquipmentSlot.MAINHAND)
                || !UDSHostResolver.isMeleeWeapon(stack)
                || stack.getCapability(CapabilityOP.ITEM) == null) {
            return;
        }

        float bonusDamage = getBonusDamage(stack);
        if (bonusDamage <= 0F) {
            return;
        }

        event.setNewDamage(event.getNewDamage() + bonusDamage);
        consumeAttackEnergy(player, stack, bonusDamage);
    }

    // 弓和弩发射出的箭矢在生成时补投射物模块数据，这样普通弓也能吃到 DE 的弹道强化。
    @SubscribeEvent
    public static void onArrowSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof AbstractArrow arrow)) {
            return;
        }

        Entity owner = arrow.getOwner();
        if (!(owner instanceof Player player)) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (!UDSHostResolver.isRuntimeSupported(stack, EquipmentSlot.MAINHAND)
                || !UDSHostResolver.isRangedWeapon(stack)
                || stack.getCapability(CapabilityOP.ITEM) == null) {
            return;
        }

        ProjectileData data = getProjectileData(stack);
        if (data == null) {
            return;
        }

        float velocityMultiplier = 1F + data.velocity();
        if (velocityMultiplier > 0F) {
            arrow.setDeltaMovement(arrow.getDeltaMovement().scale(velocityMultiplier));
        }
        arrow.setBaseDamage(arrow.getBaseDamage() * (1F + data.damage()));

        long cost = ModularBow.calculateShotEnergy(stack, player.registryAccess());
        var opStorage = stack.getCapability(CapabilityOP.ITEM);
        if (opStorage != null && cost > 0) {
            opStorage.modifyEnergyStored(-cost);
        }
    }

    private static float getBonusDamage(ItemStack stack) {
        try (var host = stack.getCapability(DECapabilities.Host.ITEM)) {
            if (host == null) {
                return 0F;
            }
            DamageData data = host.getModuleData(ModuleTypes.DAMAGE, new DamageData(0));
            return (float) data.damagePoints();
        }
    }

    private static float getAttackAOE(ItemStack stack) {
        try (var host = stack.getCapability(DECapabilities.Host.ITEM)) {
            if (host == null) {
                return 0F;
            }
            double aoe = host.getModuleData(ModuleTypes.AOE, new AOEData(0)).aoe() * 1.5D;
            if (host.hasDecimal("attack_aoe")) {
                aoe = host.getDecimal("attack_aoe").getValue();
            }
            return (float) aoe;
        }
    }

    private static int getMiningAOE(ItemStack stack) {
        try (var host = stack.getCapability(DECapabilities.Host.ITEM)) {
            if (host == null) {
                return 0;
            }
            int aoe = host.getModuleData(ModuleTypes.AOE, new AOEData(0)).aoe();
            if (host.hasInt("mining_aoe")) {
                aoe = host.getInt("mining_aoe").getValue();
            }
            return aoe;
        }
    }

    private static ProjectileData getProjectileData(ItemStack stack) {
        try (var host = stack.getCapability(DECapabilities.Host.ITEM)) {
            if (host == null) {
                return null;
            }
            return host.getModuleData(ModuleTypes.PROJ_MODIFIER, new ProjectileData(0, 0, 0, 0, 0));
        }
    }

    private static void consumeAttackEnergy(Player player, ItemStack stack, float damage) {
        var opStorage = stack.getCapability(CapabilityOP.ITEM);
        if (opStorage != null && damage > 0F) {
            long cost = Math.round(EquipCfg.energyAttack * damage);
            opStorage.modifyEnergyStored(-cost);
        }
    }

    private static boolean breakAOEBlocks(Player player, ItemStack stack, BlockPos origin, int aoe) {
        BlockState originState = player.level().getBlockState(origin);
        if (!stack.isCorrectToolForDrops(originState)) {
            return false;
        }

        float originStrength = originState.getDestroySpeed(player.level(), origin);
        if (originStrength < 0) {
            return false;
        }

        int radius = aoe;
        boolean brokeAny = false;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, 0, -radius), origin.offset(radius, 0, radius))) {
            if (pos.equals(origin)) {
                continue;
            }

            // 这里先做一轮强度和掉落判断，尽量贴近 DE 原版工具对 AOE 的限制。
            BlockState state = player.level().getBlockState(pos);
            if (state.isAir() || !stack.isCorrectToolForDrops(state)) {
                continue;
            }

            float strength = state.getDestroySpeed(player.level(), pos);
            if (strength < 0 || originStrength / Math.max(strength, 0.0001F) > 10F) {
                continue;
            }

            if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                stack.mineBlock(serverLevel, state, pos, player);
                serverLevel.destroyBlock(pos, true, player);
                var opStorage = stack.getCapability(CapabilityOP.ITEM);
                if (opStorage != null) {
                    opStorage.modifyEnergyStored(-EquipCfg.energyHarvest);
                }
                brokeAny = true;
            }
        }
        return brokeAny;
    }
}
