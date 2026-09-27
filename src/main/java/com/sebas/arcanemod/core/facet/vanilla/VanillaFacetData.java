package com.sebas.arcanemod.core.facet.vanilla;

import com.sebas.arcanemod.core.facet.FacetSignature;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Computes default Facet signatures for every vanilla (and our own) block, item, and mob — the
 * "ship default facet data for all vanilla content" line item from the Stage 0 plan (0.4).
 * <p>
 * Called from {@code FacetEvents} in response to Forge's {@code TagsUpdatedEvent} — the earliest
 * point at which tag membership checks are actually valid (see {@link VanillaBlockFacets} for
 * why this can't just run once at mod-construction time). The result feeds {@code FacetRegistry}
 * as its base layer; hand-authored {@code data/arcanemod/facets/*.json} entries (loaded
 * separately by {@code FacetDataLoader}) sit on top and can override anything computed here —
 * that's the intended path for a user or datapack to customize/correct a specific entry without
 * touching mod code.
 * <p>
 * Each registry entry is resolved through a priority chain: {@link VanillaFacetOverrides}
 * (hand-curated, checked first) → the matching {@code Vanilla*Facets.resolve()} (tag-driven
 * category fallback, possibly returning {@code null} for deliberately-excluded technical
 * entries).
 */
public final class VanillaFacetData {
    private VanillaFacetData() {}

    public static Map<Identifier, FacetSignature> compute() {
        Map<Identifier, FacetSignature> result = new LinkedHashMap<>();

        for (Block block : BuiltInRegistries.BLOCK) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            add(result, id, VanillaFacetOverrides.get().getOrDefault(id, VanillaBlockFacets.resolve(block, id)));
        }

        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            add(result, id, VanillaFacetOverrides.get().getOrDefault(id, VanillaItemFacets.resolve(item, id)));
        }

        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            add(result, id, VanillaFacetOverrides.get().getOrDefault(id, VanillaEntityFacets.resolve(type, id)));
        }

        return result;
    }

    private static void add(Map<Identifier, FacetSignature> out, Identifier id, FacetSignature signature) {
        if (signature == null || signature.isEmpty()) return;
        out.put(id, signature);
    }
}
