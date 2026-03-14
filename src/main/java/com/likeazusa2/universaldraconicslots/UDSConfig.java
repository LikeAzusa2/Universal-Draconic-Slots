package com.likeazusa2.universaldraconicslots;

import com.brandon3055.brandonscore.api.TechLevel;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class UDSConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.BooleanValue ALLOW_HANDHELD_ITEMS;
    public static final ModConfigSpec.BooleanValue REQUIRE_SINGLE_STACK;
    public static final ModConfigSpec.BooleanValue REQUIRE_DURABILITY_FOR_HANDHELD;
    public static final ModConfigSpec.BooleanValue REPAIR_DURABILITY_WITH_OP;
    public static final ModConfigSpec.EnumValue<TechLevel> DEFAULT_TECH_LEVEL;
    public static final ModConfigSpec.IntValue ARMOR_GRID_WIDTH;
    public static final ModConfigSpec.IntValue ARMOR_GRID_HEIGHT;
    public static final ModConfigSpec.IntValue CHEST_GRID_WIDTH;
    public static final ModConfigSpec.IntValue CHEST_GRID_HEIGHT;
    public static final ModConfigSpec.IntValue HANDHELD_GRID_WIDTH;
    public static final ModConfigSpec.IntValue HANDHELD_GRID_HEIGHT;
    public static final ModConfigSpec.IntValue WYVERN_MAX_GRID;
    public static final ModConfigSpec.IntValue DRACONIC_MAX_GRID;
    public static final ModConfigSpec.IntValue CHAOTIC_MAX_GRID;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("general");
        ENABLED = builder
                .comment("Master switch for attaching DE module hosts to supported items.")
                .define("enabled", true);
        ALLOW_HANDHELD_ITEMS = builder
                .comment("If true, handheld tools and weapons also receive a module host.")
                .define("allowHandheldItems", true);
        REQUIRE_SINGLE_STACK = builder
                .comment("If true, only max-stack-size-1 items can receive a module host.")
                .define("requireSingleStack", true);
        REQUIRE_DURABILITY_FOR_HANDHELD = builder
                .comment("If true, handheld items must be damageable to qualify.")
                .define("requireDurabilityForHandheld", true);
        REPAIR_DURABILITY_WITH_OP = builder
                .comment("If true, upgraded damageable items repair themselves by consuming OP.")
                .define("repairDurabilityWithOP", true);
        DEFAULT_TECH_LEVEL = builder
                .comment("Fallback host tech level for all generated module hosts.")
                .defineEnum("defaultTechLevel", TechLevel.DRACONIC);
        builder.pop();

        builder.push("grid");
        ARMOR_GRID_WIDTH = builder.defineInRange("armorWidth", 4, 1, 16);
        ARMOR_GRID_HEIGHT = builder.defineInRange("armorHeight", 4, 1, 16);
        CHEST_GRID_WIDTH = builder.defineInRange("chestWidth", 6, 1, 16);
        CHEST_GRID_HEIGHT = builder.defineInRange("chestHeight", 5, 1, 16);
        HANDHELD_GRID_WIDTH = builder.defineInRange("handheldWidth", 5, 1, 16);
        HANDHELD_GRID_HEIGHT = builder.defineInRange("handheldHeight", 5, 1, 16);
        builder.pop();

        builder.push("host_forge");
        WYVERN_MAX_GRID = builder
                .comment("Maximum width and height that Host Forge can reach with a Wyvern core.")
                .defineInRange("wyvernMaxGrid", 4, 1, 16);
        DRACONIC_MAX_GRID = builder
                .comment("Maximum width and height that Host Forge can reach with a Draconic core.")
                .defineInRange("draconicMaxGrid", 8, 1, 16);
        CHAOTIC_MAX_GRID = builder
                .comment("Maximum width and height that Host Forge can reach with a Chaotic core.")
                .defineInRange("chaoticMaxGrid", 16, 1, 16);
        builder.pop();
        COMMON_SPEC = builder.build();
    }

    private UDSConfig() {
    }
}
