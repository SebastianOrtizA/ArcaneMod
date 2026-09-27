package com.sebas.arcanemod.network;

import com.sebas.arcanemod.ArcaneMod;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

/**
 * Phase 0.B's first real network channel — everything before this (wand Wyrd, Wellspring charge,
 * chunk density) rode data components or block-entity update tags, which need no packets of our
 * own. Player Facet knowledge is per-player, server-authoritative state with no existing sync
 * path, so it needs one.
 * <p>
 * Uses the classic {@code SimpleChannel} API (via {@code ChannelBuilder}) rather than the newer
 * {@code CustomPacketPayload} registration — this is still the supported, non-deprecated way to
 * build a channel in this Forge version (only the old {@code messageBuilder} methods on
 * {@code SimpleChannel} itself are deprecated, in favor of the {@code play()/clientbound()/add()}
 * chain used below).
 */
public final class ModNetwork {
    private static final SimpleChannel CHANNEL = ChannelBuilder
            .named(Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "main"))
            .networkProtocolVersion(1)
            .simpleChannel();

    private ModNetwork() {}

    /** Called once from {@code ArcaneMod}'s common setup. */
    public static void register() {
        CHANNEL.play().clientbound()
                .addMain(SyncFacetKnowledgePacket.class, SyncFacetKnowledgePacket.STREAM_CODEC, SyncFacetKnowledgePacket::handle)
                .addMain(ScanResultPacket.class, ScanResultPacket.STREAM_CODEC, ScanResultPacket::handle)
                .addMain(SyncPersonalFrayPacket.class, SyncPersonalFrayPacket.STREAM_CODEC, SyncPersonalFrayPacket::handle)
                .addMain(SyncChunkFrayPacket.class, SyncChunkFrayPacket.STREAM_CODEC, SyncChunkFrayPacket::handle)
                .addMain(SyncResearchPacket.class, SyncResearchPacket.STREAM_CODEC, SyncResearchPacket::handle)
                .addMain(SyncResearchTreePacket.class, SyncResearchTreePacket.STREAM_CODEC, SyncResearchTreePacket::handle);

        // The Loom screen's node-click button is the first thing that needs the client to tell
        // the server anything — everything before Phase 0.D was purely server -> client sync.
        CHANNEL.play().serverbound()
                .addMain(RequestResearchPacket.class, RequestResearchPacket.STREAM_CODEC, RequestResearchPacket::handle)
                .addMain(CycleFocusPacket.class, CycleFocusPacket.STREAM_CODEC, CycleFocusPacket::handle);
    }

    /** Untyped on purpose — {@code SimpleChannel} dispatches by the message's own runtime class. */
    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(packet, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToServer(Object packet) {
        CHANNEL.send(packet, PacketDistributor.SERVER.noArg());
    }
}
