package com.pocketdimensions.dimension;

import com.pocketdimensions.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

public class PocketPlotManager {
    public static void generateStarterPlatform(ServerLevel level, PocketData data) {
        int cx = data.spawnBlockX();
        int cz = data.spawnBlockZ();
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int x = cx - 1; x <= cx + 1; x++) {
            for (int z = cz - 1; z <= cz + 1; z++) {
                mpos.set(x, 64, z);
                level.setBlock(mpos, ModBlocks.POCKET_KEYSTONE.get().defaultBlockState(), 3);
            }
        }
    }

    public static void clearPlot(ServerLevel level, PocketData data) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int x = data.minBlockX(); x <= data.maxBlockX(); x++) {
            for (int z = data.minBlockZ(); z <= data.maxBlockZ(); z++) {
                for (int y = -64; y <= 320; y++) {
                    mpos.set(x, y, z);
                    if (!level.isEmptyBlock(mpos)) {
                        level.setBlock(mpos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    public static void applyChunkLoading(ServerLevel level, PocketData data, boolean enable) {
        int cx = data.getCenterChunkX();
        int cz = data.getCenterChunkZ();
        for (int x = cx - 1; x <= cx + 1; x++) {
            for (int z = cz - 1; z <= cz + 1; z++) {
                level.setChunkForced(x, z, enable);
            }
        }
    }
}
