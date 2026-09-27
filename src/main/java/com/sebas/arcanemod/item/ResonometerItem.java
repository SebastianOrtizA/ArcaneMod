package com.sebas.arcanemod.item;

import com.sebas.arcanemod.core.facet.FacetCapabilities;
import com.sebas.arcanemod.core.facet.FacetRegistry;
import com.sebas.arcanemod.core.facet.FacetSignature;
import com.sebas.arcanemod.core.facet.IPlayerFacetKnowledge;
import com.sebas.arcanemod.client.ModParticles;
import com.sebas.arcanemod.network.FacetKnowledgeSync;
import com.sebas.arcanemod.network.ModNetwork;
import com.sebas.arcanemod.network.ScanResultPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;

import java.util.Optional;

/**
 * The player's Facet-scanning tool. Point at a block, item, or mob and hold right-click for
 * {@link #SCAN_DURATION_TICKS} — the target's {@link FacetSignature} is revealed and logged to
 * the player's {@code PlayerFacetKnowledge}. Re-scanning something already known reveals it
 * instantly instead of waiting out the hold again (checked every tick in {@link #onUseTick}, the
 * same trick {@code WandItem} uses to end a channel early).
 */
public class ResonometerItem extends Item {
    private static final int SCAN_DURATION_TICKS = 30; // 1.5s
    private static final double RANGE = 8.0;
    /** Vanilla's own entity picking tops out here too (see ProjectileUtil#computeMargin) — a
     * dropped item's actual hitbox is tiny (getPickRadius() is 0 for ItemEntity, unlike things
     * built to be clicked on), so without a generous fixed margin it's nearly impossible to aim
     * precisely enough to lock onto one. */
    private static final float ENTITY_PICK_MARGIN = 0.3F;
    /** Below this distance, a fluid "hit" is just the water/lava at the player's own eye — see findTarget. */
    private static final double MIN_FLUID_HIT_DISTANCE_SQR = 0.01;

    public ResonometerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (findTarget(level, player) == null) return InteractionResult.PASS;
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.SPYGLASS;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return SCAN_DURATION_TICKS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(level instanceof ServerLevel serverLevel) || !(user instanceof ServerPlayer player)) return;

        // Deliberately NOT cancelling the hold just because this one tick missed — a dropped
        // item's hitbox is small and it spins/bobs, so requiring pixel-perfect aim on every
        // single tick for the full 1.5s made scanning one nearly impossible in practice.
        // finishUsingItem does its own fresh check when the duration actually ends, so a hold
        // that drifts off target for a moment and comes back still completes normally.
        HitResult hit = findTarget(level, player);
        if (hit == null) return;

        Identifier targetId = resolveTargetId(hit, level).orElse(null);
        boolean alreadyKnown = targetId != null && player.getCapability(FacetCapabilities.PLAYER_FACET_KNOWLEDGE)
                .resolve().map(knowledge -> knowledge.hasScanned(targetId)).orElse(false);
        if (alreadyKnown) {
            completeScan(serverLevel, player, hit, targetId);
            player.stopUsingItem();
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel serverLevel && entity instanceof ServerPlayer player) {
            HitResult hit = findTarget(level, player);
            if (hit != null) {
                resolveTargetId(hit, level).ifPresent(targetId -> completeScan(serverLevel, player, hit, targetId));
            }
        }
        return stack;
    }

    private static void completeScan(ServerLevel level, ServerPlayer player, HitResult hit, Identifier targetId) {
        FacetSignature signature = FacetRegistry.get(targetId);

        player.getCapability(FacetCapabilities.PLAYER_FACET_KNOWLEDGE).resolve()
                .ifPresent((IPlayerFacetKnowledge knowledge) -> knowledge.recordScan(targetId, signature));
        FacetKnowledgeSync.sendTo(player);

        Component displayName = resolveDisplayName(hit, level);
        ModNetwork.sendToPlayer(player, new ScanResultPacket(targetId, displayName, signature));

        Vec3 pos = hit.getLocation();
        level.sendParticles(ModParticles.WYRD_SPARK.get(), pos.x, pos.y, pos.z, 12, 0.3, 0.3, 0.3, 0.02);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.2F);
    }

    /**
     * Block-first, then entities up to the block's distance (or full RANGE if nothing blocks) —
     * the same ordering vanilla's own crosshair picking uses, so a mob behind a wall can't be
     * scanned through it.
     */
    private static HitResult findTarget(Level level, Player player) {
        Vec3 from = player.getEyePosition();
        Vec3 viewVector = player.calculateViewVector(player.getXRot(), player.getYRot());
        Vec3 to = from.add(viewVector.scale(RANGE));

        // Two separate clips, not one: a plain solid clip (Fluid.NONE) is what lets you see past
        // water to the seafloor — necessary, or nothing underwater could ever be scanned — but it
        // also can't ever find water itself, since the ray just ignores fluid entirely. A
        // fluid-aware clip (Fluid.ANY) fixes that, but *only* helps if we prefer it just when its
        // hit is actually closer than the solid one — otherwise, while swimming, it would "hit"
        // the water at the player's own face (distance ~0) before ever reaching the seafloor, and
        // solid ground would become unscannable *while underwater* instead. MIN_FLUID_HIT_DISTANCE
        // filters out exactly that self-hit case without losing "the pond surface ahead of me."
        BlockHitResult solidHit = level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        BlockHitResult fluidHit = level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));

        BlockHitResult blockHit = solidHit;
        if (fluidHit.getType() != HitResult.Type.MISS
                && from.distanceToSqr(fluidHit.getLocation()) > MIN_FLUID_HIT_DISTANCE_SQR
                && (solidHit.getType() == HitResult.Type.MISS
                    || from.distanceToSqr(fluidHit.getLocation()) < from.distanceToSqr(solidHit.getLocation()))) {
            blockHit = fluidHit;
        }

        double maxDistanceSqr = blockHit.getType() == HitResult.Type.MISS ? RANGE * RANGE : from.distanceToSqr(blockHit.getLocation());
        Vec3 entitySearchTo = blockHit.getType() == HitResult.Type.MISS ? to : blockHit.getLocation();

        // isPickable() alone would miss dropped items — a plain ItemEntity doesn't override it
        // to true (that flag is for things you'd right-click, like boats or armor stands, not
        // passive drops), so it has to be allowed through explicitly.
        AABB searchBox = player.getBoundingBox().expandTowards(viewVector.scale(RANGE)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player, from, entitySearchTo, searchBox,
                entity -> !entity.isSpectator() && (entity.isPickable() || entity instanceof ItemEntity), maxDistanceSqr);

        if (entityHit != null) return entityHit;
        return blockHit.getType() != HitResult.Type.MISS ? blockHit : null;
    }

    /**
     * An {@link ItemEntity} resolves to the identifier of the item it's holding (that's what a
     * player actually wants to know when pointing at a dropped item), not "minecraft:item".
     */
    private static Optional<Identifier> resolveTargetId(HitResult hit, Level level) {
        if (hit instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            if (entity instanceof ItemEntity itemEntity) {
                return Optional.ofNullable(BuiltInRegistries.ITEM.getKey(itemEntity.getItem().getItem()));
            }
            return Optional.ofNullable(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
        } else if (hit instanceof BlockHitResult blockHit) {
            BlockState state = level.getBlockState(blockHit.getBlockPos());
            return Optional.ofNullable(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
        }
        return Optional.empty();
    }

    private static Component resolveDisplayName(HitResult hit, Level level) {
        if (hit instanceof EntityHitResult entityHit) {
            return entityHit.getEntity().getName();
        } else if (hit instanceof BlockHitResult blockHit) {
            return level.getBlockState(blockHit.getBlockPos()).getBlock().getName();
        }
        return Component.literal("?");
    }
}
