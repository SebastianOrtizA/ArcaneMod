package com.sebas.arcanemod.client;

import com.sebas.arcanemod.client.gui.CodexScreen;
import net.minecraft.client.Minecraft;

/**
 * Isolates the single line of code that constructs a {@link CodexScreen} (and therefore
 * touches the client-only {@code Screen} class hierarchy) in its own class.
 * <p>
 * This matters on a dedicated server: JVM bytecode verification resolves the classes a
 * method's instructions reference (e.g. a {@code NEW CodexScreen} instruction needs to
 * check the class isn't abstract), and that resolution happens when the *containing*
 * class loads — not only when the method is actually called. If this code lived directly
 * in {@code CodexArcanumItem}, merely loading that item class (which happens on every
 * dist, including DEDICATED_SERVER, just to register the item) would force-load
 * {@code CodexScreen} and its superclass {@code Screen}, which Forge's RuntimeDistCleaner
 * rejects on a server. Keeping it in a separate class means that class — and the client
 * types it touches — is only ever loaded when {@link #open()} is actually invoked, which
 * only happens from client-side code.
 */
public final class ClientCodexOpener {
    private ClientCodexOpener() {}

    public static void open() {
        Minecraft.getInstance().gui.setScreen(new CodexScreen());
    }
}
