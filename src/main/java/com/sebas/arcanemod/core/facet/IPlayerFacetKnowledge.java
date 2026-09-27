package com.sebas.arcanemod.core.facet;

import net.minecraft.resources.Identifier;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.Set;

/**
 * Tracks what a single player has learned about the world's Facets, via Resonometer scans (and,
 * for now, the {@code /arcane scan} debug command that stands in for it until Checkpoint 4).
 * <p>
 * {@code scannedEntries} is the source of truth — every id the player has ever pointed the
 * Resonometer at. {@code knownFacets}/{@code knownCompounds} are derived from those scans'
 * signatures and cached here (rather than recomputed from {@code scannedEntries} + FacetRegistry
 * every time) so they stay simple {@code Set} reads for the HUD and research-gating checks that
 * will read them constantly once the Resonometer and Loom exist.
 * <p>
 * {@code @AutoRegisterCapability} gives {@link FacetCapabilities} a handle just by referencing
 * this interface, same as {@code IChunkWeaveData} does for the Weave capability.
 */
@AutoRegisterCapability
public interface IPlayerFacetKnowledge {
    Set<Identifier> getScannedEntries();

    boolean hasScanned(Identifier id);

    /**
     * Records a scan of {@code id} with the given signature: adds {@code id} to the scanned set,
     * folds its Facets into {@code knownFacets}, and marks any {@link CompoundFacet} whose both
     * halves appear in this signature as known too.
     *
     * @return {@code true} if {@code id} had not been scanned before (i.e. this is a "new
     * discovery" the caller might want to celebrate with a sound/message), {@code false} if it
     * was already known.
     */
    boolean recordScan(Identifier id, FacetSignature signature);

    Set<Facet> getKnownFacets();

    Set<CompoundFacet> getKnownCompounds();

    boolean knowsFacet(Facet facet);

    boolean knowsCompound(CompoundFacet compound);
}
