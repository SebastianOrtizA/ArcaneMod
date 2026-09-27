package com.sebas.arcanemod.network;

import com.sebas.arcanemod.client.ClientChunkFray;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraftforge.event.network.CustomPayloadEvent;

/**
 * Server -> client, sent whenever the player's current chunk (or that chunk's regional Fray)
 * changes — see {@code FrayEvents}. This is the regional counterpart to
 * {@link SyncPersonalFrayPacket}; the client has no way to see chunk capability state on its own,
 * so the Checkpoint 4 terrain/fog effects need this to know when to kick in.
 */
public record SyncChunkFrayPacket(int regionalFray) {

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncChunkFrayPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncChunkFrayPacket::regionalFray,
            SyncChunkFrayPacket::new
    );

    public static void handle(SyncChunkFrayPacket packet, CustomPayloadEvent.Context ctx) {
        ClientChunkFray.set(packet.regionalFray());
    }
}
