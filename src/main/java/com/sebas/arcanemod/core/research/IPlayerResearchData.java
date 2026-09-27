package com.sebas.arcanemod.core.research;

import net.minecraft.resources.Identifier;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.Set;

/**
 * Tracks which research nodes a single player has completed. {@code completedResearch} is the
 * full source of truth — "in progress" puzzle state doesn't exist yet (Section 1's nodes are all
 * "guided," completed the instant their trigger condition is met, per the design doc's "Warp-free
 * early game" note), so there's nothing else to track until the Loom puzzle checkpoint needs it.
 * <p>
 * {@code @AutoRegisterCapability} gives {@link ResearchCapabilities} a handle just by referencing
 * this interface, same as {@code IPlayerFacetKnowledge}/{@code IPlayerFrayData}.
 */
@AutoRegisterCapability
public interface IPlayerResearchData {
    Set<Identifier> getCompletedResearch();

    boolean hasCompleted(Identifier id);

    /** {@code true} if every prerequisite of {@code id} is already completed (see {@link ResearchTree#prerequisitesMet}). */
    boolean canStart(Identifier id);

    /**
     * Marks {@code id} completed.
     *
     * @return {@code true} if this was a new completion, {@code false} if it was already completed.
     */
    boolean complete(Identifier id);

    /**
     * Debug-only escape hatch (see {@code /arcane research forget}) — un-completes {@code id} so
     * a node (especially a puzzle one) can be re-tested without a fresh player/world.
     *
     * @return {@code true} if {@code id} had been completed and is now forgotten.
     */
    boolean forget(Identifier id);
}
