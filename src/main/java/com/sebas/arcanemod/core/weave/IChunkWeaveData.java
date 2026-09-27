package com.sebas.arcanemod.core.weave;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

/**
 * Per-chunk Weave state.
 * <p>
 * {@code baseDensity} is fixed by the chunk's biome (see {@link WeaveDensity}) and never
 * changes on its own. {@code currentDensity} is the runtime value Wellsprings actually draw
 * from — Wyrd overdraw pushes it down, and it slowly recovers back toward {@code baseDensity}
 * over time (see {@code WeaveEvents}'s regen tick).
 * <p>
 * {@code regionalFray} is stored here too, ahead of the Fray phase actually using it — later
 * checkpoints will read/write it, but nothing sets it above 0 yet, so it's inert for now.
 * <p>
 * {@code @AutoRegisterCapability} is what lets {@link WeaveCapabilities} obtain a
 * {@code Capability<IChunkWeaveData>} handle just by referencing this interface — no separate
 * registration event needed.
 */
@AutoRegisterCapability
public interface IChunkWeaveData {
    float getBaseDensity();

    void setBaseDensity(float density);

    float getCurrentDensity();

    void setCurrentDensity(float density);

    int getRegionalFray();

    void setRegionalFray(int fray);
}
