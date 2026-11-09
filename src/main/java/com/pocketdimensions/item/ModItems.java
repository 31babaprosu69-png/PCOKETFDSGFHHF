package com.pocketdimensions.item;

import com.pocketdimensions.PocketDimensionsMod;
import com.pocketdimensions.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PocketDimensionsMod.MODID);

    public static final RegistryObject<PocketKeyItem> POCKET_KEY = ITEMS.register("pocket_key",
            () -> new PocketKeyItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    public static final RegistryObject<BlockItem> POCKET_KEYSTONE_ITEM = ITEMS.register("pocket_keystone",
            () -> new BlockItem(ModBlocks.POCKET_KEYSTONE.get(), new Item.Properties().rarity(Rarity.RARE)));
}
