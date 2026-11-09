package com.pocketdimensions.block;

import com.pocketdimensions.PocketDimensionsMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PocketDimensionsMod.MODID);

    public static final RegistryObject<Block> POCKET_KEYSTONE = BLOCKS.register("pocket_keystone",
            () -> new PocketKeystoneBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(state -> 5)
                    .noLootTable()
            ));
}
