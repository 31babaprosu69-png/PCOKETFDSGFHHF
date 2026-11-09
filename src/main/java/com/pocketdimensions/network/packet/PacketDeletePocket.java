package com.pocketdimensions.network.packet;

import com.pocketdimensions.dimension.PocketSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketDeletePocket {
    private final UUID owner;

    public PacketDeletePocket(UUID o) {
        this.owner = o;
    }

    public static void encode(PacketDeletePocket m, FriendlyByteBuf b) {
        b.writeUUID(m.owner);
    }

    public static PacketDeletePocket decode(FriendlyByteBuf b) {
        return new PacketDeletePocket(b.readUUID());
    }

    public static void handle(PacketDeletePocket m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer s = ctx.get().getSender();
            if (s == null) return;
            if (!s.getUUID().equals(m.owner) && !s.hasPermissions(2)) {
                s.sendSystemMessage(Component.translatable("message.pocketdimensions.no_permission"));
                return;
            }
            if (PocketSavedData.get(s.server).deletePocket(m.owner, s.server)) {
                s.sendSystemMessage(Component.translatable("message.pocketdimensions.deleted"));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
