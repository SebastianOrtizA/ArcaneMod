package com.sebas.arcanemod.event;

import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.core.ModDataComponents;
import com.sebas.arcanemod.core.fray.FrayCapabilities;
import com.sebas.arcanemod.core.fray.IPlayerFrayData;
import com.sebas.arcanemod.core.fray.PlayerFrayData;
import com.sebas.arcanemod.core.wand.WandAssembly;
import com.sebas.arcanemod.core.weave.ChunkWeaveAccess;
import com.sebas.arcanemod.core.weave.IChunkWeaveData;
import com.sebas.arcanemod.item.ModItems;
import com.sebas.arcanemod.item.ModularWandItem;
import com.sebas.arcanemod.network.FraySync;
import com.sebas.arcanemod.network.ModNetwork;
import com.sebas.arcanemod.network.SyncChunkFrayPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

/**
 * Personal Fray: attach/persist/sync follows the exact same shape as {@code FacetEvents}'
 * {@code PlayerFacetKnowledge} handling (including the reviveCaps/never-invalidate pairing for
 * {@code PlayerEvent.Clone}) — see that class for the full explanation of why both halves of that
 * fix are needed.
 * <p>
 * Only one of the design doc's three personal-Fray sources exists yet: lingering in a Frayed
 * chunk. Forbidden spells and Duskbound equipment don't exist until later stages, so there's
 * nothing to wire them to — {@link #onPlayerTick} just checks the chunk the player is standing in.
 */
@Mod.EventBusSubscriber(modid = ArcaneMod.MODID)
public class FrayEvents {

    private static final Identifier FRAY_CAP_KEY = Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "fray");

    /** Matches WeaveEvents' regen cadence — no reason for this to check more often than density does. */
    private static final int LINGER_CHECK_INTERVAL_TICKS = 200;
    private static final int LINGER_FRAY_THRESHOLD = 50;

    /**
     * Separate, much shorter cadence than the linger check — this is purely a client-rendering
     * sync (Checkpoint 4's fog tint), so it needs to notice a player crossing into/out of a
     * Frayed chunk quickly. Resyncing unconditionally every second is cheap (one varint) and
     * avoids needing a per-player "did this change" cache just to save the occasional packet.
     */
    private static final int CHUNK_FRAY_SYNC_INTERVAL_TICKS = 20;

    /** "Decays by 1 every 5 minutes" per the design doc. */
    private static final int DECAY_INTERVAL_TICKS = 6000;

    /** Voidglass Core: +0.1 Fray/min = +1 every 10 minutes. */
    private static final int VOIDGLASS_FRAY_INTERVAL_TICKS = 12000;

    private static final int MAX_PERSONAL_FRAY = 100;

    /**
     * How far out (in chunks) the rendered atmosphere bleeds from a Frayed chunk before fading to
     * nothing — a source chunk's own Fray value is felt at full strength, then falls off linearly
     * with (Euclidean) chunk distance until it hits zero exactly at this radius, giving a smooth
     * walk-away taper instead of a hard edge. Purely a rendering concern: personal Fray still only
     * accumulates from the chunk you're actually standing in (see {@link #onPlayerTick}); this only
     * affects what {@link #effectiveRegionalFrayForRendering} reports to the client.
     */
    private static final int RENDER_BLEED_RADIUS_CHUNKS = 3;

    @SubscribeEvent
    public static void onAttachPlayer(AttachCapabilitiesEvent.Entities event) {
        if (!(event.getObject() instanceof Player)) return;

        event.addCapability(FRAY_CAP_KEY, new PlayerFrayData());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        original.reviveCaps();
        try {
            original.getCapability(FrayCapabilities.PLAYER_FRAY).resolve().ifPresent(oldData ->
                    event.getEntity().getCapability(FrayCapabilities.PLAYER_FRAY).resolve().ifPresent(newData -> {
                        if (newData instanceof PlayerFrayData impl) {
                            impl.copyFrom(oldData);
                        }
                    }));
        } finally {
            original.invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            FraySync.sendTo(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent.Post event) {
        if (event.side() != LogicalSide.SERVER) return;
        if (!(event.player() instanceof ServerPlayer player)) return;

        long time = player.level().getGameTime();
        boolean lingerDue = time % LINGER_CHECK_INTERVAL_TICKS == 0;
        boolean decayDue = time % DECAY_INTERVAL_TICKS == 0;
        boolean chunkSyncDue = time % CHUNK_FRAY_SYNC_INTERVAL_TICKS == 0;
        boolean voidglassDue = time % VOIDGLASS_FRAY_INTERVAL_TICKS == 0;

        if (chunkSyncDue) {
            int effectiveFray = effectiveRegionalFrayForRendering(player);
            ModNetwork.sendToPlayer(player, new SyncChunkFrayPacket(effectiveFray));
        }

        if (!lingerDue && !decayDue && !voidglassDue) return;

        player.getCapability(FrayCapabilities.PLAYER_FRAY).resolve().ifPresent(data -> {
            int fray = data.getPersonalFray();
            int updated = fray;

            if (lingerDue && exactChunkRegionalFray(player) > LINGER_FRAY_THRESHOLD && updated < MAX_PERSONAL_FRAY) {
                updated += 1;
            }
            if (voidglassDue && isHoldingVoidglassCoreWand(player) && updated < MAX_PERSONAL_FRAY) {
                updated += 1;
            }
            if (decayDue && updated > 0) {
                updated -= 1;
            }

            if (updated != fray) {
                data.setPersonalFray(updated);
                FraySync.sendTo(player);
            }
        });
    }

    private static boolean isHoldingVoidglassCoreWand(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof ModularWandItem)) continue;
            WandAssembly assembly = stack.get(ModDataComponents.WAND_ASSEMBLY.get());
            if (assembly == null) continue;
            Identifier coreId = assembly.core();
            if (ForgeRegistries.ITEMS.getKey(ModItems.VOIDGLASS_CORE.get()).equals(coreId)) return true;
        }
        return false;
    }

    private static int exactChunkRegionalFray(ServerPlayer player) {
        return ChunkWeaveAccess.get(player.level().getChunkAt(player.blockPosition()))
                .map(IChunkWeaveData::getRegionalFray)
                .orElse(0);
    }

    /**
     * The highest <em>distance-attenuated</em> regional Fray found within
     * {@link #RENDER_BLEED_RADIUS_CHUNKS} chunks — each neighbor's own Fray value is scaled down
     * linearly by how many chunks away it is (full strength at distance 0, zero at the radius),
     * and the max across the neighborhood wins, so a player standing between two Frayed chunks
     * feels whichever bleeds in strongest rather than an average. Only scans chunks already loaded
     * ({@code getChunkNow}, same as the Wellspring's neighbor scan), so this never forces chunk
     * loads just to render fog.
     */
    private static int effectiveRegionalFrayForRendering(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return 0;

        ChunkPos center = player.chunkPosition();
        float highest = 0F;
        for (int dx = -RENDER_BLEED_RADIUS_CHUNKS; dx <= RENDER_BLEED_RADIUS_CHUNKS; dx++) {
            for (int dz = -RENDER_BLEED_RADIUS_CHUNKS; dz <= RENDER_BLEED_RADIUS_CHUNKS; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > RENDER_BLEED_RADIUS_CHUNKS) continue;

                LevelChunk chunk = serverLevel.getChunkSource().getChunkNow(center.x() + dx, center.z() + dz);
                if (chunk == null) continue;

                int fray = ChunkWeaveAccess.get(chunk).map(IChunkWeaveData::getRegionalFray).orElse(0);
                if (fray <= 0) continue;

                float falloff = 1F - (float) (distance / RENDER_BLEED_RADIUS_CHUNKS);
                highest = Math.max(highest, fray * falloff);
            }
        }
        return Math.round(highest);
    }
}
