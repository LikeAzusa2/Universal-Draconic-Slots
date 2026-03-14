package com.likeazusa2.universaldraconicslots.host;

import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.draconicevolution.api.modules.ModuleCategory;
import com.brandon3055.draconicevolution.api.modules.ModuleType;

import java.util.List;

public record UDSHostSpec(
        TechLevel techLevel,
        int gridWidth,
        int gridHeight,
        String providerName,
        List<ModuleType<?>> additionalTypes,
        List<ModuleType<?>> blacklistedTypes,
        ModuleCategory... categories
) {
    public UDSHostSpec(
            TechLevel techLevel,
            int gridWidth,
            int gridHeight,
            String providerName,
            ModuleCategory... categories
    ) {
        this(techLevel, gridWidth, gridHeight, providerName, List.of(), List.of(), categories);
    }
}
