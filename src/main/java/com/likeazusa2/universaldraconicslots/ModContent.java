package com.likeazusa2.universaldraconicslots;

import com.likeazusa2.universaldraconicslots.data.UDSHostUpgradeData;
import com.likeazusa2.universaldraconicslots.item.UDSContextAdapterItem;
import com.likeazusa2.universaldraconicslots.machine.HostForgeBlock;
import com.likeazusa2.universaldraconicslots.machine.HostForgeBlockEntity;
import com.likeazusa2.universaldraconicslots.machine.HostForgeMenu;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModContent {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UniversalDraconicSlots.MOD_ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(UniversalDraconicSlots.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, UniversalDraconicSlots.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, UniversalDraconicSlots.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UniversalDraconicSlots.MOD_ID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, UniversalDraconicSlots.MOD_ID);

    public static final DeferredHolder<Item, UDSContextAdapterItem> CONTEXT_ADAPTER_ITEM =
            ITEMS.register("context_adapter_internal", () -> new UDSContextAdapterItem(new Item.Properties()));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UDSHostUpgradeData>> HOST_UPGRADE_DATA =
            DATA_COMPONENTS.registerComponentType(
                    "host_upgrade_data",
                    builder -> builder.persistent(UDSHostUpgradeData.CODEC).networkSynchronized(UDSHostUpgradeData.STREAM_CODEC)
            );

    public static final DeferredBlock<Block> HOST_FORGE =
            BLOCKS.register("host_forge", HostForgeBlock::new);

    public static final DeferredHolder<Item, BlockItem> HOST_FORGE_ITEM =
            ITEMS.register("host_forge", () -> new BlockItem(HOST_FORGE.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HostForgeBlockEntity>> HOST_FORGE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(
                    "host_forge",
                    () -> BlockEntityType.Builder.of(HostForgeBlockEntity::new, HOST_FORGE.get()).build(null)
            );

    public static final DeferredHolder<MenuType<?>, MenuType<HostForgeMenu>> HOST_FORGE_MENU =
            MENUS.register("host_forge", () -> IMenuTypeExtension.create(HostForgeMenu::new));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            CREATIVE_TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.universaldraconicslots"))
                    .icon(() -> new ItemStack(HOST_FORGE_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(HOST_FORGE_ITEM.get());
                    })
                    .build());

    private ModContent() {
    }

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
        BLOCKS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        MENUS.register(modBus);
        CREATIVE_TABS.register(modBus);
        DATA_COMPONENTS.register(modBus);
    }
}
