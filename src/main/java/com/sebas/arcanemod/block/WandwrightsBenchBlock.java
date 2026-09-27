package com.sebas.arcanemod.block;

import com.sebas.arcanemod.inventory.WandwrightsBenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WandwrightsBenchBlock extends Block {
    private static final Component TITLE = Component.translatable("container.arcanemod.wandwrights_bench");
    private final boolean advanced;

    public WandwrightsBenchBlock(Properties properties, boolean advanced) {
        super(properties);
        this.advanced = advanced;
    }

    public boolean isAdvanced() { return advanced; }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, inventory, player) ->
                        new WandwrightsBenchMenu(containerId, inventory,
                                ContainerLevelAccess.create(level, pos), advanced),
                TITLE);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            player.openMenu(state.getMenuProvider(level, pos));
        }
        return InteractionResult.SUCCESS;
    }
}
