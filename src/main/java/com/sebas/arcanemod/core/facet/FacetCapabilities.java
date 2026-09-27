package com.sebas.arcanemod.core.facet;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

/**
 * Holds the {@link Capability} handle for {@link IPlayerFacetKnowledge}, attached to every
 * player entity (see {@code FacetEvents#onAttachPlayer}).
 */
public final class FacetCapabilities {
    public static final Capability<IPlayerFacetKnowledge> PLAYER_FACET_KNOWLEDGE =
            CapabilityManager.get(new CapabilityToken<>() {});

    private FacetCapabilities() {}
}
