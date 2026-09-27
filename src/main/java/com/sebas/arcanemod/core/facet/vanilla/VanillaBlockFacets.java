package com.sebas.arcanemod.core.facet.vanilla;

import com.sebas.arcanemod.core.facet.Facet;
import com.sebas.arcanemod.core.facet.FacetSignature;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.sebas.arcanemod.core.facet.Facet.*;

/**
 * Tag-driven fallback for blocks not covered by {@link VanillaFacetOverrides}. Vanilla's own tags
 * already group blocks by real shared meaning (all logs, all ore of a metal, every wood's stair
 * shape...), so leaning on them keeps the long tail of block variants thematically consistent
 * instead of arbitrary, while covering the huge combinatorial explosion of shapes (stairs, slabs,
 * walls, fences, doors, trapdoors per material) in one rule each rather than one per block.
 * <p>
 * Checked in declaration order — first matching tag wins, so more specific tags are listed before
 * broader ones (e.g. ore tags before the generic stone-family rules).
 * <p>
 * Tag membership ({@code Holder#is(TagKey)}) only works once tags are bound to the registry,
 * which happens during a real resource reload — this is called from {@code FacetEvents} in
 * response to Forge's {@code TagsUpdatedEvent}, never at mod-construction time.
 */
final class VanillaBlockFacets {
    private VanillaBlockFacets() {}

    /** Purely technical/non-physical blocks — never worth a "resonance" reading. */
    private static final Set<String> EXCLUDED = Set.of(
            "air", "cave_air", "void_air", "barrier", "structure_void", "light",
            "moving_piston", "command_block", "chain_command_block", "repeating_command_block",
            "jigsaw", "structure_block"
    );

    private static final Map<TagKey<Block>, FacetSignature> TAG_RULES = new LinkedHashMap<>();

    private static void rule(TagKey<Block> tag, Facet a, int aAmt) {
        TAG_RULES.put(tag, FacetSignature.of(Map.of(a, aAmt)));
    }

    private static void rule(TagKey<Block> tag, Facet a, int aAmt, Facet b, int bAmt) {
        TAG_RULES.put(tag, FacetSignature.of(Map.of(a, aAmt, b, bAmt)));
    }

    static {
        // Living / plant matter
        rule(BlockTags.LOGS, VITA, 2, TERRA, 1);
        rule(BlockTags.LEAVES, VITA, 1, AER, 1);
        rule(BlockTags.CROPS, VITA, 2);
        rule(BlockTags.SMALL_FLOWERS, VITA, 1, LUX, 1);
        rule(BlockTags.FLOWERS, VITA, 1, LUX, 1);
        rule(BlockTags.CORALS, VITA, 1, AQUA, 2);
        rule(BlockTags.CORAL_PLANTS, VITA, 1, AQUA, 2);
        rule(BlockTags.CORAL_BLOCKS, VITA, 1, AQUA, 2);
        rule(BlockTags.NYLIUM, VITA, 1, UMBRA, 1);
        rule(BlockTags.MOSS_BLOCKS, VITA, 1, TERRA, 1);
        rule(BlockTags.WART_BLOCKS, UMBRA, 1, VITA, 1);

        // Wood shape variants (planks and everything cut from them)
        rule(BlockTags.PLANKS, VITA, 1, TERRA, 1);
        rule(BlockTags.WOODEN_STAIRS, VITA, 1, TERRA, 1);
        rule(BlockTags.WOODEN_SLABS, VITA, 1, TERRA, 1);
        rule(BlockTags.WOODEN_FENCES, VITA, 1, TERRA, 1);
        rule(BlockTags.FENCE_GATES, VITA, 1, TERRA, 1);
        rule(BlockTags.WOODEN_DOORS, VITA, 1, TERRA, 1);
        rule(BlockTags.WOODEN_TRAPDOORS, VITA, 1, TERRA, 1);
        rule(BlockTags.WOODEN_BUTTONS, VITA, 1);
        rule(BlockTags.WOODEN_PRESSURE_PLATES, VITA, 1);
        rule(BlockTags.WOODEN_SHELVES, VITA, 1, TERRA, 1);

        // Ores not already in the override list (iron/gold/copper have their own vanilla tags;
        // coal/diamond/emerald/redstone/lapis don't, so those rely entirely on the override map).
        rule(BlockTags.IRON_ORES, TERRA, 2, IGNIS, 1);
        rule(BlockTags.GOLD_ORES, TERRA, 1, LUX, 2);
        rule(BlockTags.COPPER_ORES, TERRA, 2, MOTUS, 1);

        // Stone/earth family
        rule(BlockTags.STAIRS, TERRA, 1);
        rule(BlockTags.SLABS, TERRA, 1);
        rule(BlockTags.WALLS, TERRA, 1);
        rule(BlockTags.STONE_BRICKS, TERRA, 1);
        rule(BlockTags.DIRT, TERRA, 1);
        rule(BlockTags.SAND, TERRA, 1);
        rule(BlockTags.MUD, TERRA, 1, AQUA, 1);
        rule(BlockTags.TERRACOTTA, TERRA, 2);
        rule(BlockTags.GLAZED_TERRACOTTA, TERRA, 2, ORDO, 1);
        rule(BlockTags.CONCRETE, TERRA, 2, ORDO, 1);
        rule(BlockTags.CONCRETE_POWDERS, TERRA, 2);
        rule(BlockTags.BASE_STONE_OVERWORLD, TERRA, 1);
        rule(BlockTags.BASE_STONE_NETHER, TERRA, 1, IGNIS, 1);

        // Fabric / dye / decoration
        rule(BlockTags.WOOL, VITA, 1, PERDO, 1);
        rule(BlockTags.WOOL_CARPETS, VITA, 1, PERDO, 1);
        rule(BlockTags.BEDS, VITA, 1, PERDO, 1);
        rule(BlockTags.BANNERS, ORDO, 1, PERDO, 1);
        rule(BlockTags.CANDLES, LUX, 1);
        rule(BlockTags.LANTERNS, LUX, 1);

        // Mechanism / motion
        rule(BlockTags.RAILS, MOTUS, 2);
        rule(BlockTags.ANVIL, ORDO, 1, IGNIS, 1);
        rule(BlockTags.ALL_SIGNS, ORDO, 1, COGNITIO, 1);
        rule(BlockTags.BEACON_BASE_BLOCKS, LUX, 1, ORDO, 1);

        // Water / fire / portals
        rule(BlockTags.ICE, AQUA, 2, TERRA, 1);
        rule(BlockTags.FIRE, IGNIS, 3);
        rule(BlockTags.CAMPFIRES, IGNIS, 2);
        rule(BlockTags.PORTALS, MOTUS, 2, UMBRA, 1);
    }

    /** @return null if this block should get no entry at all (a technical block). */
    static FacetSignature resolve(Block block, Identifier id) {
        if (id == null || EXCLUDED.contains(id.getPath())) return null;

        for (Map.Entry<TagKey<Block>, FacetSignature> entry : TAG_RULES.entrySet()) {
            if (block.builtInRegistryHolder().is(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Default: any other solid, placeable block carries at least a trace of Terra —
        // defensible flavor ("everything physical touches the earth somehow") rather than a
        // meaningless empty entry, per the "fully exhaustive" coverage this pass is going for.
        return FacetSignature.of(Map.of(TERRA, 1));
    }
}
