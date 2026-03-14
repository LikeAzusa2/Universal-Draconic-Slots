package com.likeazusa2.universaldraconicslots.host;

import com.brandon3055.draconicevolution.api.DataComponentAccessor;
import com.brandon3055.draconicevolution.api.modules.lib.ModuleHostImpl;
import net.minecraft.world.item.ItemStack;

public final class UDSHostFactory {
    private UDSHostFactory() {
    }

    public static Host create(ItemStack stack) {
        UDSHostSpec spec = UDSHostResolver.resolve(stack);
        if (spec == null) {
            return null;
        }

        Host host = new Host(spec);
        host.updateDataAccess(DataComponentAccessor.itemStack(stack));
        return host;
    }

    public static final class Host extends ModuleHostImpl {
        private Host(UDSHostSpec spec) {
            super(spec.techLevel(), spec.gridWidth(), spec.gridHeight(), spec.providerName(), false, spec.categories());
            for (var type : spec.additionalTypes()) {
                addAdditionalType(type);
            }
            for (var type : spec.blacklistedTypes()) {
                blackListType(type);
            }
        }
    }
}
