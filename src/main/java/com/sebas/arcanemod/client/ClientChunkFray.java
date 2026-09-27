package com.sebas.arcanemod.client;

/**
 * The regional Fray of the chunk the local player is currently standing in, as last synced from
 * the server. A plain static holder — the fog-tint effect in {@code FrayRenderEvents} reads from
 * here, same pattern as {@link ClientPersonalFray}.
 */
public final class ClientChunkFray {
    private static volatile int regionalFray = 0;

    private ClientChunkFray() {}

    public static void set(int value) {
        regionalFray = value;
    }

    public static int get() {
        return regionalFray;
    }
}
