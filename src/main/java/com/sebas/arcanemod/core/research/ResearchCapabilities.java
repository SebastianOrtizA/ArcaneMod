package com.sebas.arcanemod.core.research;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

/**
 * Holds the {@link Capability} handle for {@link IPlayerResearchData}, attached to every player
 * entity (see {@code ResearchEvents#onAttachPlayer}).
 */
public final class ResearchCapabilities {
    public static final Capability<IPlayerResearchData> PLAYER_RESEARCH_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});

    private ResearchCapabilities() {}
}
