package com.likeazusa2.universaldraconicslots.host;

import com.brandon3055.draconicevolution.api.modules.ModuleCategory;
import com.brandon3055.draconicevolution.api.modules.ModuleType;
import com.brandon3055.draconicevolution.api.modules.ModuleTypes;
import com.brandon3055.draconicevolution.items.equipment.IModularItem;
import com.likeazusa2.universaldraconicslots.ModContent;
import com.likeazusa2.universaldraconicslots.UDSConfig;
import com.likeazusa2.universaldraconicslots.UniversalDraconicSlots;
import com.likeazusa2.universaldraconicslots.data.UDSHostUpgradeData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class UDSHostResolver {
    // 护甲额外允许的模块类型：这样头盔/护腿/靴子也能挂能量和护盾相关模块。
    private static final List<ModuleType<?>> EXTRA_ARMOR_TYPES = List.of(
            ModuleTypes.ENERGY_STORAGE,
            ModuleTypes.ENERGY_SHARE,
            ModuleTypes.ENERGY_LINK,
            ModuleTypes.SHIELD_CONTROLLER,
            ModuleTypes.SHIELD_BOOST
    );
    private static final List<ModuleType<?>> EXTRA_HANDHELD_TYPES = List.of(
            ModuleTypes.ENERGY_STORAGE,
            ModuleTypes.ENERGY_SHARE,
            ModuleTypes.ENERGY_LINK
    );

    // 这里优先使用原版常见标签，而不是硬编码具体物品类，方便兼容第三方工具和武器。
    private static final TagKey<Item> SWORDS = itemTag("minecraft", "swords");
    private static final TagKey<Item> AXES = itemTag("minecraft", "axes");
    private static final TagKey<Item> PICKAXES = itemTag("minecraft", "pickaxes");
    private static final TagKey<Item> SHOVELS = itemTag("minecraft", "shovels");
    private static final TagKey<Item> HOES = itemTag("minecraft", "hoes");
    private static final TagKey<Item> ENCHANTABLE_BOW = itemTag("minecraft", "enchantable/bow");
    private static final TagKey<Item> ENCHANTABLE_CROSSBOW = itemTag("minecraft", "enchantable/crossbow");

    private UDSHostResolver() {
    }

    // 能否“理论上”充当宿主，只看物品种类，不看当前物品是否已经做过机器改造。
    public static boolean canEverSupportHost(Item item) {
        ItemStack stack = item.getDefaultInstance();
        return !stack.isEmpty() && !(item instanceof IModularItem) && inferSlot(stack) != null;
    }

    // 能否在运行时真正启用宿主，要同时满足：做过改造、配置允许、槽位类型也正确。
    public static boolean isStackSupported(ItemStack stack) {
        return isRuntimeSupported(stack, inferSlot(stack));
    }

    public static boolean isRuntimeSupported(ItemStack stack, EquipmentSlot slot) {
        if (!UDSConfig.ENABLED.get() || stack.isEmpty() || slot == null) {
            return false;
        }
        UDSHostUpgradeData data = getUpgradeData(stack);
        if (data == null || !data.hasHost()) {
            return false;
        }
        if (UDSConfig.REQUIRE_SINGLE_STACK.get() && stack.getMaxStackSize() != 1) {
            return false;
        }
        if ((slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) && !UDSConfig.ALLOW_HANDHELD_ITEMS.get()) {
            return false;
        }
        if ((slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND)
                && UDSConfig.REQUIRE_DURABILITY_FOR_HANDHELD.get()
                && !stack.isDamageableItem()) {
            return false;
        }
        return true;
    }

    public static UDSHostSpec resolve(ItemStack stack) {
        EquipmentSlot slot = inferSlot(stack);
        UDSHostUpgradeData data = getUpgradeData(stack);
        if (!isRuntimeSupported(stack, slot) || data == null) {
            return null;
        }

        List<ModuleCategory> categories = new ArrayList<>();
        switch (slot) {
            case HEAD -> {
                categories.add(ModuleCategory.ARMOR);
                categories.add(ModuleCategory.ARMOR_HEAD);
                return armorSpec(stack, data, categories);
            }
            case CHEST -> {
                categories.add(ModuleCategory.ARMOR);
                categories.add(ModuleCategory.ARMOR_CHEST);
                categories.add(ModuleCategory.CHESTPIECE);
                categories.add(ModuleCategory.ENERGY);
                return chestSpec(stack, data, categories);
            }
            case LEGS -> {
                categories.add(ModuleCategory.ARMOR);
                categories.add(ModuleCategory.ARMOR_LEGS);
                return armorSpec(stack, data, categories);
            }
            case FEET -> {
                categories.add(ModuleCategory.ARMOR);
                categories.add(ModuleCategory.ARMOR_FEET);
                return armorSpec(stack, data, categories);
            }
            case MAINHAND, OFFHAND -> {
                addHandheldCategories(stack, categories);
                categories.add(ModuleCategory.ENERGY);
                return new UDSHostSpec(
                        data.techTier().techLevel(),
                        data.gridWidth(),
                        data.gridHeight(),
                        providerName(stack),
                        EXTRA_HANDHELD_TYPES,
                        List.of(),
                        categories.toArray(ModuleCategory[]::new)
                );
            }
            default -> {
                return null;
            }
        }
    }

    public static EquipmentSlot inferSlot(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        if (stack.getItem() instanceof ArmorItem armorItem) {
            return armorItem.getEquipmentSlot();
        }
        // 手持类统一按主手宿主看待，具体是否在副手运行由实际装备位置决定。
        if (isHandheldTagged(stack)) {
            return EquipmentSlot.MAINHAND;
        }
        return null;
    }

    // 这里集中读取物品上的改造数据，避免其它地方自己拼 component key。
    public static UDSHostUpgradeData getUpgradeData(ItemStack stack) {
        return stack.getOrDefault(ModContent.HOST_UPGRADE_DATA, UDSHostUpgradeData.DISABLED);
    }

    public static boolean isMeleeWeapon(ItemStack stack) {
        return stack.is(SWORDS) || stack.is(AXES);
    }

    public static boolean isMiningTool(ItemStack stack) {
        return stack.is(PICKAXES) || stack.is(AXES) || stack.is(SHOVELS) || stack.is(HOES);
    }

    public static boolean isRangedWeapon(ItemStack stack) {
        return stack.is(ENCHANTABLE_BOW) || stack.is(ENCHANTABLE_CROSSBOW);
    }

    private static UDSHostSpec armorSpec(ItemStack stack, UDSHostUpgradeData data, List<ModuleCategory> categories) {
        return new UDSHostSpec(
                data.techTier().techLevel(),
                data.gridWidth(),
                data.gridHeight(),
                providerName(stack),
                EXTRA_ARMOR_TYPES,
                List.of(),
                categories.toArray(ModuleCategory[]::new)
        );
    }

    private static UDSHostSpec chestSpec(ItemStack stack, UDSHostUpgradeData data, List<ModuleCategory> categories) {
        return new UDSHostSpec(
                data.techTier().techLevel(),
                data.gridWidth(),
                data.gridHeight(),
                providerName(stack),
                EXTRA_ARMOR_TYPES,
                List.of(),
                categories.toArray(ModuleCategory[]::new)
        );
    }

    private static void addHandheldCategories(ItemStack stack, List<ModuleCategory> categories) {
        if (stack.is(SWORDS) || stack.is(AXES)) {
            categories.add(ModuleCategory.MELEE_WEAPON);
        }
        if (stack.is(PICKAXES) || stack.is(AXES) || stack.is(SHOVELS) || stack.is(HOES)) {
            categories.add(ModuleCategory.MINING_TOOL);
        }
        if (stack.is(AXES)) {
            categories.add(ModuleCategory.TOOL_AXE);
        }
        if (stack.is(SHOVELS)) {
            categories.add(ModuleCategory.TOOL_SHOVEL);
        }
        if (stack.is(HOES)) {
            categories.add(ModuleCategory.TOOL_HOE);
        }
        if (stack.is(ENCHANTABLE_BOW) || stack.is(ENCHANTABLE_CROSSBOW)) {
            categories.add(ModuleCategory.RANGED_WEAPON);
        }
    }

    private static String providerName(ItemStack stack) {
        return UniversalDraconicSlots.MOD_ID + ":" + stack.getDescriptionId();
    }

    private static boolean isHandheldTagged(ItemStack stack) {
        return stack.is(SWORDS)
                || stack.is(AXES)
                || stack.is(PICKAXES)
                || stack.is(SHOVELS)
                || stack.is(HOES)
                || stack.is(ENCHANTABLE_BOW)
                || stack.is(ENCHANTABLE_CROSSBOW);
    }

    private static TagKey<Item> itemTag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
