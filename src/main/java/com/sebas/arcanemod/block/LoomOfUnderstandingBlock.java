package com.sebas.arcanemod.block;

import com.mojang.serialization.MapCodec;
import com.sebas.arcanemod.client.ClientLoomOpener;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The Loom of Understanding — the mod's research table. No block entity or container menu: the
 * research tree and a player's completed set are both already synced to the client independently
 * (see {@code SyncResearchTreePacket}/{@code SyncResearchPacket}), so opening the screen needs no
 * server round trip at all — this mirrors {@code CodexArcanumItem}'s open pattern exactly, just
 * triggered from a block interaction instead of an item use. Attempting to actually complete a
 * node (clicking an available one in the screen) is what needs a real client -> server packet,
 * handled separately by {@code RequestResearchPacket}.
 */
public class LoomOfUnderstandingBlock extends Block {
    public static final MapCodec<LoomOfUnderstandingBlock> CODEC = simpleCodec(LoomOfUnderstandingBlock::new);

    public LoomOfUnderstandingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            ClientLoomOpener.open();
        }
        return InteractionResult.SUCCESS;
    }
}
