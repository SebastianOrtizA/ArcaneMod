package com.sebas.arcanemod.event;

import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.core.research.PlayerResearchData;
import com.sebas.arcanemod.core.research.ResearchCapabilities;
import com.sebas.arcanemod.core.research.ResearchDataLoader;
import com.sebas.arcanemod.core.research.ResearchNode;
import com.sebas.arcanemod.core.research.ResearchTree;
import com.sebas.arcanemod.item.ModItems;
import com.sebas.arcanemod.network.ResearchSync;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Section 1's research nodes are all "guided" (design doc: "Warp-free early game") — no puzzle,
 * completed the instant their trigger condition is met. This class is both the capability
 * plumbing (attach/persist/sync, identical shape to {@code FacetEvents}/{@code FrayEvents}) and
 * the trigger wiring for the handful of nodes that don't depend on the Loom of Understanding
 * existing yet: crafting the Resonometer/Wyrd Dust/Cobblestone Wand, and reading the Codex.
 * <p>
 * "First Threads" (craft the Loom) has no trigger here — it stays locked until the Loom block
 * exists in a later checkpoint. That's an intentional gate, not a bug.
 */
@Mod.EventBusSubscriber(modid = ArcaneMod.MODID)
public class ResearchEvents {

    private static final Identifier RESEARCH_CAP_KEY = Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "research");

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ResearchDataLoader());
    }

    /** See {@code FacetEvents#onAttachPlayer} for why this never wires an invalidate listener. */
    @SubscribeEvent
    public static void onAttachPlayer(AttachCapabilitiesEvent.Entities event) {
        if (!(event.getObject() instanceof Player)) return;

        event.addCapability(RESEARCH_CAP_KEY, new PlayerResearchData());
    }

    /** See {@code FacetEvents#onPlayerClone} for the full reviveCaps/invalidateCaps explanation. */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        original.reviveCaps();
        try {
            original.getCapability(ResearchCapabilities.PLAYER_RESEARCH_DATA).resolve().ifPresent(oldData ->
                    event.getEntity().getCapability(ResearchCapabilities.PLAYER_RESEARCH_DATA).resolve().ifPresent(newData -> {
                        if (newData instanceof PlayerResearchData impl) {
                            impl.copyFrom(oldData);
                        }
                    }));
        } finally {
            original.invalidateCaps();
        }
    }

    /**
     * "Awakening — reading the Codex for the first time; no cost, always unlocked" is granted
     * here rather than tied to an actual Codex-open packet: it has no real trigger condition, so
     * the simplest honest implementation is "every player always has it." Granting it at login
     * (after NBT load) rather than at capability attach avoids the freshly-attached instance's
     * grant being wiped when a returning player's saved data loads afterward.
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            tryComplete(serverPlayer, id("awakening"));
            ResearchSync.sendTreeTo(serverPlayer);
            ResearchSync.sendTo(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Item item = event.getCrafting().getItem();
        if (item == ModItems.RESONOMETER.get()) {
            tryComplete(player, id("sensing_the_weave"));
        } else if (item == ModItems.WYRD_DUST.get()) {
            tryComplete(player, id("wyrdstone_refining"));
        } else if (item == ModItems.WAND.get()) {
            tryComplete(player, id("a_crude_focus"));
        } else if (item == ModItems.LOOM_OF_UNDERSTANDING.get()) {
            tryComplete(player, id("first_threads"));
        } else if (item == ModItems.WANDWRIGHTS_BENCH.get()) {
            tryComplete(player, id("wandwrights_bench"));
        }
    }

    @SubscribeEvent
    public static void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (event.getSmelting().getItem() == ModItems.WYRD_DUST.get()) {
            tryComplete(player, id("wyrdstone_refining"));
        }
    }

    /**
     * Shared by every trigger above and by {@code WandItem} (for "The First Wellspring," which
     * fires from the middle of a channel tick rather than a Forge event). No-ops quietly if
     * {@code id} isn't a real research node — lets callers reference a node id without needing
     * to know whether its JSON has loaded yet.
     */
    public static void tryComplete(ServerPlayer player, Identifier id) {
        if (!ResearchTree.has(id)) return;

        player.getCapability(ResearchCapabilities.PLAYER_RESEARCH_DATA).resolve().ifPresent(data -> {
            if (data.complete(id)) {
                ResearchSync.sendTo(player);
                ResearchNode node = ResearchTree.get(id);
                player.sendSystemMessage(Component.literal("Research unlocked: " + node.displayName()));
            }
        });
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ArcaneMod.MODID, path);
    }
}
