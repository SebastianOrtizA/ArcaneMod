package com.sebas.arcanemod.client;

import com.sebas.arcanemod.core.facet.Facet;
import com.sebas.arcanemod.core.facet.FacetSignature;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * The most recent Resonometer scan result, as pushed by {@code ScanResultPacket}. A plain static
 * holder read by {@link ResonometerHud} — display-only, not persisted, and replaced/expired on
 * its own so there's no need to explicitly clear it between scans.
 */
public final class ClientScanResult {
    private static final long DISPLAY_DURATION_MILLIS = 4000;

    private static volatile ClientScanResult current;

    private final Component displayName;
    private final List<Component> lines;
    private final long expiresAtMillis;

    private ClientScanResult(Component displayName, List<Component> lines) {
        this.displayName = displayName;
        this.lines = lines;
        this.expiresAtMillis = System.currentTimeMillis() + DISPLAY_DURATION_MILLIS;
    }

    public static void show(Component displayName, FacetSignature signature) {
        current = new ClientScanResult(displayName, describe(signature));
    }

    /** Returns null once nothing has been scanned yet, or the last result has expired. */
    public static ClientScanResult current() {
        ClientScanResult snapshot = current;
        return snapshot == null || System.currentTimeMillis() > snapshot.expiresAtMillis ? null : snapshot;
    }

    public Component displayName() {
        return displayName;
    }

    public List<Component> lines() {
        return lines;
    }

    private static List<Component> describe(FacetSignature signature) {
        if (signature.isEmpty()) {
            return List.of(Component.translatable("hud.arcanemod.no_resonance"));
        }
        return signature.asMap().entrySet().stream()
                .<Component>map(entry -> Component.literal(titleCase(entry.getKey()) + " " + entry.getValue()))
                .toList();
    }

    private static String titleCase(Facet facet) {
        String name = facet.name();
        return name.charAt(0) + name.substring(1).toLowerCase(Locale.ROOT);
    }
}
