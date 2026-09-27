package com.sebas.arcanemod.client;

import com.sebas.arcanemod.client.gui.LoomOfUnderstandingScreen;
import net.minecraft.client.Minecraft;

/**
 * Isolates the single line of code that constructs a {@link LoomOfUnderstandingScreen} — same
 * dedicated-server dist-loading reason as {@code ClientCodexOpener}, see its javadoc for the full
 * explanation (JVM bytecode verification would otherwise force-load client-only {@code Screen}
 * types the moment {@code LoomOfUnderstandingBlock} itself loads, which happens on every dist).
 */
public final class ClientLoomOpener {
    private ClientLoomOpener() {}

    public static void open() {
        Minecraft.getInstance().gui.setScreen(new LoomOfUnderstandingScreen());
    }
}
