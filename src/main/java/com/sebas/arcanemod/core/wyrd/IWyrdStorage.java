package com.sebas.arcanemod.core.wyrd;

/**
 * Generic Wyrd energy storage — implemented by anything that can hold Wyrd (wands now;
 * Wyrdstone Batteries and Anchor Braziers in later stages).
 * <p>
 * Mirrors the shape of Forge's own {@code IEnergyStorage} deliberately, but kept as our own
 * interface since Wyrd isn't Forge Energy — it has its own draw/regen rules tied to Weave
 * density rather than being a generic power unit.
 */
public interface IWyrdStorage {
    int getWyrd();

    int getMaxWyrd();

    /**
     * @param simulate if true, don't actually change stored Wyrd — just report what would happen
     * @return the amount actually received (capped by remaining capacity)
     */
    int receiveWyrd(int amount, boolean simulate);

    /**
     * @param simulate if true, don't actually change stored Wyrd — just report what would happen
     * @return the amount actually extracted (capped by what's stored)
     */
    int extractWyrd(int amount, boolean simulate);
}
