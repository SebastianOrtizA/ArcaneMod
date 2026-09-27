package com.sebas.arcanemod.entity;

import com.sebas.arcanemod.ArcaneMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registry holder for the Frayed mob variants — same DeferredRegister shape as {@code ModBlocks}/{@code ModItems}. */
public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ArcaneMod.MODID);

    public static final RegistryObject<EntityType<FrayedZombie>> FRAYED_ZOMBIE = ENTITY_TYPES.register("frayed_zombie",
            () -> EntityType.Builder.of(FrayedZombie::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .build(ENTITY_TYPES.key("frayed_zombie")));

    public static final RegistryObject<EntityType<FrayedSkeleton>> FRAYED_SKELETON = ENTITY_TYPES.register("frayed_skeleton",
            () -> EntityType.Builder.of(FrayedSkeleton::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.99F)
                    .build(ENTITY_TYPES.key("frayed_skeleton")));

    public static final RegistryObject<EntityType<FrayedSpider>> FRAYED_SPIDER = ENTITY_TYPES.register("frayed_spider",
            () -> EntityType.Builder.of(FrayedSpider::new, MobCategory.MONSTER)
                    .sized(1.4F, 0.9F)
                    .build(ENTITY_TYPES.key("frayed_spider")));

    public static final RegistryObject<EntityType<SparkBoltEntity>> SPARK_BOLT = ENTITY_TYPES.register("spark_bolt",
            () -> EntityType.Builder.<SparkBoltEntity>of(SparkBoltEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(ENTITY_TYPES.key("spark_bolt")));
}
