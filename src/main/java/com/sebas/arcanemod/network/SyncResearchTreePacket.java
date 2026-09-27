package com.sebas.arcanemod.network;

import com.sebas.arcanemod.client.ClientResearchTree;
import com.sebas.arcanemod.core.research.ResearchNode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.Map;

/**
 * Server -> client, sent on login. Carries the whole research tree's metadata (not just what's
 * completed — see {@code SyncResearchPacket} for that) since a client never loads
 * {@code data/arcanemod/research/*.json} on its own.
 */
public record SyncResearchTreePacket(Map<Identifier, ResearchNode> nodes) {

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncResearchTreePacket> STREAM_CODEC = StreamCodec.composite(
            ResearchStreamCodecs.TREE, p -> Map.copyOf(p.nodes()),
            SyncResearchTreePacket::new
    );

    public static void handle(SyncResearchTreePacket packet, CustomPayloadEvent.Context ctx) {
        ClientResearchTree.set(packet.nodes());
    }
}
