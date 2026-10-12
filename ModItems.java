package com.parrotwars.item;

import com.parrotwars.ParrotWars;
import com.parrotwars.block.ModBlocks;
import com.parrotwars.entity.ModEntities;
import net.minecraft.item.BlockItem;
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

    public static final RegistryObject<Item> PARROT_FEATHER = ITEMS.register("parrot_feather",
            () -> new ParrotFeatherItem(new Item.Properties().tab(ItemGroup.TAB_MISC).stacksTo(1)));

    public static final RegistryObject<Item> COMMAND_MAP = ITEMS.register("command_map",
            () -> new StrategyMapItem(new Item.Properties().tab(ItemGroup.TAB_MISC).stacksTo(1)));

    public static final RegistryObject<Item> PARROT_HOUSE = ITEMS.register("parrot_house",
            () -> new HouseItem(new Item.Properties().tab(ItemGroup.TAB_MISC)));

    public static final RegistryObject<Item> BARRACKS = ITEMS.register("barracks",
            () -> new BlockItem(ModBlocks.BARRACKS.get(), new Item.Properties().tab(ItemGroup.TAB_MISC)));

    public static final RegistryObject<Item> PARROT_SOLDIER_SPAWN_EGG = ITEMS.register("parrot_soldier_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PARROT_SOLDIER, 0xD62828, 0x1D4ED8,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));

    public static final RegistryObject<Item> PARROT_SHOOTER_SPAWN_EGG = ITEMS.register("parrot_shooter_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PARROT_SHOOTER, 0x16A34A, 0xFACC15,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));

    public static final RegistryObject<Item> PARROT_MEDIC_SPAWN_EGG = ITEMS.register("parrot_medic_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PARROT_MEDIC, 0xFACC15, 0xFFFFFF,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));

    public static final RegistryObject<Item> PARROT_TANK_SPAWN_EGG = ITEMS.register("parrot_tank_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PARROT_TANK, 0x4B5320, 0x1F2937,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));

    public static final RegistryObject<Item> PARROT_ARTILLERY_SPAWN_EGG = ITEMS.register("parrot_artillery_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PARROT_ARTILLERY, 0x4B5320, 0xB45309,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));

    public static final RegistryObject<Item> PIGEON_SPAWN_EGG = ITEMS.register("pigeon_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PIGEON, 0x9CA3AF, 0x4B5563,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));

    public static final RegistryObject<Item> PIGEON_BOMBER_SPAWN_EGG = ITEMS.register("pigeon_bomber_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PIGEON_BOMBER, 0x9CA3AF, 0xDC2626,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));

    public static final RegistryObject<Item> PIGEON_SNIPER_SPAWN_EGG = ITEMS.register("pigeon_sniper_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PIGEON_SNIPER, 0x9CA3AF, 0x111827,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));

    public static final RegistryObject<Item> PIGEON_TANK_SPAWN_EGG = ITEMS.register("pigeon_tank_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PIGEON_TANK, 0x6B7280, 0x1F2937,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));

    public static final RegistryObject<Item> PIGEON_ARTILLERY_SPAWN_EGG = ITEMS.register("pigeon_artillery_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PIGEON_ARTILLERY, 0x6B7280, 0xB45309,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));

    public static final RegistryObject<Item> PARROT_CITIZEN_SPAWN_EGG = ITEMS.register("parrot_citizen_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PARROT_CITIZEN, 0x43A047, 0xFFFFFF,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));

    public static final RegistryObject<Item> PIGEON_CITIZEN_SPAWN_EGG = ITEMS.register("pigeon_citizen_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PIGEON_CITIZEN, 0x9CA3AF, 0xFFFFFF,
                    new Item.Properties().tab(ItemGroup.TAB_MISC)));
}
