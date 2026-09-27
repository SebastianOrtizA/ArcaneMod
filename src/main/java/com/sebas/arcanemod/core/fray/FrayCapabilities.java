package com.sebas.arcanemod.core.fray;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

/**
 * Holds the {@link Capability} handle for {@link IPlayerFrayData}, attached to every player
 * entity (see {@code FrayEvents#onAttachPlayer}).
 */
public final class FrayCapabilities {
    public static final Capability<IPlayerFrayData> PLAYER_FRAY = CapabilityManager.get(new CapabilityToken<>() {});

    private FrayCapabilities() {}
}
