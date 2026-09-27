package com.sebas.arcanemod.core.facet;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link IPlayerFacetKnowledge}, attached to every player entity (see
 * {@code FacetEvents#onAttachPlayer}).
 * <p>
 * Unlike chunk capabilities, a player's capability does NOT automatically survive respawn — death
 * (and End-return) replaces the {@code Player} object entirely, and Forge only copies capability
 * data across that swap if a mod does it manually in {@code PlayerEvent.Clone}. See
 * {@code FacetEvents#onPlayerClone}.
 */
public class PlayerFacetKnowledge implements IPlayerFacetKnowledge, ICapabilitySerializable<CompoundTag> {
    private final Set<Identifier> scannedEntries = new HashSet<>();
    private final Set<Facet> knownFacets = EnumSet.noneOf(Facet.class);
    private final Set<CompoundFacet> knownCompounds = EnumSet.noneOf(CompoundFacet.class);

    private final LazyOptional<IPlayerFacetKnowledge> lazyOptional = LazyOptional.of(() -> this);

    @Override
    public Set<Identifier> getScannedEntries() {
        return Set.copyOf(scannedEntries);
    }

    @Override
    public boolean hasScanned(Identifier id) {
        return scannedEntries.contains(id);
    }

    @Override
    public boolean recordScan(Identifier id, FacetSignature signature) {
        boolean isNew = scannedEntries.add(id);

        for (Map.Entry<Facet, Integer> entry : signature.asMap().entrySet()) {
            knownFacets.add(entry.getKey());
        }
        for (CompoundFacet compound : CompoundFacet.values()) {
            if (signature.amountOf(compound.first()) > 0 && signature.amountOf(compound.second()) > 0) {
                knownCompounds.add(compound);
            }
        }

        return isNew;
    }

    @Override
    public Set<Facet> getKnownFacets() {
        return Set.copyOf(knownFacets);
    }

    @Override
    public Set<CompoundFacet> getKnownCompounds() {
        return Set.copyOf(knownCompounds);
    }

    @Override
    public boolean knowsFacet(Facet facet) {
        return knownFacets.contains(facet);
    }

    @Override
    public boolean knowsCompound(CompoundFacet compound) {
        return knownCompounds.contains(compound);
    }

    /**
     * Never invalidated (see {@code FacetEvents#onAttachPlayer} for why) — kept as a plain field
     * rather than reconstructed per-call so {@code getCapability} doesn't allocate one every time.
     */
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return FacetCapabilities.PLAYER_FACET_KNOWLEDGE.orEmpty(cap, lazyOptional);
    }

    /** Copies another instance's state into this one — used by {@code FacetEvents#onPlayerClone}. */
    public void copyFrom(IPlayerFacetKnowledge other) {
        scannedEntries.addAll(other.getScannedEntries());
        knownFacets.addAll(other.getKnownFacets());
        knownCompounds.addAll(other.getKnownCompounds());
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registryAccess) {
        CompoundTag tag = new CompoundTag();
        tag.put("scannedEntries", identifiersToList(scannedEntries));
        tag.put("knownFacets", namesToList(knownFacets));
        tag.put("knownCompounds", namesToList(knownCompounds));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registryAccess, CompoundTag tag) {
        scannedEntries.clear();
        ListTag scanned = tag.getListOrEmpty("scannedEntries");
        for (int i = 0; i < scanned.size(); i++) {
            Identifier id = Identifier.tryParse(scanned.getStringOr(i, ""));
            if (id != null) scannedEntries.add(id);
        }

        knownFacets.clear();
        ListTag facets = tag.getListOrEmpty("knownFacets");
        for (int i = 0; i < facets.size(); i++) {
            try {
                knownFacets.add(Facet.valueOf(facets.getStringOr(i, "")));
            } catch (IllegalArgumentException ignored) {
                // Facet was removed/renamed since this was saved — drop it rather than crash the load.
            }
        }

        knownCompounds.clear();
        ListTag compounds = tag.getListOrEmpty("knownCompounds");
        for (int i = 0; i < compounds.size(); i++) {
            try {
                knownCompounds.add(CompoundFacet.valueOf(compounds.getStringOr(i, "")));
            } catch (IllegalArgumentException ignored) {
                // Same as above, for compounds.
            }
        }
    }

    private static ListTag identifiersToList(Set<Identifier> ids) {
        ListTag list = new ListTag();
        for (Identifier id : ids) {
            list.add(StringTag.valueOf(id.toString()));
        }
        return list;
    }

    private static ListTag namesToList(Set<? extends Enum<?>> values) {
        ListTag list = new ListTag();
        for (Enum<?> value : values) {
            list.add(StringTag.valueOf(value.name()));
        }
        return list;
    }
}
