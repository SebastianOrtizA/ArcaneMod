package com.sebas.arcanemod.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.block.ModBlocks;
import com.sebas.arcanemod.blockentity.WeaveWellspringBlockEntity;
import com.sebas.arcanemod.core.facet.FacetCapabilities;
import com.sebas.arcanemod.core.facet.FacetRegistry;
import com.sebas.arcanemod.core.facet.FacetSignature;
import com.sebas.arcanemod.core.facet.IPlayerFacetKnowledge;
import com.sebas.arcanemod.core.fray.FrayCapabilities;
import com.sebas.arcanemod.core.fray.IPlayerFrayData;
import com.sebas.arcanemod.core.research.IPlayerResearchData;
import com.sebas.arcanemod.core.research.ResearchCapabilities;
import com.sebas.arcanemod.core.research.ResearchNode;
import com.sebas.arcanemod.core.research.ResearchTree;
import com.sebas.arcanemod.core.weave.ChunkWeaveAccess;
import com.sebas.arcanemod.core.weave.IChunkWeaveData;
import com.sebas.arcanemod.entity.ModEntityTypes;
import com.sebas.arcanemod.event.ResearchEvents;
import com.sebas.arcanemod.network.FacetKnowledgeSync;
import com.sebas.arcanemod.network.FraySync;
import com.sebas.arcanemod.network.ResearchSync;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

/**
 * Debug commands for inspecting otherwise-invisible mod state. {@code /arcane weave} is the
 * only way to see chunk Weave density until later checkpoints add visual overlays — keep it
 * around, it stays useful once Fray rendering exists too.
 */
@Mod.EventBusSubscriber(modid = ArcaneMod.MODID)
public class ArcaneCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("arcane")
                        .then(Commands.literal("weave").executes(ArcaneCommands::runWeaveCommand))
                        .then(Commands.literal("wellspring").executes(ArcaneCommands::runWellspringCommand))
                        .then(Commands.literal("loom").executes(ArcaneCommands::runLoomCommand))
                        .then(Commands.literal("facets")
                                .then(Commands.argument("id", IdentifierArgument.id())
                                        .executes(ArcaneCommands::runFacetsCommand)))
                        .then(Commands.literal("scan")
                                .then(Commands.argument("id", IdentifierArgument.id())
                                        .executes(ArcaneCommands::runScanCommand)))
                        .then(Commands.literal("knowledge").executes(ArcaneCommands::runKnowledgeCommand))
                        .then(Commands.literal("fray")
                                .executes(ArcaneCommands::runFrayCommand)
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes(ArcaneCommands::runFrayAddCommand))))
                        .then(Commands.literal("frayed")
                                .then(Commands.literal("zombie").executes(ctx -> runFrayedCommand(ctx, ModEntityTypes.FRAYED_ZOMBIE.get())))
                                .then(Commands.literal("skeleton").executes(ctx -> runFrayedCommand(ctx, ModEntityTypes.FRAYED_SKELETON.get())))
                                .then(Commands.literal("spider").executes(ctx -> runFrayedCommand(ctx, ModEntityTypes.FRAYED_SPIDER.get()))))
                        .then(Commands.literal("research")
                                .executes(ArcaneCommands::runResearchListCommand)
                                .then(Commands.literal("complete")
                                        .then(Commands.argument("id", IdentifierArgument.id())
                                                .executes(ArcaneCommands::runResearchCompleteCommand)))
                                .then(Commands.literal("forget")
                                        .then(Commands.argument("id", IdentifierArgument.id())
                                                .executes(ArcaneCommands::runResearchForgetCommand))))
        );
    }

    /**
     * Debug-only convenience: spawns a Frayed mob at the command sender's feet, so testing stats/
     * textures/loot doesn't depend on getting a chunk's regional Fray above the natural-spawn
     * threshold first.
     */
    private static int runFrayedCommand(CommandContext<CommandSourceStack> ctx, EntityType<?> type) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        Entity entity = type.create(level, EntitySpawnReason.COMMAND);
        if (entity == null) {
            source.sendFailure(Component.literal("Failed to create " + type + " — this is a bug."));
            return 0;
        }

        entity.snapTo(pos.x, pos.y, pos.z, source.getRotation().y, source.getRotation().x);
        // create() alone skips Mob#finalizeSpawn — without it a debug-spawned skeleton has no
        // bow, since that's handed out there, not in the constructor.
        if (entity instanceof Mob mob) {
            BlockPos blockPos = BlockPos.containing(pos);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(blockPos), EntitySpawnReason.COMMAND, null);
        }
        level.addFreshEntity(entity);

        source.sendSuccess(() -> Component.literal("Spawned " + entity.getName().getString()), false);
        return 1;
    }

    /** Prints the command sender's current personal Fray — there's no HUD for it until Checkpoint 4. */
    private static int runFrayCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();

        Optional<IPlayerFrayData> frayOpt = player.getCapability(FrayCapabilities.PLAYER_FRAY).resolve();
        if (frayOpt.isEmpty()) {
            source.sendFailure(Component.literal("Player has no Fray capability attached — this is a bug."));
            return 0;
        }

        int fray = frayOpt.get().getPersonalFray();
        source.sendSuccess(() -> Component.literal("Personal Fray: " + fray), false);
        return 1;
    }

    /**
     * Debug-only: nudges personal Fray by an arbitrary amount (positive or negative), clamped to
     * 0..100. Stands in for the "casting Forbidden spells"/"wearing Duskbound gear" sources that
     * don't exist yet, so the decay and threshold-gated effects can still be tested now.
     */
    private static int runFrayAddCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        Optional<IPlayerFrayData> frayOpt = player.getCapability(FrayCapabilities.PLAYER_FRAY).resolve();
        if (frayOpt.isEmpty()) {
            source.sendFailure(Component.literal("Player has no Fray capability attached — this is a bug."));
            return 0;
        }

        IPlayerFrayData data = frayOpt.get();
        int updated = Math.max(0, Math.min(100, data.getPersonalFray() + amount));
        data.setPersonalFray(updated);
        FraySync.sendTo(player);

        source.sendSuccess(() -> Component.literal("Personal Fray is now " + updated), false);
        return 1;
    }

    /**
     * Debug stand-in for the Resonometer (Checkpoint 4): records a scan against the command
     * sender's PlayerFacetKnowledge and pushes the updated knowledge to their client, so the
     * whole scan -> capability -> sync loop can be exercised before that item exists.
     */
    private static int runScanCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Identifier id = IdentifierArgument.getId(ctx, "id");
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();

        if (!FacetRegistry.has(id)) {
            source.sendFailure(Component.literal("No magical resonance detected from " + id));
            return 0;
        }

        FacetSignature signature = FacetRegistry.get(id);
        Optional<IPlayerFacetKnowledge> knowledgeOpt =
                player.getCapability(FacetCapabilities.PLAYER_FACET_KNOWLEDGE).resolve();
        if (knowledgeOpt.isEmpty()) {
            source.sendFailure(Component.literal("Player has no Facet knowledge capability attached — this is a bug."));
            return 0;
        }

        boolean isNew = knowledgeOpt.get().recordScan(id, signature);
        FacetKnowledgeSync.sendTo(player);

        source.sendSuccess(() -> Component.literal((isNew ? "Discovered " : "Already known: ") + id + " -> " + signature), false);
        return 1;
    }

    private static int runKnowledgeCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();

        Optional<IPlayerFacetKnowledge> knowledgeOpt =
                player.getCapability(FacetCapabilities.PLAYER_FACET_KNOWLEDGE).resolve();
        if (knowledgeOpt.isEmpty()) {
            source.sendFailure(Component.literal("Player has no Facet knowledge capability attached — this is a bug."));
            return 0;
        }

        IPlayerFacetKnowledge knowledge = knowledgeOpt.get();
        source.sendSuccess(() -> Component.literal(String.format(
                "Scanned %d entries — known Facets: %s — known Compounds: %s",
                knowledge.getScannedEntries().size(), knowledge.getKnownFacets(), knowledge.getKnownCompounds()
        )), false);
        return 1;
    }

    /**
     * Debug lookup for the JSON-driven FacetRegistry (Phase 0.B) — confirms data actually
     * loaded/reloaded correctly before the Resonometer exists to display it in-game.
     */
    private static int runFacetsCommand(CommandContext<CommandSourceStack> ctx) {
        Identifier id = IdentifierArgument.getId(ctx, "id");
        CommandSourceStack source = ctx.getSource();

        if (!FacetRegistry.has(id)) {
            source.sendFailure(Component.literal("No facet data registered for " + id));
            return 0;
        }

        FacetSignature signature = FacetRegistry.get(id);
        source.sendSuccess(() -> Component.literal(id + " -> " + signature), false);
        return 1;
    }

    private static int runWeaveCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPos.containing(source.getPosition());
        LevelChunk chunk = level.getChunkAt(pos);

        Optional<IChunkWeaveData> dataOpt = ChunkWeaveAccess.get(chunk);
        if (dataOpt.isEmpty()) {
            source.sendFailure(Component.literal("No Weave data attached to this chunk."));
            return 0;
        }

        IChunkWeaveData data = dataOpt.get();
        source.sendSuccess(() -> Component.literal(String.format(
                "Weave [%d, %d] — base: %.2f, current: %.2f, fray: %d",
                chunk.getPos().x(), chunk.getPos().z(),
                data.getBaseDensity(), data.getCurrentDensity(), data.getRegionalFray()
        )), false);

        // Bonus: if you're looking at a Wellspring, report its stored charge too — the only
        // way to see that number without cracking open the wand and watching it fill.
        findTargetedWellspring(source.getPlayerOrException(), level).ifPresent(wellspring ->
                source.sendSuccess(() -> Component.literal(String.format(
                        "Wellspring at %s — stored: %d / %d Wyrd (ancient: %b)",
                        wellspring.getBlockPos().toShortString(),
                        wellspring.getStoredWyrd(), wellspring.getMaxWyrd(), wellspring.isAncient()
                )), false));

        return 1;
    }

    private static Optional<WeaveWellspringBlockEntity> findTargetedWellspring(ServerPlayer player, ServerLevel level) {
        Vec3 from = player.getEyePosition();
        Vec3 to = from.add(player.calculateViewVector(player.getXRot(), player.getYRot()).scale(8.0));
        BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) return Optional.empty();

        BlockEntity blockEntity = level.getBlockEntity(hit.getBlockPos());
        return blockEntity instanceof WeaveWellspringBlockEntity wellspring ? Optional.of(wellspring) : Optional.empty();
    }

    /**
     * Debug-only convenience: places an (ancient-tier) Wellspring at the command sender's feet,
     * so testing the Wyrd loop doesn't depend on finding one in the world first. Uses
     * level.setBlock directly rather than a BlockItem, so setPlacedBy never fires and the new
     * Wellspring keeps its default isAncient = true.
     */
    private static int runWellspringCommand(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPos.containing(source.getPosition());

        level.setBlock(pos, ModBlocks.WEAVE_WELLSPRING.get().defaultBlockState(), 3);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof WeaveWellspringBlockEntity)) {
            source.sendFailure(Component.literal("Placed the block, but its block entity didn't attach — this is a bug."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Placed a Weave Wellspring at " + pos.toShortString()), false);
        return 1;
    }

    /** Debug-only convenience: places a Loom of Understanding at the command sender's feet. */
    private static int runLoomCommand(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPos.containing(source.getPosition());

        level.setBlock(pos, ModBlocks.LOOM_OF_UNDERSTANDING.get().defaultBlockState(), 3);
        source.sendSuccess(() -> Component.literal("Placed a Loom of Understanding at " + pos.toShortString()), false);
        return 1;
    }

    /**
     * Debug readout of the whole research tree against the command sender's completed set —
     * there's no Codex/Loom UI to show this yet, so this is the only way to confirm the JSON
     * loaded correctly and prerequisites resolve as expected.
     */
    private static int runResearchListCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();

        Optional<IPlayerResearchData> dataOpt = player.getCapability(ResearchCapabilities.PLAYER_RESEARCH_DATA).resolve();
        if (dataOpt.isEmpty()) {
            source.sendFailure(Component.literal("Player has no Research capability attached — this is a bug."));
            return 0;
        }

        IPlayerResearchData data = dataOpt.get();
        for (var entry : ResearchTree.entries()) {
            Identifier id = entry.getKey();
            ResearchNode node = entry.getValue();
            String status = data.hasCompleted(id) ? "[done]" : data.canStart(id) ? "[available]" : "[locked]";
            source.sendSuccess(() -> Component.literal(String.format(
                    "%s §7(§f%s§7)§r %s — %s", status, id, node.displayName(), node.description()
            )), false);
        }
        return 1;
    }

    /** Debug-only: force-completes a research node without needing its real trigger (craft/scan/etc). */
    private static int runResearchCompleteCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Identifier id = IdentifierArgument.getId(ctx, "id");
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();

        if (!ResearchTree.has(id)) {
            source.sendFailure(Component.literal("No research node registered with id " + id));
            return 0;
        }

        ResearchEvents.tryComplete(player, id);
        source.sendSuccess(() -> Component.literal("Completed (or already had) " + id), false);
        return 1;
    }

    /** Debug-only: un-completes a research node so it can be re-tested (re-solved, re-triggered) without a fresh player. */
    private static int runResearchForgetCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Identifier id = IdentifierArgument.getId(ctx, "id");
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();

        Optional<IPlayerResearchData> dataOpt = player.getCapability(ResearchCapabilities.PLAYER_RESEARCH_DATA).resolve();
        if (dataOpt.isEmpty()) {
            source.sendFailure(Component.literal("Player has no Research capability attached — this is a bug."));
            return 0;
        }

        boolean forgotten = dataOpt.get().forget(id);
        ResearchSync.sendTo(player);
        source.sendSuccess(() -> Component.literal(forgotten ? "Forgot " + id : id + " wasn't completed anyway"), false);
        return 1;
    }
}
