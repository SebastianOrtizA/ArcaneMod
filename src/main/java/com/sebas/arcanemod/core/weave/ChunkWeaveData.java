package com.sebas.arcanemod.core.weave;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Default implementation of {@link IChunkWeaveData}, attached to every chunk when it's created
 * (see {@code WeaveEvents#onAttachChunk}).
 * <p>
 * Implementing {@code ICapabilitySerializable<CompoundTag>} is what makes {@code LevelChunk}
 * automatically save/load this via NBT on its own — verified by reading the Forge
 * {@code LevelChunk} patch, which wires {@code writeCapsToNBT}/{@code readCapsFromNBT} straight
 * into chunk save/load. No manual {@code SavedData} bookkeeping needed here, unlike
 * {@code CursedLecternData}'s world-level approach (which exists because *positions*, not a
 * per-chunk object, needed tracking there).
 */
public class ChunkWeaveData implements IChunkWeaveData, ICapabilitySerializable<CompoundTag> {
    /** Sentinel meaning "not yet computed from this chunk's biome" — see {@link ChunkWeaveAccess}. */
    public static final float UNINITIALIZED = -1f;

    private float baseDensity = UNINITIALIZED;
    private float currentDensity = UNINITIALIZED;
    private int regionalFray = 0;

    private final LazyOptional<IChunkWeaveData> lazyOptional = LazyOptional.of(() -> this);

    @Override
    public float getBaseDensity() {
        return baseDensity;
    }

    @Override
    public void setBaseDensity(float density) {
        this.baseDensity = density;
    }

    @Override
    public float getCurrentDensity() {
        return currentDensity;
    }

    @Override
    public void setCurrentDensity(float density) {
        this.currentDensity = density;
    }

    @Override
    public int getRegionalFray() {
        return regionalFray;
    }

    @Override
    public void setRegionalFray(int fray) {
        this.regionalFray = fray;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return WeaveCapabilities.CHUNK_WEAVE.orEmpty(cap, lazyOptional);
    }

    /** Called when the chunk is invalidated (unloaded) — see the listener registered in WeaveEvents. */
    public void invalidate() {
        lazyOptional.invalidate();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registryAccess) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("baseDensity", baseDensity);
        tag.putFloat("currentDensity", currentDensity);
        tag.putInt("regionalFray", regionalFray);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registryAccess, CompoundTag tag) {
        baseDensity = tag.getFloatOr("baseDensity", UNINITIALIZED);
        currentDensity = tag.getFloatOr("currentDensity", UNINITIALIZED);
        regionalFray = tag.getIntOr("regionalFray", 0);
    }
}
