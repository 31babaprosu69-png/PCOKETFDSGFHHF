package com.pocketdimensions.network;

import com.pocketdimensions.PocketDimensionsMod;
import com.pocketdimensions.network.packet.PacketDeletePocket;
import com.pocketdimensions.network.packet.PacketOpenSettings;
import com.pocketdimensions.network.packet.PacketSyncSkyWeather;
import com.pocketdimensions.network.packet.PacketUpdateSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PocketDimensionsMod.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(PacketOpenSettings.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketOpenSettings::encode).decoder(PacketOpenSettings::decode).consumerMainThread(PacketOpenSettings::handle).add();
        CHANNEL.messageBuilder(PacketUpdateSettings.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketUpdateSettings::encode).decoder(PacketUpdateSettings::decode).consumerMainThread(PacketUpdateSettings::handle).add();
        CHANNEL.messageBuilder(PacketDeletePocket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketDeletePocket::encode).decoder(PacketDeletePocket::decode).consumerMainThread(PacketDeletePocket::handle).add();
        CHANNEL.messageBuilder(PacketSyncSkyWeather.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketSyncSkyWeather::encode).decoder(PacketSyncSkyWeather::decode).consumerMainThread(PacketSyncSkyWeather::handle).add();
    }
}
