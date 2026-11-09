package com.pocketdimensions.client;

import com.pocketdimensions.PocketDimensionsMod;
import com.pocketdimensions.dimension.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = PocketDimensionsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PocketClientVisuals {
    public static int currentSkyTheme = 0;
    public static int currentWeatherVisual = 0;
    private static final Random RANDOM = new Random();

    public static void setVisuals(int s, int w) {
        currentSkyTheme = s;
        currentWeatherVisual = w;
    }

    @SubscribeEvent
    public static void onFog(ViewportEvent.ComputeFogColor e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.dimension().equals(ModDimensions.POCKET_LEVEL)) {
            switch (currentSkyTheme) {
                case 0 -> { e.setRed(0.04F); e.setGreen(0.05F); e.setBlue(0.12F); }
                case 1 -> { e.setRed(0.03F); e.setGreen(0.16F); e.setBlue(0.18F); }
                case 2 -> { e.setRed(0.20F); e.setGreen(0.06F); e.setBlue(0.18F); }
                case 3 -> { e.setRed(0.08F); e.setGreen(0.02F); e.setBlue(0.14F); }
                case 4 -> { e.setRed(0.22F); e.setGreen(0.14F); e.setBlue(0.05F); }
            }
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null && mc.level.dimension().equals(ModDimensions.POCKET_LEVEL)) {
            Player p = mc.player;
            Level l = mc.level;
            if (currentWeatherVisual == 1) {
                for (int i = 0; i < 4; i++) {
                    l.addParticle(ParticleTypes.RAIN, p.getX() + (RANDOM.nextDouble()-0.5)*16, p.getY() + RANDOM.nextDouble()*10+2, p.getZ() + (RANDOM.nextDouble()-0.5)*16, 0, -0.4, 0);
                }
            } else if (currentWeatherVisual == 2) {
                for (int i = 0; i < 3; i++) {
                    l.addParticle(ParticleTypes.SNOWFLAKE, p.getX() + (RANDOM.nextDouble()-0.5)*16, p.getY() + RANDOM.nextDouble()*10+2, p.getZ() + (RANDOM.nextDouble()-0.5)*16, 0, -0.05, 0);
                }
            }
        }
    }
}
