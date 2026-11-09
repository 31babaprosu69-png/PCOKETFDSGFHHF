package com.pocketdimensions.init;

import com.pocketdimensions.PocketDimensionsMod;
import com.pocketdimensions.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PocketDimensionsMod.MODID);

    public static final RegistryObject<CreativeModeTab> POCKET_TAB = TABS.register("pocketdimensions_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.pocketdimensions_tab"))
                    .icon(() -> new ItemStack(ModItems.POCKET_KEY.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.POCKET_KEY.get());
                        output.accept(ModItems.POCKET_KEYSTONE_ITEM.get());
                    })
                    .build());
}
