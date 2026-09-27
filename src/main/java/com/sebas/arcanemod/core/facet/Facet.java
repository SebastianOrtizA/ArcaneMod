package com.sebas.arcanemod.core.facet;

/**
 * The twelve primal Facets — the mod's aspect/essentia equivalent. Every block, item, and mob
 * has a signature made up of these (see {@link FacetSignature}); pairs of them combine into
 * {@link CompoundFacet}s for recipes/research that want a more specific flavor than "some fire
 * and some earth".
 */
public enum Facet {
    IGNIS,    // fire
    AQUA,     // water
    TERRA,    // earth
    AER,      // air
    LUX,      // light
    UMBRA,    // shadow
    VITA,     // life
    MORTIS,   // death
    ORDO,     // order
    PERDO,    // entropy/decay
    MOTUS,    // motion
    COGNITIO  // thought/knowledge
}
