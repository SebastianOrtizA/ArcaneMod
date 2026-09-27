package com.sebas.arcanemod.network;

import com.sebas.arcanemod.client.ClientScanResult;
import com.sebas.arcanemod.core.facet.FacetSignature;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraftforge.event.network.CustomPayloadEvent;

/**
 * Server -> client, sent once per completed Resonometer scan (see {@code ResonometerItem}). This
 * is separate from {@link SyncFacetKnowledgePacket} — that one syncs the player's whole running
 * total of known Facets/Compounds, whereas this carries just enough to render "you just scanned
 * THIS, and here's what it showed" as a HUD popup near the crosshair for a few seconds.
 */
public record ScanResultPacket(Identifier targetId, Component displayName, FacetSignature signature) {

    public static final StreamCodec<RegistryFriendlyByteBuf, ScanResultPacket> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, ScanResultPacket::targetId,
            ComponentSerialization.STREAM_CODEC, ScanResultPacket::displayName,
            FacetStreamCodecs.SIGNATURE, ScanResultPacket::signature,
            ScanResultPacket::new
    );

    /** Runs on the client's main thread — see {@code SyncFacetKnowledgePacket} for why this is safe on a dedicated server too. */
    public static void handle(ScanResultPacket packet, CustomPayloadEvent.Context ctx) {
        ClientScanResult.show(packet.displayName(), packet.signature());
    }
}
