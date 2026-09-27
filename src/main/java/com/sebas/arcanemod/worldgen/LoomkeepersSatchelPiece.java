package com.sebas.arcanemod.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;

public class LoomkeepersSatchelPiece extends StructurePiece {
    private static final ResourceKey<LootTable> LOOT_TABLE =
            ResourceKey.create(Registries.LOOT_TABLE,
                    Identifier.fromNamespaceAndPath("arcanemod", "chests/loomkeepers_satchel"));

    public LoomkeepersSatchelPiece(BlockPos pos) {
        super(ModStructures.LOOMKEEPERS_SATCHEL_PIECE.get(), 0, new BoundingBox(pos));
    }

    public LoomkeepersSatchelPiece(CompoundTag tag) {
        super(ModStructures.LOOMKEEPERS_SATCHEL_PIECE.get(), tag);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
    }

    @Override
    public void postProcess(
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator generator,
            RandomSource random,
            BoundingBox chunkBB,
            ChunkPos chunkPos,
            BlockPos referencePos
    ) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,
                this.boundingBox.minX(), this.boundingBox.minZ());

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(
                this.boundingBox.minX(), surfaceY, this.boundingBox.minZ());

        // 3-block mossy cobblestone shelf: left, center, right
        level.setBlock(pos.offset(-1, 0, 0), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
        level.setBlock(pos, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
        level.setBlock(pos.offset(1, 0, 0), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);

        // Chest on top of center block
        this.boundingBox = new BoundingBox(pos.above());
        this.createChest(level, chunkBB, random, pos.above(), LOOT_TABLE, null);
    }
}
