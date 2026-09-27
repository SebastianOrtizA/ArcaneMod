package com.sebas.arcanemod.client;

import com.sebas.arcanemod.core.research.ResearchNode;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Set;

/**
 * The full research node graph, as last synced from the server via {@code SyncResearchTreePacket}.
 * Unlike {@link ClientResearchKnowledge} (which player), this is the same for every player — it's
 * the server's {@code ResearchTree} data pack content, mirrored client-side because a standalone
 * client never loads {@code data/} JSON on its own (only {@code assets/}); the Loom screen needs
 * every node's metadata, not just which ones are completed, to render locked/available entries.
 */
public final class ClientResearchTree {
    private static Map<Identifier, ResearchNode> nodes = Map.of();

    private ClientResearchTree() {}

    public static void set(Map<Identifier, ResearchNode> loaded) {
        nodes = Map.copyOf(loaded);
    }

    public static ResearchNode get(Identifier id) {
        return nodes.get(id);
    }

    public static Set<Map.Entry<Identifier, ResearchNode>> entries() {
        return nodes.entrySet();
    }
}
