package com.sebas.arcanemod.network;

import com.sebas.arcanemod.core.facet.FacetCapabilities;
import net.minecraft.server.level.ServerPlayer;

/**
 * Small helper shared by the places that need to push a player's current Facet knowledge to
 * their client: login (see {@code FacetEvents}) and every new scan (see {@code ArcaneCommands}'s
 * {@code /arcane scan}, later replaced/joined by the real Resonometer).
 */
public final class FacetKnowledgeSync {
    private FacetKnowledgeSync() {}

    public static void sendTo(ServerPlayer player) {
        player.getCapability(FacetCapabilities.PLAYER_FACET_KNOWLEDGE).resolve().ifPresent(knowledge ->
                ModNetwork.sendToPlayer(player,
                        new SyncFacetKnowledgePacket(knowledge.getKnownFacets(), knowledge.getKnownCompounds())));
    }
}
