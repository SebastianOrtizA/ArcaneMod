package com.sebas.arcanemod.core.fray;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

/**
 * A player's personal Fray (0–100) — the corruption cost of practicing Forbidden arts, wearing
 * Duskbound equipment, or lingering in heavily Frayed land. Only the last of those three sources
 * exists yet in Stage 0 (see {@code FrayEvents}); the field is here now so later stages don't
 * need a save-format migration, the same reasoning {@code IChunkWeaveData.regionalFray} used.
 */
@AutoRegisterCapability
public interface IPlayerFrayData {
    int getPersonalFray();

    void setPersonalFray(int fray);
}
