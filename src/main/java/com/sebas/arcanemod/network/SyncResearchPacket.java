package com.sebas.arcanemod.network;

import com.sebas.arcanemod.client.ClientResearchKnowledge;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Server -> client. Sent on login and whenever a player completes a research node, so the client
 * knows what's unlocked without asking — same pattern as {@code SyncFacetKnowledgePacket}.
 */
public record SyncResearchPacket(Set<Identifier> completedResearch) {

    /**
     * A plain {@code ByteBuf} codec (Identifier's own {@code STREAM_CODEC} needs no registry
     * access) — satisfies {@code StreamCodec<? super RegistryFriendlyByteBuf, ...>} in the
     * composite below the same way {@code FacetStreamCodecs.FACET_SET} does.
     */
    private static final StreamCodec<ByteBuf, Set<Identifier>> COMPLETED_SET =
            ByteBufCodecs.collection(HashSet::new, Identifier.STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncResearchPacket> STREAM_CODEC = StreamCodec.composite(
            COMPLETED_SET, p -> Set.copyOf(p.completedResearch()),
            SyncResearchPacket::new
    );

    public static void handle(SyncResearchPacket packet, CustomPayloadEvent.Context ctx) {
        ClientResearchKnowledge.set(packet.completedResearch());
    }
}
