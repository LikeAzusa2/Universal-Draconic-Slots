package com.likeazusa2.universaldraconicslots.mixin;

import com.brandon3055.brandonscore.client.model.EquippedItemModelLayer;
import com.likeazusa2.universaldraconicslots.client.UDSShieldRenderBridge;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 把升级装备的护盾壳挂到 Brandon's Core 的装备模型层尾部。
 * 这里可以拿到最终生效的父模型姿态，护盾外壳就能跟随其他模组自己的护甲模型一起运动。
 */
@Mixin(value = EquippedItemModelLayer.class, remap = false)
public abstract class MixinEquippedItemModelLayer<T extends LivingEntity, M extends HumanoidModel<T>> {
    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("TAIL")
    )
    private void uds$renderUpgradedShield(PoseStack poseStack, MultiBufferSource buffers, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        ItemStack chestStack = entity.getItemBySlot(EquipmentSlot.CHEST);
        HumanoidModel<?> model = ((RenderLayer<T, M>) (Object) this).getParentModel();
        UDSShieldRenderBridge.renderArmorOverlayIfNeeded(entity, EquipmentSlot.CHEST, chestStack, model, poseStack, buffers, packedLight, partialTicks);
    }
}
