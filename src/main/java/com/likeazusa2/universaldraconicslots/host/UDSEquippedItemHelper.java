package com.likeazusa2.universaldraconicslots.host;

import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.modules.ModuleTypes;
import com.brandon3055.draconicevolution.integration.equipment.EquipmentManager;
import com.brandon3055.draconicevolution.items.equipment.IModularItem;
import com.brandon3055.draconicevolution.items.equipment.IModularArmor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class UDSEquippedItemHelper {
    private UDSEquippedItemHelper() {
    }

    // 收集玩家或生物当前已经穿戴/手持的改造物品，供模块聚合和 HUD 使用。
    public static List<ItemStack> getUpgradedEquippedItems(LivingEntity entity) {
        List<ItemStack> items = new ArrayList<>();
        addIfSupported(items, entity.getItemBySlot(EquipmentSlot.HEAD), EquipmentSlot.HEAD);
        addIfSupported(items, entity.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST);
        addIfSupported(items, entity.getItemBySlot(EquipmentSlot.LEGS), EquipmentSlot.LEGS);
        addIfSupported(items, entity.getItemBySlot(EquipmentSlot.FEET), EquipmentSlot.FEET);
        addIfSupported(items, entity.getItemBySlot(EquipmentSlot.MAINHAND), EquipmentSlot.MAINHAND);
        addIfSupported(items, entity.getItemBySlot(EquipmentSlot.OFFHAND), EquipmentSlot.OFFHAND);
        return items;
    }

    // HUD 只展示玩家当前真正穿戴/手持的改造宿主，不去扫描背包。
    public static List<ItemStack> getHudDisplayItems(LivingEntity entity) {
        return getUpgradedEquippedItems(entity);
    }

    // 只收集护甲槽，给护盾、飞行、速度这类护甲逻辑使用。
    public static List<ItemStack> getUpgradedArmorItems(LivingEntity entity) {
        List<ItemStack> items = new ArrayList<>();
        addIfSupported(items, entity.getItemBySlot(EquipmentSlot.HEAD), EquipmentSlot.HEAD);
        addIfSupported(items, entity.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST);
        addIfSupported(items, entity.getItemBySlot(EquipmentSlot.LEGS), EquipmentSlot.LEGS);
        addIfSupported(items, entity.getItemBySlot(EquipmentSlot.FEET), EquipmentSlot.FEET);
        return items;
    }

    // 这里返回“像 DE 胸甲一样参与护甲逻辑”的来源，默认只认胸甲槽位。
    public static ItemStack findArmorSourceStack(LivingEntity entity) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        return UDSHostResolver.isRuntimeSupported(chest, EquipmentSlot.CHEST) ? chest : ItemStack.EMPTY;
    }

    // 只判断原生 DE 是否已经提供了真正的 modular armor，避免和我们的补桥互相打架。
    public static boolean hasNativeModularArmor(LivingEntity entity) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof IModularArmor) {
            return true;
        }

        if (!EquipmentManager.equipModLoaded()) {
            return false;
        }

        return !EquipmentManager.findItem(stack -> stack.getItem() instanceof IModularArmor, entity).isEmpty();
    }

    // 这里只判断原版护甲槽胸甲位上是不是原生 modular armor。
    // 这个判断比 hasNativeModularArmor 更窄，适合用在渲染层，避免 Curios 胸甲把普通胸甲位的兼容渲染也一并关掉。
    public static boolean hasNativeChestArmorInChestSlot(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof IModularArmor;
    }

    // 护盾模块允许装在头盔/护腿/靴子，所以这里会优先找胸甲，再退到其它护甲槽。
    public static ItemStack findShieldSourceStack(LivingEntity entity) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (hasShieldController(chest, EquipmentSlot.CHEST)) {
            return chest;
        }

        for (ItemStack stack : getUpgradedArmorItems(entity)) {
            EquipmentSlot slot = UDSHostResolver.inferSlot(stack);
            if (slot != EquipmentSlot.CHEST && hasShieldController(stack, slot)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static void addIfSupported(List<ItemStack> items, ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty() || stack.getItem() instanceof IModularItem) {
            return;
        }
        if (UDSHostResolver.isRuntimeSupported(stack, slot)) {
            items.add(stack);
        }
    }

    // 这个判断既给功能桥使用，也给客户端护盾渲染判断使用，所以保持公开。
    public static boolean hasShieldController(ItemStack stack, EquipmentSlot slot) {
        if (!UDSHostResolver.isRuntimeSupported(stack, slot)) {
            return false;
        }

        try (var host = stack.getCapability(DECapabilities.Host.ITEM)) {
            return host != null && host.getEntitiesByType(ModuleTypes.SHIELD_CONTROLLER).findAny().isPresent();
        }
    }
}
