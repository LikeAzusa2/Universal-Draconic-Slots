package com.likeazusa2.universaldraconicslots;

import com.likeazusa2.universaldraconicslots.client.UDSClientConfig;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(UniversalDraconicSlots.MOD_ID)
public class UniversalDraconicSlots {
    public static final String MOD_ID = "universaldraconicslots";
    public static final Logger LOGGER = LogUtils.getLogger();

    public UniversalDraconicSlots(IEventBus modBus, ModContainer container) {
        ModContent.init(modBus);
        container.registerConfig(ModConfig.Type.COMMON, UDSConfig.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, UDSClientConfig.CLIENT_SPEC);
        modBus.addListener(UDSCapabilities::registerCapabilities);
        NetworkHandler.init(modBus);

        NeoForge.EVENT_BUS.register(UDSEquipmentTicker.class);
        NeoForge.EVENT_BUS.register(UDSArmorEventBridge.class);
        NeoForge.EVENT_BUS.register(UDSHandheldEventBridge.class);
    }
}
