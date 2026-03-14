package com.likeazusa2.universaldraconicslots.client;

import com.likeazusa2.universaldraconicslots.UniversalDraconicSlots;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

public final class UDSKeyMappings {
    public static final String CATEGORY = "key.categories.universaldraconicslots";
    public static final KeyMapping TOGGLE_ENERGY_HUD = new KeyMapping(
            "key.universaldraconicslots.toggle_energy_hud",
            GLFW.GLFW_KEY_H,
            CATEGORY
    );

    private UDSKeyMappings() {
    }

    @EventBusSubscriber(modid = UniversalDraconicSlots.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static final class GameEvents {
        private GameEvents() {
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft minecraft = Minecraft.getInstance();
            while (TOGGLE_ENERGY_HUD.consumeClick()) {
                boolean visible = UDSClientConfig.toggleEnergyHudVisible();
                if (minecraft.player != null) {
                    minecraft.player.displayClientMessage(
                            Component.translatable(
                                    visible
                                            ? "message.universaldraconicslots.energy_hud.enabled"
                                            : "message.universaldraconicslots.energy_hud.disabled"
                            ),
                            true
                    );
                }
            }
        }
    }
}
