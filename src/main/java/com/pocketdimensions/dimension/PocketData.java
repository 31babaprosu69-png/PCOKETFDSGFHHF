package com.pocketdimensions.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PocketData {
    private final UUID ownerUUID;
    private String ownerName;
    private final int plotIndex;
    private final int centerChunkX;
    private final int centerChunkZ;

    private boolean chunkLoaderActive;
    private int skyTheme;
    private int weatherVisual;
    private boolean isPublic;
    private final Set<String> allowedPlayerNames = new HashSet<>();

    private ResourceKey<Level> returnDimension = Level.OVERWORLD;
    private double returnX = 0.5;
    private double returnY = 70.0;
    private double returnZ = 0.5;
    private float returnYaw = 0.0f;
    private float returnPitch = 0.0f;

    public PocketData(UUID owner, String name, int idx, int cx, int cz) {
        this.ownerUUID = owner;
        this.ownerName = name;
        this.plotIndex = idx;
        this.centerChunkX = cx;
        this.centerChunkZ = cz;
        this.chunkLoaderActive = false;
        this.skyTheme = 0;
        this.weatherVisual = 0;
        this.isPublic = false;
    }

    public UUID getOwnerUUID() { return ownerUUID; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String n) { this.ownerName = n; }
    public int getPlotIndex() { return plotIndex; }
    public int getCenterChunkX() { return centerChunkX; }
    public int getCenterChunkZ() { return centerChunkZ; }

    public boolean isChunkLoaderActive() { return chunkLoaderActive; }
    public void setChunkLoaderActive(boolean a) { this.chunkLoaderActive = a; }

    public int getSkyTheme() { return skyTheme; }
    public void setSkyTheme(int t) { this.skyTheme = Math.max(0, Math.min(4, t)); }

    public int getWeatherVisual() { return weatherVisual; }
    public void setWeatherVisual(int w) { this.weatherVisual = Math.max(0, Math.min(2, w)); }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean p) { this.isPublic = p; }

    public Set<String> getAllowedPlayerNames() { return allowedPlayerNames; }
    public void addAllowedPlayer(String n) {
        if (n != null && !n.isBlank()) allowedPlayerNames.add(n.toLowerCase());
    }
    public void removeAllowedPlayer(String n) {
        if (n != null) allowedPlayerNames.remove(n.toLowerCase());
    }

    public boolean isAllowed(Player p) {
        if (p.hasPermissions(2)) return true;
        if (p.getUUID().equals(ownerUUID)) return true;
        if (isPublic) return true;
        return allowedPlayerNames.contains(p.getGameProfile().getName().toLowerCase());
    }

    public int minBlockX() { return (centerChunkX - 1) * 16; }
    public int maxBlockX() { return (centerChunkX + 1) * 16 + 15; }
    public int minBlockZ() { return (centerChunkZ - 1) * 16; }
    public int maxBlockZ() { return (centerChunkZ + 1) * 16 + 15; }

    public int spawnBlockX() { return centerChunkX * 16 + 8; }
    public int spawnBlockY() { return 65; }
    public int spawnBlockZ() { return centerChunkZ * 16 + 8; }

    public boolean containsBlock(int x, int z) {
        return x >= minBlockX() && x <= maxBlockX() && z >= minBlockZ() && z <= maxBlockZ();
    }

    public ResourceKey<Level> getReturnDimension() { return returnDimension; }
    public double getReturnX() { return returnX; }
    public double getReturnY() { return returnY; }
    public double getReturnZ() { return returnZ; }
    public float getReturnYaw() { return returnYaw; }
    public float getReturnPitch() { return returnPitch; }

    public void setReturnLocation(ResourceKey<Level> d, double x, double y, double z, float yaw, float pitch) {
        this.returnDimension = d;
        this.returnX = x;
        this.returnY = y;
        this.returnZ = z;
        this.returnYaw = yaw;
        this.returnPitch = pitch;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putUUID("OwnerUUID", ownerUUID);
        tag.putString("OwnerName", ownerName != null ? ownerName : "Unknown");
        tag.putInt("PlotIndex", plotIndex);
        tag.putInt("CX", centerChunkX);
        tag.putInt("CZ", centerChunkZ);
        tag.putBoolean("ChunkLoader", chunkLoaderActive);
        tag.putInt("Sky", skyTheme);
        tag.putInt("Weather", weatherVisual);
        tag.putBoolean("Public", isPublic);

        ListTag list = new ListTag();
        for (String s : allowedPlayerNames) list.add(StringTag.valueOf(s));
        tag.put("Allowed", list);

        tag.putString("RetDim", returnDimension.location().toString());
        tag.putDouble("RetX", returnX);
        tag.putDouble("RetY", returnY);
        tag.putDouble("RetZ", returnZ);
        tag.putFloat("RetYaw", returnYaw);
        tag.putFloat("RetPitch", returnPitch);
        return tag;
    }

    public static PocketData load(CompoundTag tag) {
        PocketData d = new PocketData(
                tag.getUUID("OwnerUUID"),
                tag.getString("OwnerName"),
                tag.getInt("PlotIndex"),
                tag.getInt("CX"),
                tag.getInt("CZ")
        );
        d.chunkLoaderActive = tag.getBoolean("ChunkLoader");
        d.skyTheme = tag.getInt("Sky");
        d.weatherVisual = tag.getInt("Weather");
        d.isPublic = tag.getBoolean("Public");

        ListTag list = tag.getList("Allowed", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            d.allowedPlayerNames.add(list.getString(i));
        }

        if (tag.contains("RetDim")) {
            d.returnDimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("RetDim")));
            d.returnX = tag.getDouble("RetX");
            d.returnY = tag.getDouble("RetY");
            d.returnZ = tag.getDouble("RetZ");
            d.returnYaw = tag.getFloat("RetYaw");
            d.returnPitch = tag.getFloat("RetPitch");
        }
        return d;
    }
}
