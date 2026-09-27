package com.sebas.arcanemod.core.fray;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Default implementation of {@link IPlayerFrayData}, attached to every player entity.
 * <p>
 * Its {@code LazyOptional} is deliberately never invalidated on removal — see
 * {@code FacetEvents#onAttachPlayer} (the {@code PlayerFacetKnowledge} sibling of this class) for
 * the full explanation of why: {@code PlayerEvent.Clone} needs to read the dying player's old
 * capability data, and once a {@code LazyOptional} is invalidated there is no way to undo that,
 * even after {@code reviveCaps()} un-invalidates the entity-level flag {@code getCapability}
 * checks first.
 */
public class PlayerFrayData implements IPlayerFrayData, ICapabilitySerializable<CompoundTag> {
    private int personalFray = 0;

    private final LazyOptional<IPlayerFrayData> lazyOptional = LazyOptional.of(() -> this);

    @Override
    public int getPersonalFray() {
        return personalFray;
    }

    @Override
    public void setPersonalFray(int fray) {
        this.personalFray = fray;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return FrayCapabilities.PLAYER_FRAY.orEmpty(cap, lazyOptional);
    }

    /** Copies another instance's state into this one — used by {@code FrayEvents#onPlayerClone}. */
    public void copyFrom(IPlayerFrayData other) {
        this.personalFray = other.getPersonalFray();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registryAccess) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("personalFray", personalFray);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registryAccess, CompoundTag tag) {
        personalFray = tag.getIntOr("personalFray", 0);
    }
}
