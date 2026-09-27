package com.sebas.arcanemod.event;

import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.core.facet.FacetCapabilities;
import com.sebas.arcanemod.core.facet.FacetDataLoader;
import com.sebas.arcanemod.core.facet.PlayerFacetKnowledge;
import com.sebas.arcanemod.network.FacetKnowledgeSync;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ArcaneMod.MODID)
public class FacetEvents {

    private static final Identifier FACET_KNOWLEDGE_CAP_KEY =
            Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "facet_knowledge");

    /**
     * Also where the vanilla bulk Facet defaults get (re)computed — see {@code FacetDataLoader}'s
     * class javadoc for why that lives inside its {@code apply()} rather than a dedicated event.
     */
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new FacetDataLoader());
    }

    /**
     * AttachCapabilitiesEvent.Entities fires for every entity, so filter down to players.
     * <p>
     * Deliberately NOT wiring {@code event.addListener(data::invalidate)} here, unlike the chunk
     * Weave capability. There are actually TWO separate "is this dead" flags involved in a
     * capability read, and both have to stay passable for {@link #onPlayerClone} to work:
     * <ol>
     *   <li>{@code CapabilityProvider.valid} — a flag on the ENTITY itself, flipped false by
     *   {@code Entity#remove()}. {@code reviveCaps()}/{@code invalidateCaps()} in
     *   {@link #onPlayerClone} handle this one; it's reversible.</li>
     *   <li>Our own {@link PlayerFacetKnowledge}'s {@code LazyOptional} — if we invalidate it
     *   here, {@code LazyOptional#invalidate()} sets its {@code isValid} flag permanently, with
     *   no un-invalidate method anywhere. Reviving the entity's capability provider does nothing
     *   for this — the dispatcher would still delegate to our own dead LazyOptional and get
     *   {@code empty()} regardless.</li>
     * </ol>
     * So the old player's {@code LazyOptional} has to simply never be invalidated in the first
     * place. A dead player object isn't reused the way an unloaded chunk might be, so leaving it
     * resolvable forever is harmless — it just becomes garbage once nothing references the old
     * entity anymore.
     */
    @SubscribeEvent
    public static void onAttachPlayer(AttachCapabilitiesEvent.Entities event) {
        if (!(event.getObject() instanceof Player)) return;

        event.addCapability(FACET_KNOWLEDGE_CAP_KEY, new PlayerFacetKnowledge());
    }

    /**
     * Player capabilities do NOT survive respawn/dimension-return on their own — death and the
     * End exit both replace the {@code Player} object, and only NBT-backed entity data (not
     * capabilities) is carried over automatically. This is the standard Forge pattern for making
     * capability state respawn-proof.
     * <p>
     * By the time this fires, the OLD player has already been removed from the world —
     * {@code PlayerList#respawn} calls {@code removePlayerImmediately} before
     * {@code restoreFrom} (which is what triggers this event) — and {@code Entity#remove()}
     * unconditionally flips {@code CapabilityProvider.valid} to false, which
     * {@code getCapability} checks before anything else. {@code reviveCaps()} is Forge's
     * documented escape hatch for exactly this ("modders can use this if they need to copy caps
     * from one removed provider to a new one"), so we flip it back on just long enough to read
     * the old data, then invalidate again afterward. See {@link #onAttachPlayer} for the other
     * half of this fix (why we must NOT invalidate our own LazyOptional on removal).
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        original.reviveCaps();
        try {
            original.getCapability(FacetCapabilities.PLAYER_FACET_KNOWLEDGE).resolve().ifPresent(oldData ->
                    event.getEntity().getCapability(FacetCapabilities.PLAYER_FACET_KNOWLEDGE).resolve().ifPresent(newData -> {
                        if (newData instanceof PlayerFacetKnowledge impl) {
                            impl.copyFrom(oldData);
                        }
                    }));
        } finally {
            original.invalidateCaps();
        }
    }

    /** The client has no Facet knowledge of its own until the server tells it what it is. */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            FacetKnowledgeSync.sendTo(serverPlayer);
        }
    }
}
