package com.sebas.arcanemod.core.research;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation of {@link IPlayerResearchData}, attached to every player entity (see
 * {@code ResearchEvents#onAttachPlayer}).
 * <p>
 * Same respawn-survival caveat as {@code PlayerFacetKnowledge}/{@code PlayerFrayData}: a player
 * capability does NOT automatically survive death/dimension-return, so {@code ResearchEvents}
 * copies this across manually in {@code PlayerEvent.Clone} — see that class for the full
 * reviveCaps/never-invalidate explanation.
 */
public class PlayerResearchData implements IPlayerResearchData, ICapabilitySerializable<CompoundTag> {
    private final Set<Identifier> completedResearch = new HashSet<>();

    private final LazyOptional<IPlayerResearchData> lazyOptional = LazyOptional.of(() -> this);

    @Override
    public Set<Identifier> getCompletedResearch() {
        return Set.copyOf(completedResearch);
    }

    @Override
    public boolean hasCompleted(Identifier id) {
        return completedResearch.contains(id);
    }

    @Override
    public boolean canStart(Identifier id) {
        return ResearchTree.prerequisitesMet(id, completedResearch);
    }

    @Override
    public boolean complete(Identifier id) {
        return completedResearch.add(id);
    }

    @Override
    public boolean forget(Identifier id) {
        return completedResearch.remove(id);
    }

    /** Copies another instance's state into this one — used by {@code ResearchEvents#onPlayerClone}. */
    public void copyFrom(IPlayerResearchData other) {
        completedResearch.addAll(other.getCompletedResearch());
    }

    /** Never invalidated (see {@code ResearchEvents#onAttachPlayer} for why). */
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return ResearchCapabilities.PLAYER_RESEARCH_DATA.orEmpty(cap, lazyOptional);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registryAccess) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Identifier id : completedResearch) {
            list.add(StringTag.valueOf(id.toString()));
        }
        tag.put("completedResearch", list);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registryAccess, CompoundTag tag) {
        completedResearch.clear();
        ListTag list = tag.getListOrEmpty("completedResearch");
        for (int i = 0; i < list.size(); i++) {
            Identifier id = Identifier.tryParse(list.getStringOr(i, ""));
            if (id != null) completedResearch.add(id);
        }
    }
}
