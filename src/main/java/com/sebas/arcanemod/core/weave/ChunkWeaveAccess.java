package com.sebas.arcanemod.core.weave;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Optional;

/**
 * Small helper for reading a chunk's Weave data and lazily computing its base density from
 * the chunk's biome on first access.
 * <p>
 * Base density is deliberately NOT computed at capability-attach time: during worldgen a
 * chunk's final biome isn't necessarily settled yet when its capabilities are attached, so
 * {@link ChunkWeaveData} starts at the {@link ChunkWeaveData#UNINITIALIZED} sentinel and this
 * class fills it in the first time anything actually asks for the data.
 */
public final class ChunkWeaveAccess {
    private ChunkWeaveAccess() {}

    public static Optional<IChunkWeaveData> get(LevelChunk chunk) {
        return chunk.getCapability(WeaveCapabilities.CHUNK_WEAVE)
                .resolve()
                .map(data -> ensureInitialized(data, chunk));
    }

    private static IChunkWeaveData ensureInitialized(IChunkWeaveData data, LevelChunk chunk) {
        if (data.getBaseDensity() == ChunkWeaveData.UNINITIALIZED) {
            Level level = chunk.getLevel();
            BlockPos samplePos = chunk.getPos().getMiddleBlockPosition(level.getSeaLevel());
            Holder<Biome> biome = level.getBiome(samplePos);
            float base = WeaveDensity.baseDensityFor(biome);
            data.setBaseDensity(base);
            data.setCurrentDensity(base); // start fully charged rather than empty
        }
        return data;
    }
}
