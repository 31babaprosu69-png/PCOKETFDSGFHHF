package com.pocketdimensions.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

public class PocketDimensionEffects extends DimensionSpecialEffects {
    public PocketDimensionEffects() {
        super(Float.NaN, false, SkyType.NORMAL, false, true);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fog, float b) {
        return fog;
    }

    @Override
    public boolean isSunriseOrSunset(float t) {
        return false;
    }
}
