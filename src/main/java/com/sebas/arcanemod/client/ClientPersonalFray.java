package com.sebas.arcanemod.client;

/**
 * The local player's personal Fray, as last synced from the server. A plain static holder — the
 * Checkpoint 4 screen-vignette overlay reads from here, same pattern as {@link ClientFacetKnowledge}.
 */
public final class ClientPersonalFray {
    private static volatile int personalFray = 0;

    private ClientPersonalFray() {}

    public static void set(int value) {
        personalFray = value;
    }

    public static int get() {
        return personalFray;
    }
}
