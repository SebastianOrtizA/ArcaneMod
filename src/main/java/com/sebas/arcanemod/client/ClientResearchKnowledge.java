package com.sebas.arcanemod.client;

import net.minecraft.resources.Identifier;

import java.util.HashSet;
import java.util.Set;

/**
 * The local player's completed research, as last synced from the server via
 * {@code SyncResearchPacket}. A plain static holder — nothing renders from this yet, but this is
 * where the research-driven Codex rework and the Loom's node-availability display will read from.
 */
public final class ClientResearchKnowledge {
    private static Set<Identifier> completed = Set.of();

    private ClientResearchKnowledge() {}

    public static void set(Set<Identifier> ids) {
        completed = ids.isEmpty() ? Set.of() : new HashSet<>(ids);
    }

    public static boolean hasCompleted(Identifier id) {
        return completed.contains(id);
    }

    public static Set<Identifier> getCompleted() {
        return completed;
    }
}
