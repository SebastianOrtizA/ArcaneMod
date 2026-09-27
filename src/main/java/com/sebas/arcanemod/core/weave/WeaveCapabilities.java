package com.sebas.arcanemod.core.weave;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

/**
 * Holds the {@link Capability} handle for {@link IChunkWeaveData}. This is the classic Forge
 * capability system (Capability/CapabilityToken/LazyOptional) — this Forge version still uses
 * it for arbitrary objects like chunks; it's only ItemStacks that moved to data components
 * (see {@link com.sebas.arcanemod.core.ModDataComponents}).
 */
public final class WeaveCapabilities {
    public static final Capability<IChunkWeaveData> CHUNK_WEAVE = CapabilityManager.get(new CapabilityToken<>() {});

    private WeaveCapabilities() {}
}
