package com.likeazusa2.universaldraconicslots.client;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.buffer.VBORenderType;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.render.shader.CCUniform;
import codechicken.lib.vec.Matrix4;
import com.brandon3055.brandonscore.client.model.ExtendedModelPart;
import com.brandon3055.brandonscore.handlers.contributor.ContributorHandler;
import com.brandon3055.brandonscore.handlers.contributor.ContributorProperties;
import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.capability.ModuleHost;
import com.brandon3055.draconicevolution.api.modules.ModuleTypes;
import com.brandon3055.draconicevolution.api.modules.entities.ShieldControlEntity;
import com.brandon3055.draconicevolution.client.DEShaders;
import com.brandon3055.draconicevolution.client.shader.ShieldShader;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Objects;

/**
 * 只渲染护盾壳，不渲染 DE 胸甲本体。
 * 这样别的护甲被改造后，能看到原版风格的护盾外观，但不会被强行套上一整件 DE 胸甲模型。
 */
public class UDSShieldBodyModel<T extends LivingEntity> extends HumanoidModel<T> {
    private static final RenderType SHIELD_RENDER_TYPE = RenderType.create(
            "draconicevolution:armor_shield",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.TRIANGLES,
            256,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(UDSShieldBodyModel::getShieldShader))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .createCompositeState(false)
    );

    private final ModelPart shieldHead;
    private final ModelPart shieldBody;
    private final ModelPart shieldLeftArm;
    private final ModelPart shieldRightArm;
    private final ModelPart shieldLeftLeg;
    private final ModelPart shieldRightLeg;
    private int shieldColour = -1;
    private float shieldState = 0;

    public UDSShieldBodyModel() {
        super(createMesh(new CubeDeformation(1.0F), 0.0F).getRoot().bake(64, 64));

        // 直接复用 DE 胸甲 OBJ 里的护盾部件命名。
        // 如果上游模型路径或 part 名称变化，这里会在加载时直接暴露问题。
        Map<String, CCModel> modelParts = new OBJParser(ResourceLocation.fromNamespaceAndPath("draconicevolution", "models/item/equipment/chestpeice.obj"))
                .ignoreMtl()
                .parse();

        CCModel shieldHeadModel = Objects.requireNonNull(modelParts.get("shield_head")).backfacedCopy();
        CCModel shieldBodyModel = Objects.requireNonNull(modelParts.get("shield_body")).backfacedCopy();
        CCModel shieldRightArmModel = Objects.requireNonNull(modelParts.get("shield_right_arm")).backfacedCopy();
        CCModel shieldLeftArmModel = Objects.requireNonNull(modelParts.get("shield_left_arm")).backfacedCopy();
        CCModel shieldRightLegModel = Objects.requireNonNull(modelParts.get("shield_right_leg")).backfacedCopy();
        CCModel shieldLeftLegModel = Objects.requireNonNull(modelParts.get("shield_left_leg")).backfacedCopy();

        ExtendedModelPart bodyRoot = new ExtendedModelPart();
        bodyRoot.addChild(new ShieldRenderPart(shieldBodyModel));

        this.shieldHead = new ShieldRenderPart(shieldHeadModel);
        this.shieldBody = bodyRoot;
        this.shieldLeftArm = new ShieldRenderPart(shieldLeftArmModel);
        this.shieldRightArm = new ShieldRenderPart(shieldRightArmModel);
        this.shieldLeftLeg = new ShieldRenderPart(shieldLeftLegModel);
        this.shieldRightLeg = new ShieldRenderPart(shieldRightLegModel);
    }

    /**
     * 和 DE 原版胸甲模型一样，从宿主里读取护盾状态和颜色，然后只渲染护盾部件。
     */
    public void renderShield(T entity, PoseStack poseStack, MultiBufferSource buffers, ItemStack stack, int packedLight, float partialTicks) {
        shieldColour = -1;
        shieldState = 0;

        try (ModuleHost host = DECapabilities.getHost(stack)) {
            if (!stack.isEmpty() && host != null) {
                ShieldControlEntity shield = host.getEntitiesByType(ModuleTypes.SHIELD_CONTROLLER)
                        .map(ShieldControlEntity.class::cast)
                        .findAny()
                        .orElse(null);
                if (shield != null) {
                    shieldState = shield.getShieldState();
                    shieldColour = shield.getShieldColour() | 0xFF000000;

                    if (entity instanceof Player player) {
                        ContributorProperties props = ContributorHandler.getProps(player);
                        if (props.hasShieldRGB() && props.getConfig().overrideShield()) {
                            shieldColour = props.getConfig().getShieldColour(partialTicks);
                        }
                    }
                }
            }
        }

        for (ModelPart part : headParts()) {
            renderPart(part, poseStack, buffers, packedLight);
        }
        for (ModelPart part : bodyParts()) {
            renderPart(part, poseStack, buffers, packedLight);
        }
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(shieldHead);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(shieldBody, shieldLeftArm, shieldRightArm, shieldLeftLeg, shieldRightLeg);
    }

    /**
     * 把外部护甲模型当前帧的姿态同步到护盾部件上。
     * 这样头部转向、手臂摆动、腿部走路动作都会跟着实体实时变化。
     */
    public void syncPoseFrom(HumanoidModel<?> model) {
        copyPose(model.head, shieldHead);
        copyPose(model.body, shieldBody);
        copyPose(model.leftArm, shieldLeftArm);
        copyPose(model.rightArm, shieldRightArm);
        copyPose(model.leftLeg, shieldLeftLeg);
        copyPose(model.rightLeg, shieldRightLeg);
        setAllVisible(true);
    }

    private void copyPose(ModelPart source, ModelPart target) {
        target.x = source.x;
        target.y = source.y;
        target.z = source.z;
        target.xRot = source.xRot;
        target.yRot = source.yRot;
        target.zRot = source.zRot;
        target.xScale = source.xScale;
        target.yScale = source.yScale;
        target.zScale = source.zScale;
    }

    private static codechicken.lib.render.shader.CCShaderInstance getShieldShader() {
        return DEShaders.CHESTPIECE_SHIELD_SHADER.getShaderInstance();
    }

    private void renderPart(ModelPart part, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        ((ExtendedModelPart) part).render(poseStack, buffers, packedLight, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
    }

    /**
     * 这里直接复制 DE 里 ShieldModelPart 的做法，只是把外层模型换成了我们自己的轻量版本。
     */
    private class ShieldRenderPart extends ExtendedModelPart {
        private final VBORenderType renderType;

        private ShieldRenderPart(CCModel model) {
            this.renderType = new VBORenderType(SHIELD_RENDER_TYPE, (VertexFormat format, BufferBuilder builder) -> {
                CCRenderState ccrs = CCRenderState.instance();
                ccrs.reset();
                ccrs.bind(builder, format);
                model.render(ccrs, new IVertexOperation[0]);
            });
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
            if (shieldState <= 0) {
                return;
            }

            poseStack.pushPose();
            translateAndRotate(poseStack);
            Matrix4 mat = new Matrix4(poseStack);
            int colour = shieldColour;
            float state = shieldState;
            renderType.withCallback(() -> applyShieldUniforms(colour, state, mat)).draw(buffers);
            poseStack.popPose();
        }

        private void applyShieldUniforms(int colour, float state, Matrix4 mat) {
            // 不走原版胸甲渲染链时，需要手动把颜色、激活度和模型矩阵写回 DE 的护盾 shader。
            ShieldShader shader = DEShaders.CHESTPIECE_SHIELD_SHADER;
            CCUniform baseColour = shader.getBaseColourUniform();
            baseColour.glUniform4f(
                    ((colour >> 16) & 0xFF) / 255.0F,
                    ((colour >> 8) & 0xFF) / 255.0F,
                    (colour & 0xFF) / 255.0F,
                    ((colour >> 24) & 0xFF) / 255.0F
            );
            shader.getActivationUniform().glUniform1f(state);
            shader.getModelMatUniform().glUniformMatrix4f(mat);
        }
    }
}
