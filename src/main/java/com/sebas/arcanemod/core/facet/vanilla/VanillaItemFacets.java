package com.sebas.arcanemod.core.facet.vanilla;

import com.sebas.arcanemod.core.facet.Facet;
import com.sebas.arcanemod.core.facet.FacetSignature;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.sebas.arcanemod.core.facet.Facet.*;

/** Tag-driven fallback for items not covered by {@link VanillaFacetOverrides}. */
final class VanillaItemFacets {
    private VanillaItemFacets() {}

    private static final Set<String> EXCLUDED = Set.of(
            "barrier", "light", "structure_void", "command_block", "chain_command_block",
            "repeating_command_block", "debug_stick", "jigsaw", "structure_block"
    );

    private static final Map<TagKey<Item>, FacetSignature> TAG_RULES = new LinkedHashMap<>();

    private static void rule(TagKey<Item> tag, Facet a, int aAmt) {
        TAG_RULES.put(tag, FacetSignature.of(Map.of(a, aAmt)));
    }

    private static void rule(TagKey<Item> tag, Facet a, int aAmt, Facet b, int bAmt) {
        TAG_RULES.put(tag, FacetSignature.of(Map.of(a, aAmt, b, bAmt)));
    }

    static {
        // Ores that don't have a dedicated vanilla BLOCK tag still have an ITEM tag for what
        // they drop, so this catches coal/diamond/emerald/redstone/lapis ore's item form even
        // though VanillaBlockFacets can't reach their block form the same way.
        rule(ItemTags.COAL_ORES, IGNIS, 2, TERRA, 1);
        rule(ItemTags.IRON_ORES, TERRA, 2, IGNIS, 1);
        rule(ItemTags.GOLD_ORES, TERRA, 1, LUX, 2);
        rule(ItemTags.COPPER_ORES, TERRA, 2, MOTUS, 1);
        rule(ItemTags.DIAMOND_ORES, TERRA, 1, LUX, 2);
        rule(ItemTags.EMERALD_ORES, TERRA, 1, COGNITIO, 2);
        rule(ItemTags.REDSTONE_ORES, MOTUS, 2, TERRA, 1);
        rule(ItemTags.LAPIS_ORES, COGNITIO, 2, LUX, 1);
        rule(ItemTags.COALS, IGNIS, 2);

        rule(ItemTags.LOGS, VITA, 2, TERRA, 1);
        rule(ItemTags.LEAVES, VITA, 1, AER, 1);
        rule(ItemTags.SAPLINGS, VITA, 2);
        rule(ItemTags.PLANKS, VITA, 1, TERRA, 1);

        rule(ItemTags.WOOL, VITA, 1, PERDO, 1);
        rule(ItemTags.WOOL_CARPETS, VITA, 1, PERDO, 1);
        rule(ItemTags.BEDS, VITA, 1, PERDO, 1);
        rule(ItemTags.BANNERS, ORDO, 1, PERDO, 1);
        rule(ItemTags.DYES, PERDO, 1, LUX, 1);
        rule(ItemTags.CANDLES, LUX, 1);
        rule(ItemTags.TERRACOTTA, TERRA, 2);
        rule(ItemTags.GLAZED_TERRACOTTA, TERRA, 2, ORDO, 1);
        rule(ItemTags.CONCRETE, TERRA, 2, ORDO, 1);
        rule(ItemTags.CONCRETE_POWDERS, TERRA, 2);
        rule(ItemTags.SAND, TERRA, 1);
        rule(ItemTags.DIRT, TERRA, 1);
        rule(ItemTags.MUD, TERRA, 1, AQUA, 1);
        rule(ItemTags.WALLS, TERRA, 1);
        rule(ItemTags.STONE_BRICKS, TERRA, 1);

        rule(ItemTags.SWORDS, MOTUS, 1, PERDO, 1);
        rule(ItemTags.AXES, MOTUS, 1, PERDO, 1);
        rule(ItemTags.PICKAXES, MOTUS, 1, ORDO, 1);
        rule(ItemTags.SHOVELS, MOTUS, 1, ORDO, 1);
        rule(ItemTags.HOES, MOTUS, 1, VITA, 1);
        rule(ItemTags.SPEARS, MOTUS, 1, PERDO, 1);
        rule(ItemTags.ARROWS, MOTUS, 2);
        rule(ItemTags.HEAD_ARMOR, ORDO, 1);
        rule(ItemTags.CHEST_ARMOR, ORDO, 1);
        rule(ItemTags.LEG_ARMOR, ORDO, 1);
        rule(ItemTags.FOOT_ARMOR, ORDO, 1);
        rule(ItemTags.SKULLS, MORTIS, 1, COGNITIO, 1);
        rule(ItemTags.TRIM_MATERIALS, ORDO, 1, LUX, 1);
        rule(ItemTags.DECORATED_POT_SHERDS, ORDO, 1, COGNITIO, 1);

        rule(ItemTags.BOATS, MOTUS, 1, AQUA, 1);
        rule(ItemTags.CHEST_BOATS, MOTUS, 1, AQUA, 1);
        rule(ItemTags.FISHES, VITA, 1, AQUA, 1);
        rule(ItemTags.MEAT, VITA, 1);
        rule(ItemTags.EGGS, VITA, 2);
        rule(ItemTags.METAL_NUGGETS, TERRA, 1);
        rule(ItemTags.BUNDLES, ORDO, 1);
        rule(ItemTags.SIGNS, ORDO, 1, COGNITIO, 1);
        rule(ItemTags.HANGING_SIGNS, ORDO, 1, COGNITIO, 1);
        rule(ItemTags.SHULKER_BOXES, ORDO, 2, UMBRA, 1);
    }

    /** @return null if this item should get no entry at all (a technical item). */
    static FacetSignature resolve(Item item, Identifier id) {
        if (id == null || EXCLUDED.contains(id.getPath())) return null;

        if (id.getPath().endsWith("_spawn_egg")) {
            return resolveSpawnEgg(id.getPath());
        }

        for (Map.Entry<TagKey<Item>, FacetSignature> entry : TAG_RULES.entrySet()) {
            if (item.builtInRegistryHolder().is(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Default: most items are crafted from something of the earth. Deliberately not applied
        // to items EXCLUDED above (technical items get no entry at all).
        //
        // Note: this used to check DataComponents.FOOD and scale Vita with nutrition, so plain
        // foods without an explicit override still read as faintly alive. Turns out item default
        // components aren't bound yet even this late — reload listeners are strictly ordered
        // after tag binding, but component binding happens later still, throwing
        // "NullPointerException: Components not bound yet" from Item#components(). Common foods
        // are hand-listed in VanillaFacetOverrides instead now (see CLAUDE.md).
        return FacetSignature.of(Map.of(TERRA, 1));
    }

    /**
     * A spawn egg is really "a bottled version of the mob it spawns" — its Facets should be the
     * mob's, not something derived independently. {@code minecraft:zombie_spawn_egg} strips down
     * to {@code minecraft:zombie} and looks that entity type up directly.
     */
    private static FacetSignature resolveSpawnEgg(String path) {
        String mobPath = path.substring(0, path.length() - "_spawn_egg".length());
        Identifier mobId = Identifier.withDefaultNamespace(mobPath);
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(mobId).orElse(null);
        if (type == null) return FacetSignature.of(Map.of(VITA, 1));

        FacetSignature mobSignature = VanillaFacetOverrides.get().getOrDefault(mobId, VanillaEntityFacets.resolve(type, mobId));
        return mobSignature != null ? mobSignature : FacetSignature.of(Map.of(VITA, 1));
    }
}
