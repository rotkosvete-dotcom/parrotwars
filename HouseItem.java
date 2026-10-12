package com.parrotwars.item;

import com.parrotwars.country.CountryManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * Домик попугаев: ставится на своей территории. Вход всего 1 блок высотой -
 * человек не пролезет, а попугай (игрок в форме попугая, граждане) войдёт.
 * Внутри в форме попугая лечишься. Каждый дом даёт жильё гражданам.
 */
public class HouseItem extends Item {

    public HouseItem(Properties props) {
        super(props);
    }

    private static boolean free(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial().isReplaceable();
    }

    @Override
    public ActionResultType useOn(ItemUseContext ctx) {
        World world = ctx.getLevel();
        PlayerEntity player = ctx.getPlayer();
        if (player == null) {
            return ActionResultType.PASS;
        }
        if (world.isClientSide) {
            return ActionResultType.sidedSuccess(true);
        }
        CountryManager manager = CountryManager.get(world.getServer());
        CountryManager.Country country = manager.getCountry(player.getUUID());
        if (country == null) {
            player.displayClientMessage(new TranslationTextComponent("message.parrotwars.country_none"), true);
            return ActionResultType.FAIL;
        }
        BlockPos base = ctx.getClickedPos().above();
        if (!country.chunks.contains(new ChunkPos(base).toLong())) {
            player.displayClientMessage(new TranslationTextComponent("message.parrotwars.house_outside"), true);
            return ActionResultType.FAIL;
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int y = 0; y <= 3; y++) {
                    if (!free(world, base.offset(dx, y, dz))) {
                        player.displayClientMessage(new TranslationTextComponent("message.parrotwars.house_space"), true);
                        return ActionResultType.FAIL;
                    }
                }
            }
        }
        if (world.isEmptyBlock(base.below())) {
            player.displayClientMessage(new TranslationTextComponent("message.parrotwars.house_space"), true);
            return ActionResultType.FAIL;
        }

        Direction door = ctx.getHorizontalDirection().getOpposite();
        int doorX = door.getStepX() * 2;
        int doorZ = door.getStepZ() * 2;
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
        BlockState slab = Blocks.OAK_SLAB.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                boolean wall = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                boolean corner = Math.abs(dx) == 2 && Math.abs(dz) == 2;
                for (int y = 0; y <= 2; y++) {
                    BlockPos p = base.offset(dx, y, dz);
                    if (!wall) {
                        world.setBlock(p, air, 3);
                    } else if (dx == doorX && dz == doorZ && y == 0) {
                        world.setBlock(p, air, 3);              // вход 1x1: только для попугаев
                    } else if (corner) {
                        world.setBlock(p, log, 3);
                    } else if (y == 1 && (dx == 0 || dz == 0)) {
                        world.setBlock(p, glass, 3);            // окошки
                    } else {
                        world.setBlock(p, planks, 3);
                    }
                }
                world.setBlock(base.offset(dx, 3, dz), slab, 3); // крыша
            }
        }
        // гнездо (сено) у дальней стены и факел
        BlockPos nest = base.offset(-door.getStepX(), 0, -door.getStepZ());
        world.setBlock(nest, Blocks.HAY_BLOCK.defaultBlockState(), 3);
        world.setBlock(nest.above(), Blocks.TORCH.defaultBlockState(), 3);

        manager.addHouse(country, base);
        ItemStack stack = ctx.getItemInHand();
        if (!player.abilities.instabuild) {
            stack.shrink(1);
        }
        player.displayClientMessage(new TranslationTextComponent("message.parrotwars.house_built"), true);
        return ActionResultType.SUCCESS;
    }
}
