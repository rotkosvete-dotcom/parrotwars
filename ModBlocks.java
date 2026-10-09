package com.parrotwars.block;

import com.parrotwars.ParrotWars;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ParrotWars.MODID);

    public static final RegistryObject<Block> BARRACKS = BLOCKS.register("barracks",
            () -> new BarracksBlock(AbstractBlock.Properties.of(Material.STONE).strength(2.0F, 6.0F)));
}
