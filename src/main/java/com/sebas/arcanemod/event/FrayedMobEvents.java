package com.sebas.arcanemod.event;

import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.core.weave.ChunkWeaveAccess;
import com.sebas.arcanemod.core.weave.IChunkWeaveData;
import com.sebas.arcanemod.entity.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Frayed mob variants (Stage 0 plan, "Frayed Mob Spawning"): corrupted zombie/skeleton/spider
 * with boosted stats, swapped in for their vanilla counterpart's natural spawns once a chunk's
 * regional Fray gets high enough. No new AI — same behavior as the vanilla mob, just tougher and
 * (client-side) retextured.
 */
@Mod.EventBusSubscriber(modid = ArcaneMod.MODID)
public class FrayedMobEvents {

    /** Design doc: 51–80 "enable Frayed mob spawning", 81–100 "increase spawn rate". */
    private static final int FRAY_SPAWN_THRESHOLD = 50;
    private static final int FRAY_HIGH_THRESHOLD = 80;
    private static final float BASE_REPLACE_CHANCE = 0.5F;
    private static final float HIGH_REPLACE_CHANCE = 0.85F;

    private static final double HEALTH_MULTIPLIER = 1.2;
    private static final double DAMAGE_MULTIPLIER = 1.1;

    private static final Map<EntityType<?>, Supplier<EntityType<?>>> REPLACEMENTS = Map.of(
            EntityTypes.ZOMBIE, ModEntityTypes.FRAYED_ZOMBIE::get,
            EntityTypes.SKELETON, ModEntityTypes.FRAYED_SKELETON::get,
            EntityTypes.SPIDER, ModEntityTypes.FRAYED_SPIDER::get
    );

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.FRAYED_ZOMBIE.get(), scaled(Zombie::createAttributes));
        event.put(ModEntityTypes.FRAYED_SKELETON.get(), scaled(AbstractSkeleton::createAttributes));
        event.put(ModEntityTypes.FRAYED_SPIDER.get(), scaled(Spider::createAttributes));
    }

    /**
     * Builds a Frayed variant's attributes from the vanilla mob's own builder, scaled by
     * {@link #HEALTH_MULTIPLIER}/{@link #DAMAGE_MULTIPLIER} — reads vanilla's base values rather
     * than hand-copying them, so this stays correct even if a future MC update changes them.
     */
    private static AttributeSupplier scaled(Supplier<AttributeSupplier.Builder> factory) {
        AttributeSupplier vanilla = factory.get().build();
        double health = vanilla.getBaseValue(Attributes.MAX_HEALTH) * HEALTH_MULTIPLIER;

        AttributeSupplier.Builder builder = factory.get().add(Attributes.MAX_HEALTH, health);
        if (vanilla.hasAttribute(Attributes.ATTACK_DAMAGE)) {
            builder.add(Attributes.ATTACK_DAMAGE, vanilla.getBaseValue(Attributes.ATTACK_DAMAGE) * DAMAGE_MULTIPLIER);
        }
        return builder.build();
    }

    /**
     * Intercepts a natural zombie/skeleton/spider spawn and probabilistically replaces it with
     * its Frayed counterpart, based on the spawn chunk's regional Fray. {@code setSpawnCancelled}
     * stops the original mob from actually joining the world; the replacement is spawned fresh at
     * the same position.
     */
    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getSpawnReason() != EntitySpawnReason.NATURAL) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        Mob original = event.getEntity();
        Supplier<EntityType<?>> replacementSupplier = REPLACEMENTS.get(original.getType());
        if (replacementSupplier == null) return;

        BlockPos pos = BlockPos.containing(event.getX(), event.getY(), event.getZ());
        int fray = ChunkWeaveAccess.get(serverLevel.getChunkAt(pos))
                .map(IChunkWeaveData::getRegionalFray)
                .orElse(0);
        if (fray <= FRAY_SPAWN_THRESHOLD) return;

        float chance = fray > FRAY_HIGH_THRESHOLD ? HIGH_REPLACE_CHANCE : BASE_REPLACE_CHANCE;
        if (serverLevel.getRandom().nextFloat() >= chance) return;

        event.setSpawnCancelled(true);
        Entity replacement = replacementSupplier.get().create(serverLevel, EntitySpawnReason.NATURAL);
        if (replacement == null) return;

        replacement.snapTo(event.getX(), event.getY(), event.getZ(), original.getYRot(), original.getXRot());
        // create() only constructs the entity — it does NOT run Mob#finalizeSpawn, which is what
        // vanilla uses to hand skeletons their bow, roll zombie armor chances, etc. Without this
        // call the replacement spawns with none of that default equipment.
        if (replacement instanceof Mob mobReplacement) {
            mobReplacement.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pos), EntitySpawnReason.NATURAL, null);
        }
        serverLevel.addFreshEntity(replacement);
    }
}
