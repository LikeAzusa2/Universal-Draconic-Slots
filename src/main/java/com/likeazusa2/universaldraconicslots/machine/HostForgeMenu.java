package com.likeazusa2.universaldraconicslots.machine;

import com.likeazusa2.universaldraconicslots.ModContent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class HostForgeMenu extends AbstractContainerMenu {
    public static final int APPLY_BUTTON_ID = 0;
    public static final int FEEDBACK_NONE = 0;
    public static final int FEEDBACK_INVALID_TARGET = 1;
    public static final int FEEDBACK_NEED_TECH_CORE = 2;
    public static final int FEEDBACK_INVALID_SETUP = 3;
    public static final int FEEDBACK_NEED_OP_CORE = 4;
    public static final int FEEDBACK_MISMATCHED_OP_CORE = 5;
    public static final int FEEDBACK_NO_CHANGES = 6;
    public static final int FEEDBACK_NOT_ENOUGH_ENERGY = 7;

    private final HostForgeBlockEntity blockEntity;
    private final ContainerData data;

    public HostForgeMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, (HostForgeBlockEntity) inventory.player.level().getBlockEntity(buffer.readBlockPos()));
    }

    public HostForgeMenu(int containerId, Inventory inventory, HostForgeBlockEntity blockEntity) {
        super(ModContent.HOST_FORGE_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = new ContainerData() {
            private final int[] values = new int[4];

            @Override
            public int get(int index) {
                if (blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide) {
                    return switch (index) {
                        case 0 -> (int) blockEntity.getEnergyStorage().getOPStored();
                        case 1 -> (int) blockEntity.getEnergyStorage().getMaxOPStored();
                        case 2 -> blockEntity.getFeedbackCode();
                        case 3 -> blockEntity.getFeedbackNonce();
                        default -> 0;
                    };
                }
                return values[index];
            }

            @Override
            public void set(int index, int value) {
                values[index] = value;
            }

            @Override
            public int getCount() {
                return values.length;
            }
        };

        addSlot(new SlotItemHandler(blockEntity.getInventory(), HostForgeBlockEntity.SLOT_TARGET, 80, 42));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), HostForgeBlockEntity.SLOT_TECH_CORE, 80, 12));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), HostForgeBlockEntity.SLOT_WIDTH, 26, 42));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), HostForgeBlockEntity.SLOT_HEIGHT, 134, 42));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), HostForgeBlockEntity.SLOT_OP_CORE, 80, 72));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 108 + row * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(inventory, i, 8 + i * 18, 166));
        }

        addDataSlots(data);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == APPLY_BUTTON_ID) {
            return blockEntity.applyUpgrade(player);
        }
        return super.clickMenuButton(player, id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copied = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copied = stack.copy();
            if (index < 5) {
                if (!moveItemStackTo(stack, 5, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, 5, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return copied;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(net.minecraft.world.inventory.ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, blockEntity.getBlockState().getBlock());
    }

    public int getEnergyStored() {
        return data.get(0);
    }

    public int getMaxEnergyStored() {
        return data.get(1);
    }

    public int getPreviewCost() {
        return HostForgeLogic.buildPlan(blockEntity.getInventory().getStackInSlot(HostForgeBlockEntity.SLOT_TARGET), blockEntity.getInventory()).energyCost();
    }

    public int getFeedbackCode() {
        return data.get(2);
    }

    public int getFeedbackNonce() {
        return data.get(3);
    }

    public Component getFeedbackMessage() {
        return switch (getFeedbackCode()) {
            case FEEDBACK_INVALID_TARGET -> Component.translatable("message.universaldraconicslots.host_forge.invalid_target");
            case FEEDBACK_NEED_TECH_CORE -> Component.translatable("message.universaldraconicslots.host_forge.need_tech_core");
            case FEEDBACK_INVALID_SETUP -> Component.translatable("message.universaldraconicslots.host_forge.invalid_setup");
            case FEEDBACK_NEED_OP_CORE -> Component.translatable("message.universaldraconicslots.host_forge.need_op_core");
            case FEEDBACK_MISMATCHED_OP_CORE -> Component.translatable("message.universaldraconicslots.host_forge.mismatched_op_core");
            case FEEDBACK_NO_CHANGES -> Component.translatable("message.universaldraconicslots.host_forge.no_changes");
            case FEEDBACK_NOT_ENOUGH_ENERGY -> Component.translatable("message.universaldraconicslots.host_forge.not_enough_energy");
            default -> Component.empty();
        };
    }

    public static int feedbackCodeForKey(String translationKey) {
        return switch (translationKey) {
            case "message.universaldraconicslots.host_forge.invalid_target" -> FEEDBACK_INVALID_TARGET;
            case "message.universaldraconicslots.host_forge.need_tech_core" -> FEEDBACK_NEED_TECH_CORE;
            case "message.universaldraconicslots.host_forge.invalid_setup" -> FEEDBACK_INVALID_SETUP;
            case "message.universaldraconicslots.host_forge.need_op_core" -> FEEDBACK_NEED_OP_CORE;
            case "message.universaldraconicslots.host_forge.mismatched_op_core" -> FEEDBACK_MISMATCHED_OP_CORE;
            case "message.universaldraconicslots.host_forge.no_changes" -> FEEDBACK_NO_CHANGES;
            case "message.universaldraconicslots.host_forge.not_enough_energy" -> FEEDBACK_NOT_ENOUGH_ENERGY;
            default -> FEEDBACK_NONE;
        };
    }
}
