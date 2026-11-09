package com.pocketdimensions;

import com.pocketdimensions.block.ModBlocks;
import com.pocketdimensions.dimension.ModDimensions;
import com.pocketdimensions.init.ModCreativeTabs;
import com.pocketdimensions.item.ModItems;
import com.pocketdimensions.network.ModNetworking;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PocketDimensionsMod.MODID)
public class PocketDimensionsMod {
    public static final String MODID = "pocketdimensions";

    public PocketDimensionsMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModCreativeTabs.TABS.register(bus);
        ModDimensions.register();
        bus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetworking::register);
    }
}
