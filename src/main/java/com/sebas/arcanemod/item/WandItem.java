package com.sebas.arcanemod.item;

import com.sebas.arcanemod.blockentity.WeaveWellspringBlockEntity;
import com.sebas.arcanemod.client.ModParticles;
import com.sebas.arcanemod.core.wyrd.WandWyrdStorage;
import com.sebas.arcanemod.entity.SparkBoltEntity;
import com.sebas.arcanemod.event.ResearchEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * The Cobblestone Wand. Section 1's "crude beginning" per the design doc: no core/cap/binding
 * system yet, minimal Wyrd capacity, and its main jobs are interacting with magical objects
 * (the lectern ritual, Resonometer scanning later) and drawing Wyrd from a Wellspring.
 * <p>
 * Channeling is implemented as a continuous "use" action — the exact same mechanism vanilla
 * uses for drawing a bow or eating food — rather than anything packet-based. Right-clicking a
 * Wellspring: the block itself has no special use behavior, so that interaction falls through
 * to this item's generic {@link #use}, which starts a use-duration hold; {@link #onUseTick}
 * then fires every tick for as long as the player holds right-click, each time re-checking
 * what they're looking at. Verified this fallthrough directly in Minecraft's own
 * {@code startUseItem()} — a block click that neither the block nor the item's useOn consumes
 * automatically proceeds to the item's generic use.
 */
public class WandItem extends Item {
    private static final int CHANNEL_DURATION_TICKS = 72000;
    private static final int SPARK_WYRD_COST = 5;

    public WandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (findDrainableWellspring(level, player, stack) != null) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            WandWyrdStorage storage = new WandWyrdStorage(stack, WandWyrdStorage.COBBLESTONE_WAND_MAX_WYRD);
            if (storage.getWyrd() >= SPARK_WYRD_COST) {
                storage.extractWyrd(SPARK_WYRD_COST, false);
                Vec3 look = player.getLookAngle();
                SparkBoltEntity spark = new SparkBoltEntity(serverLevel, player, look);
                spark.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                serverLevel.addFreshEntity(spark);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.5F, 1.8F);
                stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND
                        ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return CHANNEL_DURATION_TICKS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(level instanceof ServerLevel serverLevel) || !(user instanceof Player player)) return;

        // Re-checked every tick: stop the moment there's genuinely nothing left to draw —
        // wellspring ran dry, or the wand's already full — rather than idling out the rest of
        // the hold with no effect. isLookingAtWellspring/hit-testing lives inside this check too,
        // so looking away also ends the channel.
        WeaveWellspringBlockEntity wellspring = findDrainableWellspring(level, player, stack);
        if (wellspring == null) {
            player.stopUsingItem();
            return;
        }

        WandWyrdStorage wandStorage = new WandWyrdStorage(stack, WandWyrdStorage.COBBLESTONE_WAND_MAX_WYRD);
        int transferred = wellspring.channelTick(serverLevel, wandStorage);
        var pos = wellspring.getBlockPos();

        if (transferred > 0 && player instanceof ServerPlayer serverPlayer) {
            ResearchEvents.tryComplete(serverPlayer, ResearchEvents.id("the_first_wellspring"));
        }

        // Particles run every tick for as long as there's still something to drain, rather than
        // only the exact ticks a transfer happened to land — reads as a sustained channeling
        // effect instead of a flicker, and now stops cleanly with the channel itself above.
        serverLevel.sendParticles(ModParticles.WYRD_SPARK.get(),
                pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                2, 0.2, 0.3, 0.2, 0.01);

        // Sound stays tied to genuine transfer, throttled so it doesn't fire every single tick.
        if (transferred > 0 && remainingUseTicks % 5 == 0) {
            serverLevel.playSound(null, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.4F, 1.4F);
        }
    }

    /**
     * @return the Wellspring the player is aimed at, if it actually has Wyrd to give AND the
     * wand has room to receive it — or null if either the aim, the source, or the sink can't
     * support a channel right now.
     */
    private WeaveWellspringBlockEntity findDrainableWellspring(Level level, Player player, ItemStack stack) {
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hit.getType() != HitResult.Type.BLOCK) return null;

        BlockEntity blockEntity = level.getBlockEntity(hit.getBlockPos());
        if (!(blockEntity instanceof WeaveWellspringBlockEntity wellspring)) return null;
        if (wellspring.getStoredWyrd() <= 0) return null;

        WandWyrdStorage wandStorage = new WandWyrdStorage(stack, WandWyrdStorage.COBBLESTONE_WAND_MAX_WYRD);
        if (wandStorage.getWyrd() >= wandStorage.getMaxWyrd()) return null;

        return wellspring;
    }

    // appendHoverText is marked @Deprecated in this version (Mojang points toward a
    // data-component-driven tooltip system instead) but is still the functional, called API —
    // there's no non-deprecated replacement usable here for dynamically-computed text.
    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        WandWyrdStorage storage = new WandWyrdStorage(stack, WandWyrdStorage.COBBLESTONE_WAND_MAX_WYRD);
        tooltip.accept(Component.translatable("tooltip.arcanemod.wyrd_charge", storage.getWyrd(), storage.getMaxWyrd()));
    }
}
