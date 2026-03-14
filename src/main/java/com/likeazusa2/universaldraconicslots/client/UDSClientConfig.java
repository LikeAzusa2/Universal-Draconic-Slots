package com.likeazusa2.universaldraconicslots.client;

import com.likeazusa2.universaldraconicslots.UniversalDraconicSlots;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class UDSClientConfig {
    private static final boolean DEFAULT_SHOW_ENERGY_HUD = true;
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec.BooleanValue SHOW_ENERGY_HUD_BY_DEFAULT;

    private static boolean energyHudVisible;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("hud");
        SHOW_ENERGY_HUD_BY_DEFAULT = builder
                .comment("If true, the left-side upgraded item energy HUD is visible by default.")
                .define("showEnergyHudByDefault", DEFAULT_SHOW_ENERGY_HUD);
        builder.pop();
        CLIENT_SPEC = builder.build();
        energyHudVisible = DEFAULT_SHOW_ENERGY_HUD;
    }

    private UDSClientConfig() {
    }

    public static boolean isEnergyHudVisible() {
        return energyHudVisible;
    }

    public static boolean toggleEnergyHudVisible() {
        energyHudVisible = !energyHudVisible;
        return energyHudVisible;
    }

    private static void syncRuntimeState() {
        energyHudVisible = SHOW_ENERGY_HUD_BY_DEFAULT.get();
    }

    @EventBusSubscriber(modid = UniversalDraconicSlots.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Events {
        private Events() {
        }

        @SubscribeEvent
        public static void onConfigLoading(ModConfigEvent.Loading event) {
            syncIfClient(event.getConfig());
        }

        @SubscribeEvent
        public static void onConfigReloading(ModConfigEvent.Reloading event) {
            syncIfClient(event.getConfig());
        }

        private static void syncIfClient(ModConfig config) {
            if (config.getSpec() == CLIENT_SPEC) {
                syncRuntimeState();
            }
        }
    }
}
