package com.sebas.arcanemod.network;

import com.sebas.arcanemod.core.research.IPlayerResearchData;
import com.sebas.arcanemod.core.research.ResearchCapabilities;
import com.sebas.arcanemod.core.research.ResearchNode;
import com.sebas.arcanemod.core.research.ResearchTree;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

/**
 * Client -> server: the player clicked an "available" node in the Loom of Understanding screen.
 * <p>
 * Unlike {@code ResearchEvents#tryComplete} (used by passive triggers, which never block on
 * prerequisites so out-of-order crafting can't soft-lock a player), a player-initiated click here
 * DOES require {@link IPlayerResearchData#canStart} to pass — this is the one path where the
 * prerequisite graph is actually enforced, matching the design doc's "connecting the right Facets
 * in the right pattern unlocks a node" framing. A client only ever offers a button for nodes it
 * already believes are available, but the server re-checks anyway rather than trusting the client.
 */
public record RequestResearchPacket(Identifier id) {

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestResearchPacket> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, RequestResearchPacket::id,
            RequestResearchPacket::new
    );

    public static void handle(RequestResearchPacket packet, CustomPayloadEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player == null) return;

        Identifier id = packet.id();
        if (!ResearchTree.has(id)) return;

        player.getCapability(ResearchCapabilities.PLAYER_RESEARCH_DATA).resolve().ifPresent(data -> {
            if (!data.canStart(id) || data.hasCompleted(id)) return;

            if (data.complete(id)) {
                ResearchSync.sendTo(player);
                ResearchNode node = ResearchTree.get(id);
                player.sendSystemMessage(Component.literal("Research unlocked: " + node.displayName()));
            }
        });
    }
}
