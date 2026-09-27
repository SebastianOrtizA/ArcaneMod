package com.sebas.arcanemod.core.facet;

/**
 * A named pairing of two primal Facets, used where recipes/research want to refer to a specific
 * "flavor" (e.g. metal, or Loomkeeper lore) rather than spelling out two raw Facets every time.
 * <p>
 * This is just the four combos the design doc names explicitly for Stage 0 — later stages add
 * more constants here as their sections need them (design doc §2.2: "Add more as needed per
 * section"), it's not meant to be an exhaustive pairing of all 12 primals.
 */
public enum CompoundFacet {
    METALLUM(Facet.IGNIS, Facet.TERRA),     // metal/smithing
    VICTUS(Facet.VITA, Facet.AQUA),         // food/growth
    HISTORIA(Facet.ORDO, Facet.COGNITIO),   // Loomkeeper lore, found on ruin blocks
    TENEBRAE(Facet.UMBRA, Facet.MORTIS);    // the core Facet of Forbidden Knowledge

    private final Facet first;
    private final Facet second;

    CompoundFacet(Facet first, Facet second) {
        this.first = first;
        this.second = second;
    }

    public Facet first() {
        return first;
    }

    public Facet second() {
        return second;
    }

    public boolean matches(Facet a, Facet b) {
        return (first == a && second == b) || (first == b && second == a);
    }
}
