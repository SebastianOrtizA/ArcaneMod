package com.sebas.arcanemod.event;

import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.core.weave.ChunkWeaveAccess;
import com.sebas.arcanemod.core.weave.ChunkWeaveData;
import com.sebas.arcanemod.core.weave.IChunkWeaveData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ArcaneMod.MODID)
public class WeaveEvents {

    private static final Identifier WEAVE_CAP_KEY = Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "weave");

    /** 200 ticks = 10 seconds, per the design doc. */
    private static final int REGEN_INTERVAL_TICKS = 200;
    /** Applied once per regen interval, not per tick — see the class-level note below. */
    private static final float RECOVERY_FRACTION = 0.01f;

    @SubscribeEvent
    public static void onAttachChunk(AttachCapabilitiesEvent.LevelChunks event) {
        ChunkWeaveData data = new ChunkWeaveData();
        event.addCapability(WEAVE_CAP_KEY, data);
        event.addListener(data::invalidate);
    }

    /**
     * Recovers every loaded chunk's currentDensity a little toward its baseDensity, every
     * {@link #REGEN_INTERVAL_TICKS}. Uses ChunkMap#forEachBlockTickingChunk — the same
     * mechanism vanilla uses to walk currently-ticking chunks for random block ticks — rather
     * than any manual chunk bookkeeping.
     */
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent.Post event) {
        Level level = event.level();
        if (level.isClientSide()) return;
        if (level.getGameTime() % REGEN_INTERVAL_TICKS != 0) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        serverLevel.getChunkSource().chunkMap.forEachBlockTickingChunk(chunk ->
                ChunkWeaveAccess.get(chunk).ifPresent(WeaveEvents::regenerate));
    }

    /**
     * Regional Fray gates Wyrd regen per the design doc's tiers: 0–20 no effect, 21–50 halved,
     * 51+ halted entirely (the 81–100 tier's extra severity is all about Frayed mob spawn rate,
     * not regen — regen is already fully stopped by 51).
     */
    private static void regenerate(IChunkWeaveData data) {
        float base = data.getBaseDensity();
        float current = data.getCurrentDensity();
        int fray = data.getRegionalFray();

        float recovery = RECOVERY_FRACTION;
        if (fray > 50) {
            recovery = 0f;
        } else if (fray > 20) {
            recovery = recovery * 0.5f;
        }

        data.setCurrentDensity(current + (base - current) * recovery);
    }
}
