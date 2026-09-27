package com.sebas.arcanemod.network;

import com.sebas.arcanemod.client.ClientPersonalFray;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraftforge.event.network.CustomPayloadEvent;

/**
 * Server -> client. Sent on login and whenever a player's personal Fray changes, so the (future)
 * screen-edge vignette in Checkpoint 4 has something to read without asking the server per frame.
 */
public record SyncPersonalFrayPacket(int personalFray) {

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPersonalFrayPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncPersonalFrayPacket::personalFray,
            SyncPersonalFrayPacket::new
    );

    public static void handle(SyncPersonalFrayPacket packet, CustomPayloadEvent.Context ctx) {
        ClientPersonalFray.set(packet.personalFray());
    }
}
