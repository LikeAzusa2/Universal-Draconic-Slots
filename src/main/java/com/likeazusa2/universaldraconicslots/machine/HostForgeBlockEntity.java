package com.likeazusa2.universaldraconicslots.machine;

import com.brandon3055.brandonscore.api.power.OPStorage;
import com.likeazusa2.universaldraconicslots.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class HostForgeBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_TARGET = 0;
    public static final int SLOT_TECH_CORE = 1;
    public static final int SLOT_WIDTH = 2;
    public static final int SLOT_HEIGHT = 3;
    public static final int SLOT_OP_CORE = 4;

    private static final int ENERGY_CAPACITY = 512_000_000;
    private static final int ENERGY_RECEIVE = 16_000_000;

    private final ItemStackHandler inventory = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case SLOT_TARGET -> HostForgeLogic.isTransformableTarget(stack);
                case SLOT_TECH_CORE -> HostForgeLogic.resolveTechTier(stack) != null;
                case SLOT_WIDTH -> HostForgeLogic.isWidthMaterial(stack);
                case SLOT_HEIGHT -> HostForgeLogic.isHeightMaterial(stack);
                case SLOT_OP_CORE -> HostForgeLogic.resolveOpTier(stack) != null;
                default -> false;
            };
        }

        @Override
        public int getSlotLimit(int slot) {
            return switch (slot) {
                case SLOT_TECH_CORE, SLOT_OP_CORE -> 1;
                default -> super.getSlotLimit(slot);
            };
        }
    };

    private final OPStorage energy = new OPStorage(ENERGY_CAPACITY, ENERGY_RECEIVE, 0)
            .setReceiveOnly()
            .setChangeListener(this::setChanged);
    private int feedbackCode = HostForgeMenu.FEEDBACK_NONE;
    private int feedbackNonce = 0;

    public HostForgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModContent.HOST_FORGE_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.universaldraconicslots.host_forge");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new HostForgeMenu(containerId, inventory, this);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public OPStorage getEnergyStorage() {
        return energy;
    }

    public SimpleContainer getDrops() {
        SimpleContainer container = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            container.setItem(i, inventory.getStackInSlot(i));
        }
        return container;
    }

    public boolean applyUpgrade(Player player) {
        if (level == null || level.isClientSide) {
            return false;
        }
        ItemStack target = inventory.getStackInSlot(SLOT_TARGET);
        if (!HostForgeLogic.isTransformableTarget(target)) {
            pushFeedback("message.universaldraconicslots.host_forge.invalid_target");
            return false;
        }

        HostForgeLogic.Plan plan = HostForgeLogic.buildPlan(target, inventory);
        if (!plan.valid()) {
            pushFeedback(plan.failureKey());
            return false;
        }
        if (energy.getEnergyStored() < plan.energyCost()) {
            pushFeedback("message.universaldraconicslots.host_forge.not_enough_energy");
            return false;
        }

        energy.modifyEnergyStored(-plan.energyCost());
        if (plan.consumeTechCore()) {
            inventory.extractItem(SLOT_TECH_CORE, 1, false);
        }
        if (plan.consumeOpCore()) {
            inventory.extractItem(SLOT_OP_CORE, 1, false);
        }
        if (plan.widthConsumed() > 0) {
            inventory.extractItem(SLOT_WIDTH, plan.widthConsumed(), false);
        }
        if (plan.heightConsumed() > 0) {
            inventory.extractItem(SLOT_HEIGHT, plan.heightConsumed(), false);
        }

        target.set(ModContent.HOST_UPGRADE_DATA, plan.result());
        inventory.setStackInSlot(SLOT_TARGET, target);
        clearFeedback();
        setChanged();
        player.sendSystemMessage(Component.translatable("message.universaldraconicslots.host_forge.success"));
        return true;
    }

    public int getFeedbackCode() {
        return feedbackCode;
    }

    public int getFeedbackNonce() {
        return feedbackNonce;
    }

    private void pushFeedback(String translationKey) {
        feedbackCode = HostForgeMenu.feedbackCodeForKey(translationKey);
        feedbackNonce++;
        setChanged();
    }

    private void clearFeedback() {
        feedbackCode = HostForgeMenu.FEEDBACK_NONE;
        feedbackNonce++;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.put("energy", energy.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        if (tag.contains("energy")) {
            energy.deserializeNBT(registries, tag.getCompound("energy"));
        }
    }
}
