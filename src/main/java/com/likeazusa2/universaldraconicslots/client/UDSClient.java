package com.likeazusa2.universaldraconicslots.client;

import com.likeazusa2.universaldraconicslots.ModContent;
import com.likeazusa2.universaldraconicslots.UniversalDraconicSlots;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = UniversalDraconicSlots.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class UDSClient {
    private UDSClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModContent.HOST_FORGE_MENU.get(), HostForgeScreen::new);
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(UDSKeyMappings.TOGGLE_ENERGY_HUD);
    }
}
