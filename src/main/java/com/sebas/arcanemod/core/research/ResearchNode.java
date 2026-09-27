package com.sebas.arcanemod.core.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sebas.arcanemod.core.facet.Facet;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Locale;

/**
 * The on-disk shape of one {@code data/<namespace>/research/*.json} entry. The node's own id is
 * NOT part of this record — it's derived from the file's own location by {@link ResearchDataLoader}
 * (same as a loot table or recipe is keyed by its path), so there's nothing to keep in sync between
 * a filename and a duplicated "id" field inside it.
 * <p>
 * {@code unlocks} is an opaque list of ids future work (recipe/page unlocking) will interpret.
 * <p>
 * {@code pattern} is the Loom puzzle's target: the set of Facet-pairs the player must connect on
 * the board (drawn from {@code requiredFacets}, which are the nodes actually placed on that
 * board). An empty {@code pattern} means the node has no puzzle at all — clicking it in the Loom's
 * list completes it directly, which is how all six of Section 1's "guided" nodes work (their real
 * trigger is a gameplay action — crafting, scanning, channeling — not a Facet-connecting puzzle).
 * {@code difficulty} only matters once {@code pattern} is non-empty: at difficulty <= 1 the Loom
 * shows the target pattern as a hint (design doc: "pattern is shown as a hint, almost no puzzle").
 */
public record ResearchNode(
        int section,
        String displayName,
        String description,
        List<Facet> requiredFacets,
        int difficulty,
        List<Identifier> prerequisites,
        List<Identifier> unlocks,
        String loreText,
        List<FacetConnection> pattern
) {
    private static final Codec<Facet> FACET_CODEC = Codec.STRING.xmap(
            s -> Facet.valueOf(s.toUpperCase(Locale.ROOT)),
            f -> f.name().toLowerCase(Locale.ROOT));

    public static final Codec<ResearchNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("section").forGetter(ResearchNode::section),
            Codec.STRING.fieldOf("display_name").forGetter(ResearchNode::displayName),
            Codec.STRING.optionalFieldOf("description", "").forGetter(ResearchNode::description),
            FACET_CODEC.listOf().optionalFieldOf("required_facets", List.of()).forGetter(ResearchNode::requiredFacets),
            Codec.INT.optionalFieldOf("difficulty", 0).forGetter(ResearchNode::difficulty),
            Identifier.CODEC.listOf().optionalFieldOf("prerequisites", List.of()).forGetter(ResearchNode::prerequisites),
            Identifier.CODEC.listOf().optionalFieldOf("unlocks", List.of()).forGetter(ResearchNode::unlocks),
            Codec.STRING.optionalFieldOf("lore_text", "").forGetter(ResearchNode::loreText),
            FacetConnection.CODEC.listOf().optionalFieldOf("pattern", List.of()).forGetter(ResearchNode::pattern)
    ).apply(instance, ResearchNode::new));
}
