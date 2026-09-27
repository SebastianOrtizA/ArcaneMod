package com.sebas.arcanemod.client.gui;

import com.sebas.arcanemod.client.ClientResearchKnowledge;
import com.sebas.arcanemod.client.ClientResearchTree;
import com.sebas.arcanemod.core.research.ResearchNode;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes a Codex tab's pages from the research tree, on demand — see {@link CodexCategory}'s
 * javadoc for why nothing is precomputed/cached here.
 */
final class CodexResearchPages {
    private CodexResearchPages() {}

    static List<CodexPage> forSection(int section) {
        List<Map.Entry<Identifier, ResearchNode>> nodes = new ArrayList<>();
        for (Map.Entry<Identifier, ResearchNode> entry : ClientResearchTree.entries()) {
            if (entry.getValue().section() == section) {
                nodes.add(entry);
            }
        }

        if (nodes.isEmpty()) {
            return List.of(new CodexPage("Not Yet Written", "Thale hasn't gotten around to this section yet."));
        }

        // No explicit ordering field on a research node — sorting by prerequisite-chain depth
        // reproduces the design doc's intended "story order" (e.g. Section 1's Awakening before
        // Sensing the Weave before Wyrdstone Refining, ...) without needing one.
        Map<Identifier, Integer> depths = new HashMap<>();
        for (Map.Entry<Identifier, ResearchNode> entry : nodes) {
            depthOf(entry.getKey(), depths);
        }
        nodes.sort(Comparator.<Map.Entry<Identifier, ResearchNode>>comparingInt(e -> depths.get(e.getKey()))
                .thenComparing(e -> e.getValue().displayName()));

        List<CodexPage> pages = new ArrayList<>();
        for (Map.Entry<Identifier, ResearchNode> entry : nodes) {
            Identifier id = entry.getKey();
            ResearchNode node = entry.getValue();

            if (ClientResearchKnowledge.hasCompleted(id)) {
                String body = node.loreText().isEmpty() ? node.description() : node.loreText();
                pages.add(new CodexPage(node.displayName(), body));
            } else {
                pages.add(new CodexPage("~ torn page ~", "Something is missing here. Keep researching."));
            }
        }
        return pages;
    }

    /** Longest prerequisite chain, memoized. {@code memo.put(id, 0)} before recursing guards against a cyclic chain looping forever. */
    private static int depthOf(Identifier id, Map<Identifier, Integer> memo) {
        Integer cached = memo.get(id);
        if (cached != null) return cached;

        memo.put(id, 0);
        ResearchNode node = ClientResearchTree.get(id);
        int depth = 0;
        if (node != null) {
            for (Identifier prerequisite : node.prerequisites()) {
                depth = Math.max(depth, depthOf(prerequisite, memo) + 1);
            }
        }
        memo.put(id, depth);
        return depth;
    }
}
