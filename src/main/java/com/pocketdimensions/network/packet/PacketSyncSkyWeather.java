package com.pocketdimensions.network.packet;

import com.pocketdimensions.client.PocketClientVisuals;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncSkyWeather {
    private final int sky, weather;

    public PacketSyncSkyWeather(int s, int w) {
        this.sky = s;
        this.weather = w;
    }

    public static void encode(PacketSyncSkyWeather m, FriendlyByteBuf b) {
        b.writeInt(m.sky);
        b.writeInt(m.weather);
    }

    public static PacketSyncSkyWeather decode(FriendlyByteBuf b) {
        return new PacketSyncSkyWeather(b.readInt(), b.readInt());
    }

    public static void handle(PacketSyncSkyWeather m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PocketClientVisuals.setVisuals(m.sky, m.weather)));
        ctx.get().setPacketHandled(true);
    }
}
