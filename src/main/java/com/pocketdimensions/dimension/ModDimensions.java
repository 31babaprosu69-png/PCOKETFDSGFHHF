package com.pocketdimensions.dimension;

import com.pocketdimensions.PocketDimensionsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ModDimensions {
    public static final ResourceKey<Level> POCKET_LEVEL = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(PocketDimensionsMod.MODID, "pocket")
    );
    public static final ResourceKey<DimensionType> POCKET_TYPE = ResourceKey.create(
            Registries.DIMENSION_TYPE,
            new ResourceLocation(PocketDimensionsMod.MODID, "pocket_type")
    );

    public static void register() {}
}
