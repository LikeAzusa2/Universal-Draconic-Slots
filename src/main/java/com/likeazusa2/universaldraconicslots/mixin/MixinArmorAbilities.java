package com.likeazusa2.universaldraconicslots.mixin;

import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.draconicevolution.api.capability.ModuleHost;
import com.brandon3055.draconicevolution.api.modules.data.SpeedData;
import com.brandon3055.draconicevolution.api.modules.entities.FlightEntity;
import com.brandon3055.draconicevolution.handlers.ModularArmorEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Supplier;

@Mixin(value = ModularArmorEventHandler.ArmorAbilities.class, remap = false)
public interface MixinArmorAbilities {
    @Invoker("addSpeedData")
    void uds$addSpeedData(SpeedData data, ModuleHost host);

    @Invoker("addFlightData")
    void uds$addFlightData(FlightEntity entity, Supplier<IOPStorage> storage);
}
