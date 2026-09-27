package com.sebas.arcanemod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.level.Level;

/** See {@link FrayedZombie} — same "stats + looks only" corruption approach, for Skeleton. */
public class FrayedSkeleton extends Skeleton {
    public FrayedSkeleton(EntityType<? extends Skeleton> type, Level level) {
        super(type, level);
    }
}
