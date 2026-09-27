package com.sebas.arcanemod.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SparkBoltEntity extends AbstractHurtingProjectile {
    private static final float DAMAGE = 3.0F;
    private static final int MAX_LIFETIME_TICKS = 40;

    public SparkBoltEntity(EntityType<? extends SparkBoltEntity> type, Level level) {
        super(type, level);
        this.accelerationPower = 0.05;
    }

    public SparkBoltEntity(Level level, LivingEntity shooter, Vec3 direction) {
        super(ModEntityTypes.SPARK_BOLT.get(), shooter, direction, level);
        this.accelerationPower = 0.05;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (this.level() instanceof ServerLevel serverLevel) {
            Entity target = hitResult.getEntity();
            Entity owner = this.getOwner();
            DamageSource source = this.damageSources().indirectMagic(this, owner);
            target.hurtServer(serverLevel, source, DAMAGE);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z,
                    6, 0.2, 0.2, 0.2, 0.02);
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > MAX_LIFETIME_TICKS) {
            this.discard();
        }
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected @Nullable ParticleOptions getTrailParticle() {
        return ParticleTypes.FLAME;
    }

    @Override
    protected float getInertia() {
        return 1.0F;
    }
}
