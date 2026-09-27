package com.sebas.arcanemod.item;

import com.sebas.arcanemod.data.CursedLecternData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CursedLecternItem extends BlockItem {
    public CursedLecternItem(Item.Properties properties) {
        super(Blocks.LECTERN, properties);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        boolean placed = super.placeBlock(context, state);
        if (placed && !context.getLevel().isClientSide()) {
            CursedLecternData.get((ServerLevel) context.getLevel())
                    .markCursed(context.getClickedPos());
        }
        return placed;
    }
}