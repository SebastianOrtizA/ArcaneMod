package com.sebas.arcanemod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;

/**
 * A corrupted zombie born of Fray rather than the usual causes — same AI/behavior as a plain
 * {@link Zombie}, just tougher (see {@code FrayedMobEvents} for the attribute scaling) and
 * retextured client-side. No new behavior of its own: the "corruption" is stats + looks only,
 * per the Stage 0 plan's scope for this checkpoint.
 */
public class FrayedZombie extends Zombie {
    public FrayedZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }
}
