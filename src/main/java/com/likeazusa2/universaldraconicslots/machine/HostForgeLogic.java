package com.likeazusa2.universaldraconicslots.machine;

import com.brandon3055.draconicevolution.init.DEContent;
import com.brandon3055.draconicevolution.items.equipment.IModularItem;
import com.likeazusa2.universaldraconicslots.data.UDSHostUpgradeData;
import com.likeazusa2.universaldraconicslots.data.UDSTier;
import com.likeazusa2.universaldraconicslots.host.UDSHostResolver;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class HostForgeLogic {
    private HostForgeLogic() {
    }

    // 三档科技核心对应三套不同的扩展材料和网格上限。
    public static boolean isTransformableTarget(ItemStack stack) {
        return !stack.isEmpty()
                && !(stack.getItem() instanceof IModularItem)
                && UDSHostResolver.canEverSupportHost(stack.getItem());
    }

    public static boolean isWidthMaterial(ItemStack stack) {
        return stack.is(DEContent.CORE_DRACONIUM.get())
                || stack.is(DEContent.ITEM_AWAKENED_DRACONIUM_BLOCK.get())
                || stack.is(DEContent.CHAOS_SHARD.get());
    }

    public static boolean isHeightMaterial(ItemStack stack) {
        return stack.is(DEContent.MODULE_CORE.get())
                || stack.is(DEContent.ENERGY_CORE_WYVERN.get())
                || stack.is(DEContent.ENERGY_CORE_DRACONIC.get());
    }

    public static UDSTier resolveTechTier(ItemStack stack) {
        if (stack.is(DEContent.CORE_WYVERN.get())) {
            return UDSTier.WYVERN;
        }
        if (stack.is(DEContent.CORE_AWAKENED.get())) {
            return UDSTier.DRACONIC;
        }
        if (stack.is(DEContent.CORE_CHAOTIC.get())) {
            return UDSTier.CHAOTIC;
        }
        return null;
    }

    public static UDSTier resolveOpTier(ItemStack stack) {
        if (stack.is(DEContent.ENERGY_CORE_WYVERN.get())) {
            return UDSTier.WYVERN;
        }
        if (stack.is(DEContent.ENERGY_CORE_DRACONIC.get())) {
            return UDSTier.DRACONIC;
        }
        if (stack.is(DEContent.ENERGY_CORE_CHAOTIC.get())) {
            return UDSTier.CHAOTIC;
        }
        return null;
    }

    /**
     * 根据目标物品当前的改造状态和锻造台输入槽内容，生成一次完整升级方案。
     * 这里只做校验和规划，不直接修改物品、物品栏或能量。
     */
    public static Plan buildPlan(ItemStack target, ItemStackHandler inventory) {
        UDSHostUpgradeData current = UDSHostResolver.getUpgradeData(target);
        UDSTier techTier = current.hasHost() ? current.techTier() : null;
        int width = current.hasHost() ? current.gridWidth() : 1;
        int height = current.hasHost() ? current.gridHeight() : 1;
        boolean opEnabled = current.opEnabled();
        UDSTier opTier = current.opEnabled() ? current.opTier() : UDSTier.WYVERN;
        boolean consumeTechCore = false;
        boolean consumeOpCore = false;

        UDSTier suppliedTech = resolveTechTier(inventory.getStackInSlot(HostForgeBlockEntity.SLOT_TECH_CORE));
        if (!current.hasHost()) {
            if (suppliedTech == null) {
                return Plan.invalid("message.universaldraconicslots.host_forge.need_tech_core");
            }
            techTier = suppliedTech;
            consumeTechCore = true;
        } else if (suppliedTech != null && suppliedTech.isAtLeast(techTier) && suppliedTech != techTier) {
            techTier = suppliedTech;
            consumeTechCore = true;
        }

        if (techTier == null) {
            return Plan.invalid("message.universaldraconicslots.host_forge.invalid_setup");
        }

        ItemStack widthStack = inventory.getStackInSlot(HostForgeBlockEntity.SLOT_WIDTH);
        ItemStack heightStack = inventory.getStackInSlot(HostForgeBlockEntity.SLOT_HEIGHT);
        if (!widthStack.isEmpty() && !matchesWidthMaterial(widthStack, techTier)) {
            return Plan.invalid("message.universaldraconicslots.host_forge.invalid_setup");
        }
        if (!heightStack.isEmpty() && !matchesHeightMaterial(heightStack, techTier)) {
            return Plan.invalid("message.universaldraconicslots.host_forge.invalid_setup");
        }

        int maxGrid = maxGridSize(techTier);
        int widthAdd = Math.min(maxGrid - width, widthStack.getCount());
        int heightAdd = Math.min(maxGrid - height, heightStack.getCount());
        width += widthAdd;
        height += heightAdd;

        UDSTier suppliedOp = resolveOpTier(inventory.getStackInSlot(HostForgeBlockEntity.SLOT_OP_CORE));
        // 新开宿主或升级 tech tier 时，要求同时放入同档位能量核心，
        // 避免出现宿主档位已提升但独立 OP 侧仍停留在旧状态的半升级结果。
        if ((!current.hasHost() || consumeTechCore) && suppliedOp == null) {
            return Plan.invalid("message.universaldraconicslots.host_forge.need_op_core");
        }
        if (suppliedOp != null && techTier != null && suppliedOp != techTier) {
            return Plan.invalid("message.universaldraconicslots.host_forge.mismatched_op_core");
        }
        if (suppliedOp != null && (!opEnabled || suppliedOp.isAtLeast(opTier) && suppliedOp != opTier)) {
            opEnabled = true;
            opTier = suppliedOp;
            consumeOpCore = true;
        }

        if (width <= 0 || height <= 0) {
            return Plan.invalid("message.universaldraconicslots.host_forge.invalid_setup");
        }
        if (!consumeTechCore && !consumeOpCore && widthAdd == 0 && heightAdd == 0 && current.hasHost()) {
            return Plan.invalid("message.universaldraconicslots.host_forge.no_changes");
        }

        int energyCost = 500_000
                + widthAdd * 200_000
                + heightAdd * 200_000
                + techEnergyCost(techTier, current.hasHost() ? current.techTier() : null)
                + opEnergyCost(opTier, consumeOpCore);

        return new Plan(
                true,
                new UDSHostUpgradeData(true, techTier, width, height, opEnabled, opTier),
                widthAdd,
                heightAdd,
                consumeTechCore,
                consumeOpCore,
                energyCost,
                ""
        );
    }

    private static int techEnergyCost(UDSTier newTier, UDSTier oldTier) {
        if (oldTier == newTier) {
            return 0;
        }
        return switch (newTier) {
            case WYVERN -> 1_000_000;
            case DRACONIC -> 8_000_000;
            case CHAOTIC -> 64_000_000;
        };
    }

    private static int opEnergyCost(UDSTier opTier, boolean consumeOpCore) {
        if (!consumeOpCore) {
            return 0;
        }
        return switch (opTier) {
            case WYVERN -> 2_000_000;
            case DRACONIC -> 8_000_000;
            case CHAOTIC -> 32_000_000;
        };
    }

    private static boolean matchesWidthMaterial(ItemStack stack, UDSTier techTier) {
        return switch (techTier) {
            case WYVERN -> stack.is(DEContent.CORE_DRACONIUM.get());
            case DRACONIC -> stack.is(DEContent.ITEM_AWAKENED_DRACONIUM_BLOCK.get());
            case CHAOTIC -> stack.is(DEContent.CHAOS_SHARD.get());
        };
    }

    private static boolean matchesHeightMaterial(ItemStack stack, UDSTier techTier) {
        return switch (techTier) {
            case WYVERN -> stack.is(DEContent.MODULE_CORE.get());
            case DRACONIC -> stack.is(DEContent.ENERGY_CORE_WYVERN.get());
            case CHAOTIC -> stack.is(DEContent.ENERGY_CORE_DRACONIC.get());
        };
    }

    private static int maxGridSize(UDSTier techTier) {
        return switch (techTier) {
            case WYVERN -> com.likeazusa2.universaldraconicslots.UDSConfig.WYVERN_MAX_GRID.get();
            case DRACONIC -> com.likeazusa2.universaldraconicslots.UDSConfig.DRACONIC_MAX_GRID.get();
            case CHAOTIC -> com.likeazusa2.universaldraconicslots.UDSConfig.CHAOTIC_MAX_GRID.get();
        };
    }

    public record Plan(
            boolean valid,
            UDSHostUpgradeData result,
            int widthConsumed,
            int heightConsumed,
            boolean consumeTechCore,
            boolean consumeOpCore,
            int energyCost,
            String failureKey
    ) {
        public static Plan invalid(String failureKey) {
            return new Plan(false, UDSHostUpgradeData.DISABLED, 0, 0, false, false, 0, failureKey);
        }
    }
}
