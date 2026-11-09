package com.pocketdimensions.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PocketSavedData extends SavedData {
    private static final String DATA_NAME = "pocketdimensions_data";
    private int nextPlotIndex = 0;
    private final Map<UUID, PocketData> pockets = new HashMap<>();

    public PocketSavedData() {}

    public static PocketSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(PocketSavedData::load, PocketSavedData::new, DATA_NAME);
    }

    public Collection<PocketData> getAllPockets() { return pockets.values(); }
    public PocketData getPocketByOwner(UUID u) { return pockets.get(u); }

    public PocketData getPocketByOwnerName(String name) {
        for (PocketData pd : pockets.values()) {
            if (pd.getOwnerName().equalsIgnoreCase(name)) return pd;
        }
        return null;
    }

    public PocketData getPocketAt(int cx, int cz) {
        for (PocketData pd : pockets.values()) {
            if (Math.abs(cx - pd.getCenterChunkX()) <= 1 && Math.abs(cz - pd.getCenterChunkZ()) <= 1) return pd;
        }
        return null;
    }

    public PocketData getOrCreatePocket(ServerPlayer p) {
        UUID u = p.getUUID();
        if (pockets.containsKey(u)) {
            PocketData pd = pockets.get(u);
            pd.setOwnerName(p.getGameProfile().getName());
            setDirty();
            return pd;
        }

        int idx = nextPlotIndex++;
        int gridX = (idx % 100) * 64;
        int gridZ = (idx / 100) * 64;

        PocketData nd = new PocketData(u, p.getGameProfile().getName(), idx, gridX, gridZ);
        pockets.put(u, nd);
        setDirty();

        ServerLevel pl = p.server.getLevel(ModDimensions.POCKET_LEVEL);
        if (pl != null) {
            PocketPlotManager.generateStarterPlatform(pl, nd);
        }
        return nd;
    }

    public void setChunkLoader(UUID u, boolean active, MinecraftServer server) {
        PocketData pd = pockets.get(u);
        if (pd != null) {
            pd.setChunkLoaderActive(active);
            setDirty();
            ServerLevel pl = server.getLevel(ModDimensions.POCKET_LEVEL);
            if (pl != null) {
                PocketPlotManager.applyChunkLoading(pl, pd, active);
            }
        }
    }

    public boolean deletePocket(UUID u, MinecraftServer server) {
        PocketData pd = pockets.remove(u);
        if (pd == null) return false;

        ServerLevel pl = server.getLevel(ModDimensions.POCKET_LEVEL);
        if (pl != null) {
            PocketPlotManager.applyChunkLoading(pl, pd, false);
            for (ServerPlayer player : pl.players()) {
                if (pd.containsBlock(player.getBlockX(), player.getBlockZ())) {
                    teleportOut(player, pd, server);
                }
            }
            PocketPlotManager.clearPlot(pl, pd);
        }
        setDirty();
        return true;
    }

    public static void teleportOut(ServerPlayer player, PocketData data, MinecraftServer server) {
        ServerLevel td = server.getLevel(data.getReturnDimension());
        if (td == null) td = server.overworld();
        BlockPos sp = td.getSharedSpawnPos();
        double tx = data.getReturnX();
        double ty = data.getReturnY();
        double tz = data.getReturnZ();

        if (ty <= td.getMinBuildHeight()) {
            tx = sp.getX() + 0.5;
            ty = sp.getY() + 1.0;
            tz = sp.getZ() + 0.5;
        }

        player.teleportTo(td, tx, ty, tz, data.getReturnYaw(), data.getReturnPitch());
        player.fallDistance = 0.0f;
        player.setDeltaMovement(Vec3.ZERO);
        player.sendSystemMessage(Component.translatable("message.pocketdimensions.deleted"));
    }

    public static PocketSavedData load(CompoundTag tag) {
        PocketSavedData sd = new PocketSavedData();
        sd.nextPlotIndex = tag.getInt("NextPlot");
        ListTag list = tag.getList("Pockets", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            PocketData pd = PocketData.load(list.getCompound(i));
            sd.pockets.put(pd.getOwnerUUID(), pd);
        }
        return sd;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("NextPlot", nextPlotIndex);
        ListTag list = new ListTag();
        for (PocketData pd : pockets.values()) list.add(pd.save(new CompoundTag()));
        tag.put("Pockets", list);
        return tag;
    }
}
