package com.pocketdimensions.client;

import com.pocketdimensions.PocketDimensionsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = PocketDimensionsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(new ResourceLocation(PocketDimensionsMod.MODID, "pocket_effects"), new PocketDimensionEffects());
    }

    public static void openSettingsScreen(UUID u, String n, boolean c, int s, int w, boolean pub, List<String> al, boolean op) {
        Minecraft.getInstance().setScreen(new PocketSettingsScreen(u, n, c, s, w, pub, al, op));
    }
}
