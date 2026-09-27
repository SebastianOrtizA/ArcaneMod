package com.sebas.arcanemod.network;

import com.sebas.arcanemod.core.fray.FrayCapabilities;
import net.minecraft.server.level.ServerPlayer;

/** Mirrors {@link FacetKnowledgeSync} — pushes a player's current personal Fray to their client. */
public final class FraySync {
    private FraySync() {}

    public static void sendTo(ServerPlayer player) {
        player.getCapability(FrayCapabilities.PLAYER_FRAY).resolve().ifPresent(data ->
                ModNetwork.sendToPlayer(player, new SyncPersonalFrayPacket(data.getPersonalFray())));
    }
}
