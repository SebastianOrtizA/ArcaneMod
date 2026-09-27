package com.sebas.arcanemod.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

public class LoomkeepersSatchelStructure extends Structure {
    public static final MapCodec<LoomkeepersSatchelStructure> CODEC = simpleCodec(LoomkeepersSatchelStructure::new);
    private static final int MAX_DISTANCE_FROM_SPAWN = 200;

    public LoomkeepersSatchelStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int blockX = context.chunkPos().getMiddleBlockX();
        int blockZ = context.chunkPos().getMiddleBlockZ();
        if (blockX * blockX + blockZ * blockZ > MAX_DISTANCE_FROM_SPAWN * MAX_DISTANCE_FROM_SPAWN) {
            return Optional.empty();
        }

        return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG,
                builder -> generatePieces(builder, context));
    }

    private static void generatePieces(StructurePiecesBuilder builder, GenerationContext context) {
        BlockPos pos = new BlockPos(
                context.chunkPos().getMiddleBlockX(),
                0,
                context.chunkPos().getMiddleBlockZ());
        builder.addPiece(new LoomkeepersSatchelPiece(pos));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.LOOMKEEPERS_SATCHEL_TYPE.get();
    }
}
