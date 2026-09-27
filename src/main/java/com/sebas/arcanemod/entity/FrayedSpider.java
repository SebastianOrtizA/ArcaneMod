package com.sebas.arcanemod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;

/** See {@link FrayedZombie} — same "stats + looks only" corruption approach, for Spider. */
public class FrayedSpider extends Spider {
    public FrayedSpider(EntityType<? extends Spider> type, Level level) {
        super(type, level);
    }
}
