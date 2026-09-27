package com.sebas.arcanemod.network;

import com.sebas.arcanemod.core.research.ResearchCapabilities;
import com.sebas.arcanemod.core.research.ResearchTree;
import net.minecraft.server.level.ServerPlayer;

/**
 * Small helper shared by the places that need to push a player's completed research to their
 * client: login and every new completion (see {@code event.ResearchEvents}).
 */
public final class ResearchSync {
    private ResearchSync() {}

    public static void sendTo(ServerPlayer player) {
        player.getCapability(ResearchCapabilities.PLAYER_RESEARCH_DATA).resolve().ifPresent(data ->
                ModNetwork.sendToPlayer(player, new SyncResearchPacket(data.getCompletedResearch())));
    }

    /**
     * Sent once on login — the tree's contents only change via a resource reload, which (unlike
     * a player's own completion state) has no existing "something changed, repush to everyone
     * online" hook yet, matching {@code FacetRegistry}'s equivalent scope-limitation.
     */
    public static void sendTreeTo(ServerPlayer player) {
        ModNetwork.sendToPlayer(player, new SyncResearchTreePacket(ResearchTree.asMap()));
    }
}
