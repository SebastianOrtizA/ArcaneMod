package com.sebas.arcanemod.block;

import com.mojang.serialization.MapCodec;
import com.sebas.arcanemod.blockentity.ModBlockEntities;
import com.sebas.arcanemod.blockentity.WeaveWellspringBlockEntity;
import com.sebas.arcanemod.client.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Weave Wellspring — a stone pedestal (~1.5 blocks tall) that passively accumulates Wyrd
 * from the chunk it stands in. Deliberately has no {@code useItemOn}/{@code useWithoutItem}
 * override: leaving right-click interaction unhandled here is what lets it fall through to
 * the wand's own generic use, which is how channeling actually starts (see WandItem).
 */
public class WeaveWellspringBlock extends BaseEntityBlock {
    public static final MapCodec<WeaveWellspringBlock> CODEC = simpleCodec(WeaveWellspringBlock::new);

    // A wide base plus a narrower raised column — roughly 1.5 blocks tall overall.
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2, 0, 2, 14, 8, 14),
            Block.box(5, 8, 5, 11, 24, 11)
    );

    public WeaveWellspringBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WeaveWellspringBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.WEAVE_WELLSPRING.get(), WeaveWellspringBlockEntity::serverTick);
    }

    // Worldgen-placed Wellsprings never go through setPlacedBy (features call level.setBlock
    // directly), so this only fires for a player placing one from their inventory — exactly
    // where isAncient should flip to false (weaker, player-built, per the design doc).
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof WeaveWellspringBlockEntity wellspring) {
            wellspring.setAncient(false);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof WeaveWellspringBlockEntity wellspring)) return;
        int maxWyrd = wellspring.getMaxWyrd();
        if (maxWyrd <= 0) return; // hasn't rolled its capacity yet (fresh placement, not ticked)
        float chargeRatio = (float) wellspring.getStoredWyrd() / maxWyrd;
        if (chargeRatio <= 0f || random.nextFloat() > chargeRatio * 0.5f) return;

        double angle = random.nextDouble() * Math.PI * 2;
        double radius = 0.3;
        double x = pos.getX() + 0.5 + Math.cos(angle) * radius;
        double z = pos.getZ() + 0.5 + Math.sin(angle) * radius;
        double y = pos.getY() + 1.2 + random.nextDouble() * 0.4;
        level.addParticle(ModParticles.WYRD_SPARK.get(), x, y, z, 0.0, 0.05, 0.0);
    }
}
