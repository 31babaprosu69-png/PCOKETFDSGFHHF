package com.pocketdimensions.network.packet;

import com.pocketdimensions.dimension.ModDimensions;
import com.pocketdimensions.dimension.PocketData;
import com.pocketdimensions.dimension.PocketSavedData;
import com.pocketdimensions.network.ModNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketUpdateSettings {
    private final UUID owner;
    private final boolean chunkLoader, isPublic;
    private final int sky, weather;
    private final String addName, remName;

    public PacketUpdateSettings(UUID o, boolean c, int s, int w, boolean pub, String add, String rem) {
        this.owner = o;
        this.chunkLoader = c;
        this.sky = s;
        this.weather = w;
        this.isPublic = pub;
        this.addName = add != null ? add : "";
        this.remName = rem != null ? rem : "";
    }

    public static void encode(PacketUpdateSettings m, FriendlyByteBuf b) {
        b.writeUUID(m.owner);
        b.writeBoolean(m.chunkLoader);
        b.writeInt(m.sky);
        b.writeInt(m.weather);
        b.writeBoolean(m.isPublic);
        b.writeUtf(m.addName);
        b.writeUtf(m.remName);
    }

    public static PacketUpdateSettings decode(FriendlyByteBuf b) {
        return new PacketUpdateSettings(
                b.readUUID(),
                b.readBoolean(),
                b.readInt(),
                b.readInt(),
                b.readBoolean(),
                b.readUtf(),
                b.readUtf()
        );
    }

    public static void handle(PacketUpdateSettings m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;
            if (!sender.getUUID().equals(m.owner) && !sender.hasPermissions(2)) {
                sender.sendSystemMessage(Component.translatable("message.pocketdimensions.no_permission"));
                return;
            }
            PocketSavedData sd = PocketSavedData.get(sender.server);
            PocketData pd = sd.getPocketByOwner(m.owner);
            if (pd == null) return;

            if (pd.isChunkLoaderActive() != m.chunkLoader) {
                sd.setChunkLoader(m.owner, m.chunkLoader, sender.server);
            }
            pd.setSkyTheme(m.sky);
            pd.setWeatherVisual(m.weather);
            pd.setPublic(m.isPublic);

            if (!m.addName.isBlank()) pd.addAllowedPlayer(m.addName.trim());
            if (!m.remName.isBlank()) pd.removeAllowedPlayer(m.remName.trim());
            sd.setDirty();

            ServerLevel pl = sender.server.getLevel(ModDimensions.POCKET_LEVEL);
            if (pl != null) {
                for (ServerPlayer sp : pl.players()) {
                    if (pd.containsBlock(sp.getBlockX(), sp.getBlockZ())) {
                        ModNetworking.CHANNEL.send(
                                PacketDistributor.PLAYER.with(() -> sp),
                                new PacketSyncSkyWeather(pd.getSkyTheme(), pd.getWeatherVisual())
                        );
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
