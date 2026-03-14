package com.likeazusa2.universaldraconicslots.client;

import com.likeazusa2.universaldraconicslots.UniversalDraconicSlots;
import com.likeazusa2.universaldraconicslots.host.UDSEquippedItemHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 这里改回使用 DE 原版风格的全身护盾壳。
 * 它不依赖每个护甲槽自己的模型，因此不会再出现腿部缺块、不同槽位覆盖不完整的问题。
 */
public final class UDSShieldRenderBridge {
    private static final UDSShieldBodyModel<LivingEntity> SHIELD_MODEL = new UDSShieldBodyModel<>();

    private UDSShieldRenderBridge() {
    }

    public static void renderArmorOverlayIfNeeded(LivingEntity entity, EquipmentSlot slot, ItemStack armorStack, HumanoidModel<?> originalModel, PoseStack poseStack, MultiBufferSource buffers, int packedLight, float partialTick) {
        if (UDSEquippedItemHelper.hasNativeChestArmorInChestSlot(entity)) {
            logDebug(entity, "skip native modular armor");
            return;
        }

        // 护盾外观只渲一次，统一挂在胸甲这次护甲渲染上。
        // 这样不会因为四个护甲槽都各自叠一层，导致看起来像双层或多层护盾。
        if (slot != EquipmentSlot.CHEST) {
            return;
        }

        ItemStack chestStack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chestStack.isEmpty() || armorStack.isEmpty() || !UDSEquippedItemHelper.hasShieldController(chestStack, EquipmentSlot.CHEST)) {
            logDebug(entity, "skip no chest shield source");
            return;
        }

        SHIELD_MODEL.syncPoseFrom(originalModel);
        SHIELD_MODEL.renderShield(entity, poseStack, buffers, chestStack, packedLight, partialTick);
        logDebug(entity, "render de shield shell slot=" + slot + " item=" + chestStack.getItem());
    }

    private static void logDebug(LivingEntity entity, String message) {
        if (entity.tickCount % 40 == 0) {
            UniversalDraconicSlots.LOGGER.debug("[UDS Shield] {}", message);
        }
    }
}
