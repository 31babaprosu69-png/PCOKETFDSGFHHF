package com.pocketdimensions.event;

import com.pocketdimensions.PocketDimensionsMod;
import com.pocketdimensions.dimension.ModDimensions;
import com.pocketdimensions.dimension.PocketData;
import com.pocketdimensions.dimension.PocketPlotManager;
import com.pocketdimensions.dimension.PocketSavedData;
import com.pocketdimensions.network.ModNetworking;
import com.pocketdimensions.network.packet.PacketSyncSkyWeather;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = PocketDimensionsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PocketEvents {

    @SubscribeEvent
    public static void onSpawnCheck(MobSpawnEvent.PositionCheck e) {
        if (e.getLevel() != null && e.getLevel().getLevel() instanceof ServerLevel sl && sl.dimension().equals(ModDimensions.POCKET_LEVEL)) {
            MobSpawnType t = e.getSpawnType();
            if (t == MobSpawnType.NATURAL || t == MobSpawnType.CHUNK_GENERATION ||
                t == MobSpawnType.SPAWNER || t == MobSpawnType.STRUCTURE ||
                t == MobSpawnType.PATROL || t == MobSpawnType.REINFORCEMENT ||
                t == MobSpawnType.EVENT || t == MobSpawnType.JOCKEY) {
                e.setResult(Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void onFinalize(MobSpawnEvent.FinalizeSpawn e) {
        if (e.getLevel() != null && e.getLevel().getLevel() instanceof ServerLevel sl && sl.dimension().equals(ModDimensions.POCKET_LEVEL)) {
            MobSpawnType t = e.getSpawnType();
            if (t == MobSpawnType.NATURAL || t == MobSpawnType.CHUNK_GENERATION ||
                t == MobSpawnType.SPAWNER || t == MobSpawnType.STRUCTURE ||
                t == MobSpawnType.PATROL || t == MobSpawnType.REINFORCEMENT ||
                t == MobSpawnType.EVENT || t == MobSpawnType.JOCKEY) {
                e.setSpawnCancelled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent e) {
        if (!e.loadedFromDisk() && e.getLevel().dimension().equals(ModDimensions.POCKET_LEVEL) && e.getEntity() instanceof Enemy) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.player.level().isClientSide || !(e.player instanceof ServerPlayer player)) return;

        if (player.level().dimension().equals(ModDimensions.POCKET_LEVEL)) {
            PocketSavedData sd = PocketSavedData.get(player.server);
            PocketData pd = sd.getPocketAt(player.chunkPosition().x, player.chunkPosition().z);
            if (pd == null) pd = sd.getPocketByOwner(player.getUUID());

            if (player.getY() < -30.0) {
                if (pd != null) {
                    PocketSavedData.teleportOut(player, pd, player.server);
                } else {
                    ServerLevel ow = player.server.overworld();
                    BlockPos sp = ow.getSharedSpawnPos();
                    player.teleportTo(ow, sp.getX() + 0.5, sp.getY() + 1.0, sp.getZ() + 0.5, 0, 0);
                }
                player.fallDistance = 0.0f;
                player.setDeltaMovement(Vec3.ZERO);
                player.sendSystemMessage(Component.translatable("message.pocketdimensions.void_fall"));
                return;
            }

            if (pd != null) {
                double px = player.getX();
                double pz = player.getZ();
                boolean out = false;
                double pushX = 0, pushZ = 0;

                if (px < pd.minBlockX()) { pushX = 0.4; out = true; }
                else if (px > pd.maxBlockX() + 1.0) { pushX = -0.4; out = true; }

                if (pz < pd.minBlockZ()) { pushZ = 0.4; out = true; }
                else if (pz > pd.maxBlockZ() + 1.0) { pushZ = -0.4; out = true; }

                if (out) {
                    player.setDeltaMovement(new Vec3(pushX, player.getDeltaMovement().y, pushZ));
                    player.displayClientMessage(Component.translatable("message.pocketdimensions.boundary"), true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent e) {
        if (e.phase == TickEvent.Phase.END && !e.level.isClientSide && e.level.dimension().equals(ModDimensions.POCKET_LEVEL) && e.level instanceof ServerLevel sl) {
            if (sl.isRaining() || sl.isThundering()) {
                sl.setWeatherParameters(0, 0, false, false);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent e) {
        ServerLevel pl = e.getServer().getLevel(ModDimensions.POCKET_LEVEL);
        if (pl != null) {
            for (PocketData pd : PocketSavedData.get(e.getServer()).getAllPockets()) {
                if (pd.isChunkLoaderActive()) {
                    PocketPlotManager.applyChunkLoading(pl, pd, true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDimChange(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getTo().equals(ModDimensions.POCKET_LEVEL) && e.getEntity() instanceof ServerPlayer sp) {
            PocketSavedData sd = PocketSavedData.get(sp.server);
            PocketData pd = sd.getPocketAt(sp.chunkPosition().x, sp.chunkPosition().z);
            if (pd == null) pd = sd.getPocketByOwner(sp.getUUID());
            if (pd != null) {
                ModNetworking.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> sp),
                        new PacketSyncSkyWeather(pd.getSkyTheme(), pd.getWeatherVisual())
                );
            }
        }
    }
}
