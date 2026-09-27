package com.sebas.arcanemod.core.research;

import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Set;

/**
 * Static holder for the research node graph, loaded from {@code data/<namespace>/research/*.json}
 * by {@link ResearchDataLoader} on every resource reload — same "static registry filled by a
 * reload listener" shape as {@code FacetRegistry}.
 */
public final class ResearchTree {
    private static Map<Identifier, ResearchNode> nodes = Map.of();

    private ResearchTree() {}

    static void set(Map<Identifier, ResearchNode> loaded) {
        nodes = Map.copyOf(loaded);
    }

    public static boolean has(Identifier id) {
        return nodes.containsKey(id);
    }

    public static ResearchNode get(Identifier id) {
        return nodes.get(id);
    }

    public static Set<Map.Entry<Identifier, ResearchNode>> entries() {
        return nodes.entrySet();
    }

    public static Map<Identifier, ResearchNode> asMap() {
        return nodes;
    }

    /** {@code true} if every prerequisite of {@code id} is present in {@code completed}. Unknown ids have no prerequisites met by definition. */
    public static boolean prerequisitesMet(Identifier id, Set<Identifier> completed) {
        ResearchNode node = nodes.get(id);
        if (node == null) return false;
        return completed.containsAll(node.prerequisites());
    }
}
