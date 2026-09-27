package com.sebas.arcanemod.network;

import com.sebas.arcanemod.client.ClientFacetKnowledge;
import com.sebas.arcanemod.core.facet.CompoundFacet;
import com.sebas.arcanemod.core.facet.Facet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.Set;

/**
 * Server -> client. Sent on login and whenever a player's {@code PlayerFacetKnowledge} changes
 * (a new Resonometer/{@code /arcane scan} discovery), so the client can render known Facets in
 * the HUD/Codex without ever needing to ask the server for them.
 * <p>
 * Only the two derived sets ride the wire — {@code scannedEntries} is server-side bookkeeping the
 * client has no use for (it only ever needs "what do I know", not "what have I pointed at").
 */
public record SyncFacetKnowledgePacket(Set<Facet> knownFacets, Set<CompoundFacet> knownCompounds) {

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncFacetKnowledgePacket> STREAM_CODEC = StreamCodec.composite(
            FacetStreamCodecs.FACET_SET, p -> Set.copyOf(p.knownFacets()),
            FacetStreamCodecs.COMPOUND_FACET_SET, p -> Set.copyOf(p.knownCompounds()),
            SyncFacetKnowledgePacket::new
    );

    /**
     * Runs on the client's main thread (see {@code ModNetwork}'s {@code addMain} registration).
     * {@link ClientFacetKnowledge} is a plain data holder with no rendering-only imports, so
     * calling into it directly here is safe on a dedicated server too — unlike the CodexScreen
     * crash, nothing referenced here is missing from the server's classpath. It just never runs
     * there, since the server never receives its own clientbound packets.
     */
    public static void handle(SyncFacetKnowledgePacket packet, CustomPayloadEvent.Context ctx) {
        ClientFacetKnowledge.set(packet.knownFacets(), packet.knownCompounds());
    }
}
