package com.parrotwars.item;

import com.parrotwars.ParrotWars;
import com.parrotwars.entity.ModEntities;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ParrotWars.MODID);

    public static final RegistryObject<Item> RADIO = ITEMS.register("radio",
            () -> new RadioItem(new Item.Properties().tab(ItemGroup.TAB_MISC).stacksTo(1)));

    public static final RegistryObject<Item> PARROT_SOLDIER_SPAWN_EGG = ITEMS.register("parrot_soldier_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PARROT_SOLDIER, 0xD62828, 0x1D4ED8,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));

    public static final RegistryObject<Item> PIGEON_SPAWN_EGG = ITEMS.register("pigeon_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PIGEON, 0x9CA3AF, 0x4B5563,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));
}
