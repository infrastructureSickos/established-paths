package com.infrastructuresickos.established_paths;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class EPConfig {
    public static final ForgeConfigSpec SPEC;
    public static final EPConfig INSTANCE;

    static {
        Pair<EPConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(EPConfig::new);
        INSTANCE = specPair.getLeft();
        SPEC = specPair.getRight();
    }

    public final ForgeConfigSpec.DoubleValue sprintChance;
    public final ForgeConfigSpec.DoubleValue walkChance;

    private EPConfig(ForgeConfigSpec.Builder builder) {
        builder.push("wear");
        sprintChance = builder.comment("Chance per step to degrade a block while sprinting (0.0–1.0)")
                              .defineInRange("sprintChance", 0.50, 0.0, 1.0);
        walkChance = builder.comment("Chance per step to degrade a block while walking (0.0–1.0)")
                            .defineInRange("walkChance", 0.25, 0.0, 1.0);
        builder.pop();
    }
}
