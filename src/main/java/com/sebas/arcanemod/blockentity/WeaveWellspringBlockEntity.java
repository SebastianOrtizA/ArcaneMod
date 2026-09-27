package com.sebas.arcanemod.blockentity;

import com.sebas.arcanemod.core.weave.ChunkWeaveAccess;
import com.sebas.arcanemod.core.wyrd.IWyrdStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The Weave Wellspring's block entity. Passively accumulates Wyrd from the chunk's current
 * Weave density, and lets a wand's channel action ({@code WandItem#onUseTick}) draw it out.
 * <p>
 * Notable design choices, each straight from the Stage 0 plan:
 * <ul>
 *   <li>Ancient (worldgen) Wellsprings produce at full rate; player-built ones (in a later
 *       stage's Rite of Wellspring Raising) at 0.6x — {@code isAncient} defaults true and is
 *       flipped to false only in {@code WeaveWellspringBlock#setPlacedBy}, which fires when a
 *       player places one from their inventory but never during worldgen feature placement.</li>
 *   <li>Below {@link #MIN_DENSITY_TO_PRODUCE} currentDensity, production stops entirely — the
 *       "overdraw punishes itself" feedback loop from the design doc.</li>
 *   <li>Proximity interference is rescanned periodically rather than every tick, since it
 *       requires touching 8 neighboring chunks.</li>
 *   <li>Max capacity isn't a flat constant — each Wellspring rolls its own, once, the first
 *       time it ticks (lazily, same reasoning as {@code ChunkWeaveAccess}'s base density: the
 *       chunk's biome-derived density needs to be settled first). The roll's ceiling scales
 *       with that chunk's base density, so a Wellspring in a rich forest can end up noticeably
 *       stronger than one in a desert — but it's still a roll, not a guarantee.</li>
 * </ul>
 */
public class WeaveWellspringBlockEntity extends BlockEntity {
    /** Every Wellspring gets at least this much capacity, even at 0 base density. */
    private static final int MIN_CAPACITY = 200;
    /** Extra capacity range added on top of MIN_CAPACITY, scaled by base density (0..1). */
    private static final int CAPACITY_DENSITY_BONUS = 600;

    private static final float BASE_RATE = 2.0f; // Wyrd/tick at density 1.0, ancient, no interference
    private static final float MIN_DENSITY_TO_PRODUCE = 0.1f;
    private static final float INTERFERENCE_FACTOR = 0.85f;
    private static final int NEIGHBOR_RESCAN_INTERVAL_TICKS = 100; // 5 seconds

    // The cobblestone wand's max hold is 60 ticks (3s), so at 1/tick a single full channel
    // moves at most 60 Wyrd — filling a 100-cap wand from empty takes two holds, not one. Fits
    // the "crude, low capacity" cobblestone wand better than the earlier faster rates.
    private static final int WYRD_PER_CHANNEL_TICK = 1;
    private static final float DENSITY_DRAIN_PER_WYRD = 0.01f;

    /** Below this, drawing Wyrd starts Fraying the chunk — the design doc's "overdraw" threshold. */
    private static final float FRAY_ONSET_DENSITY = 0.2f;
    private static final int MAX_REGIONAL_FRAY = 100;

    private int storedWyrd = 0;
    /** -1 until rolled — see the class javadoc note on lazy, density-scaled capacity. */
    private int maxWyrd = -1;
    private boolean isAncient = true;
    private float accumulator = 0f;

    private int cachedNeighborWellsprings = 1; // includes self; recomputed periodically
    private long lastNeighborScanGameTime = -1;

    public WeaveWellspringBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WEAVE_WELLSPRING.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WeaveWellspringBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        be.rescanNeighborsIfDue(serverLevel, pos);

        ChunkWeaveAccess.get(serverLevel.getChunkAt(pos)).ifPresent(data -> {
            if (be.maxWyrd < 0) {
                be.maxWyrd = rollMaxWyrd(data.getBaseDensity(), serverLevel.getRandom());
                be.setChanged();
            }

            if (be.storedWyrd >= be.maxWyrd) return;

            float density = data.getCurrentDensity();
            if (density < MIN_DENSITY_TO_PRODUCE) return;

            float interference = (float) Math.pow(INTERFERENCE_FACTOR, Math.max(0, be.cachedNeighborWellsprings - 1));
            float wyrdPerTick = density * (be.isAncient ? 1.0f : 0.6f) * BASE_RATE * interference;

            be.accumulator += wyrdPerTick;
            if (be.accumulator >= 1.0f) {
                int whole = (int) be.accumulator;
                be.accumulator -= whole;
                int before = be.storedWyrd;
                be.storedWyrd = Math.min(be.maxWyrd, be.storedWyrd + whole);
                if (be.storedWyrd != before) be.setChanged();
            }
        });
    }

    /** MIN_CAPACITY..(MIN_CAPACITY + baseDensity * CAPACITY_DENSITY_BONUS), inclusive. */
    private static int rollMaxWyrd(float baseDensity, RandomSource random) {
        int ceiling = MIN_CAPACITY + Math.round(baseDensity * CAPACITY_DENSITY_BONUS);
        return MIN_CAPACITY + random.nextInt(ceiling - MIN_CAPACITY + 1);
    }

    private void rescanNeighborsIfDue(ServerLevel level, BlockPos pos) {
        long now = level.getGameTime();
        if (lastNeighborScanGameTime >= 0 && now - lastNeighborScanGameTime < NEIGHBOR_RESCAN_INTERVAL_TICKS) return;
        lastNeighborScanGameTime = now;

        ChunkPos center = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        int otherWellsprings = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(center.x() + dx, center.z() + dz);
                if (chunk == null) continue;
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof WeaveWellspringBlockEntity && blockEntity != this) {
                        otherWellsprings++;
                    }
                }
            }
        }
        cachedNeighborWellsprings = otherWellsprings + 1;
    }

    /**
     * Called from WandItem's channel hold, once per tick the player is aimed at this
     * Wellspring. Moves up to {@link #WYRD_PER_CHANNEL_TICK} Wyrd into the wand's storage and
     * drains the chunk's currentDensity proportionally.
     *
     * @return the amount actually transferred this tick (0 if nothing available/room)
     */
    public int channelTick(ServerLevel level, IWyrdStorage wandStorage) {
        if (storedWyrd <= 0) return 0;
        int wandRoom = wandStorage.getMaxWyrd() - wandStorage.getWyrd();
        if (wandRoom <= 0) return 0;

        int amount = Math.min(WYRD_PER_CHANNEL_TICK, Math.min(storedWyrd, wandRoom));
        if (amount <= 0) return 0;

        storedWyrd -= amount;
        wandStorage.receiveWyrd(amount, false);
        setChanged();

        ChunkWeaveAccess.get(level.getChunkAt(getBlockPos())).ifPresent(data -> {
            float newDensity = Math.max(0f, data.getCurrentDensity() - amount * DENSITY_DRAIN_PER_WYRD);
            data.setCurrentDensity(newDensity);

            // Overdraw: drawing Wyrd while the chunk is already running low Frays the land, per
            // the design doc — this is the "greed is punished by the system that was already
            // there" loop made literal, one step past just refusing to produce below MIN_DENSITY.
            if (newDensity < FRAY_ONSET_DENSITY) {
                int newFray = Math.min(MAX_REGIONAL_FRAY, data.getRegionalFray() + amount);
                data.setRegionalFray(newFray);
            }
        });

        return amount;
    }

    public int getStoredWyrd() {
        return storedWyrd;
    }

    /** -1 if this Wellspring hasn't ticked yet and rolled its capacity — see the class javadoc. */
    public int getMaxWyrd() {
        return maxWyrd;
    }

    public boolean isAncient() {
        return isAncient;
    }

    public void setAncient(boolean ancient) {
        this.isAncient = ancient;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("storedWyrd", storedWyrd);
        output.putInt("maxWyrd", maxWyrd);
        output.putBoolean("isAncient", isAncient);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedWyrd = input.getIntOr("storedWyrd", 0);
        maxWyrd = input.getIntOr("maxWyrd", -1);
        isAncient = input.getBooleanOr("isAncient", true);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
