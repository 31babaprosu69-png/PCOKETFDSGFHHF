package com.pocketdimensions.network.packet;

import com.pocketdimensions.client.ClientSetup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketOpenSettings {
    private final UUID ownerUUID;
    private final String ownerName;
    private final boolean chunkLoaderActive, isPublic, isOP;
    private final int skyTheme, weatherVisual;
    private final List<String> allowed;

    public PacketOpenSettings(UUID u, String n, boolean c, int s, int w, boolean pub, List<String> al, boolean op) {
        this.ownerUUID = u;
        this.ownerName = n;
        this.chunkLoaderActive = c;
        this.skyTheme = s;
        this.weatherVisual = w;
        this.isPublic = pub;
        this.allowed = al;
        this.isOP = op;
    }

    public static void encode(PacketOpenSettings m, FriendlyByteBuf b) {
        b.writeUUID(m.ownerUUID);
        b.writeUtf(m.ownerName);
        b.writeBoolean(m.chunkLoaderActive);
        b.writeInt(m.skyTheme);
        b.writeInt(m.weatherVisual);
        b.writeBoolean(m.isPublic);
        b.writeInt(m.allowed.size());
        for (String s : m.allowed) b.writeUtf(s);
        b.writeBoolean(m.isOP);
    }

    public static PacketOpenSettings decode(FriendlyByteBuf b) {
        UUID u = b.readUUID();
        String n = b.readUtf();
        boolean c = b.readBoolean();
        int s = b.readInt();
        int w = b.readInt();
        boolean pub = b.readBoolean();
        int sz = b.readInt();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < sz; i++) list.add(b.readUtf());
        return new PacketOpenSettings(u, n, c, s, w, pub, list, b.readBoolean());
    }

    public static void handle(PacketOpenSettings m, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSetup.openSettingsScreen(
                m.ownerUUID, m.ownerName, m.chunkLoaderActive, m.skyTheme, m.weatherVisual, m.isPublic, m.allowed, m.isOP
        )));
        ctx.get().setPacketHandled(true);
    }
}
