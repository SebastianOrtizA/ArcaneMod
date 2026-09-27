package com.sebas.arcanemod.client;

import com.sebas.arcanemod.core.facet.CompoundFacet;
import com.sebas.arcanemod.core.facet.Facet;

import java.util.EnumSet;
import java.util.Set;

/**
 * The local player's Facet knowledge, as last synced from the server via
 * {@code SyncFacetKnowledgePacket}. A plain static holder — no rendering yet, but this is where
 * the Resonometer's HUD (Checkpoint 4) and any future Codex "known Facets" page will read from,
 * so it doesn't need to ask the server every frame.
 */
public final class ClientFacetKnowledge {
    private static Set<Facet> knownFacets = EnumSet.noneOf(Facet.class);
    private static Set<CompoundFacet> knownCompounds = EnumSet.noneOf(CompoundFacet.class);

    private ClientFacetKnowledge() {}

    public static void set(Set<Facet> facets, Set<CompoundFacet> compounds) {
        knownFacets = facets.isEmpty() ? EnumSet.noneOf(Facet.class) : EnumSet.copyOf(facets);
        knownCompounds = compounds.isEmpty() ? EnumSet.noneOf(CompoundFacet.class) : EnumSet.copyOf(compounds);
    }

    public static Set<Facet> getKnownFacets() {
        return knownFacets;
    }

    public static Set<CompoundFacet> getKnownCompounds() {
        return knownCompounds;
    }
}
