package com.sebas.arcanemod.core.facet;

import net.minecraft.resources.Identifier;

import java.util.Map;

/**
 * Static, data-driven lookup from a block/item/mob's identifier to its {@link FacetSignature}.
 * <p>
 * Two layers, checked in order, both (re)computed together in {@link FacetDataLoader#apply}:
 * <ol>
 *   <li>{@code jsonOverrides} — hand-authored {@code data/arcanemod/facets/*.json}. This is the
 *   intended customization point for a user or datapack to override or add an entry without
 *   touching mod code.</li>
 *   <li>{@code vanillaDefaults} — the bulk rule-computed signatures for vanilla (and our own)
 *   content, from {@code VanillaFacetData}.</li>
 * </ol>
 */
public final class FacetRegistry {
    private static Map<Identifier, FacetSignature> jsonOverrides = Map.of();
    private static Map<Identifier, FacetSignature> vanillaDefaults = Map.of();

    private FacetRegistry() {}

    public static FacetSignature get(Identifier id) {
        FacetSignature fromJson = jsonOverrides.get(id);
        if (fromJson != null) return fromJson;
        return vanillaDefaults.getOrDefault(id, FacetSignature.EMPTY);
    }

    public static boolean has(Identifier id) {
        return jsonOverrides.containsKey(id) || vanillaDefaults.containsKey(id);
    }

    /** Called by {@link FacetDataLoader} after each resource reload — not meant for general use. */
    static void setJsonOverrides(Map<Identifier, FacetSignature> data) {
        jsonOverrides = data;
    }

    /** Called by {@link FacetDataLoader} after each resource reload — not meant for general use. */
    static void setVanillaDefaults(Map<Identifier, FacetSignature> data) {
        vanillaDefaults = data;
    }
}
