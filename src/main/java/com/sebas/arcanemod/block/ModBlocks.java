package com.sebas.arcanemod.block;

import com.sebas.arcanemod.ArcaneMod;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry holder for all blocks this mod adds. Follows the same
 * DeferredRegister + RegistryObject shape as {@link com.sebas.arcanemod.item.ModItems}.
 */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ArcaneMod.MODID);

    // DropExperienceBlock + these exact Properties are copied straight from vanilla's
    // IRON_ORE / DEEPSLATE_IRON_ORE registration (net.minecraft.world.level.block.Blocks,
    // decompiled) — same drop-behavior shape (silk touch vs. raw crystal is a loot table
    // concern, not a block-class concern), same rough dig speed as iron.
    public static final RegistryObject<Block> WYRDSTONE_ORE = BLOCKS.register("wyrdstone_ore",
            () -> new DropExperienceBlock(ConstantInt.of(0),
                    BlockBehaviour.Properties.of()
                            .setId(BLOCKS.key("wyrdstone_ore"))
                            .mapColor(MapColor.STONE)
                            .instrument(NoteBlockInstrument.BASEDRUM)
                            .requiresCorrectToolForDrops()
                            .strength(3.0F, 3.0F)));

    // NOTE: deliberately not built from BlockBehaviour.Properties.ofLegacyCopy(WYRDSTONE_ORE.get()) —
    // RegistryObject.get() isn't guaranteed to be bound yet while both entries' factories are still
    // being processed by the same RegisterEvent, so each block's Properties is spelled out independently
    // instead (mirroring vanilla's literal IRON_ORE/DEEPSLATE_IRON_ORE values, just without the copy call).
    public static final RegistryObject<Block> DEEPSLATE_WYRDSTONE_ORE = BLOCKS.register("deepslate_wyrdstone_ore",
            () -> new DropExperienceBlock(ConstantInt.of(0),
                    BlockBehaviour.Properties.of()
                            .setId(BLOCKS.key("deepslate_wyrdstone_ore"))
                            .mapColor(MapColor.DEEPSLATE)
                            .instrument(NoteBlockInstrument.BASEDRUM)
                            .requiresCorrectToolForDrops()
                            .strength(4.5F, 3.0F)
                            .sound(SoundType.DEEPSLATE)));

    // Unbreakable-by-design would be more lore-appropriate for an "ancient" find, but for
    // Phase 0.A simplicity it's just a sturdy block; revisit once player-built Wellsprings
    // (Rite of Wellspring Raising, a later stage) need to feel different from ancient ones.
    public static final RegistryObject<Block> WEAVE_WELLSPRING = BLOCKS.register("weave_wellspring",
            () -> new WeaveWellspringBlock(BlockBehaviour.Properties.of()
                    .setId(BLOCKS.key("weave_wellspring"))
                    .mapColor(MapColor.STONE)
                    .strength(3.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final RegistryObject<Block> LOOM_OF_UNDERSTANDING = BLOCKS.register("loom_of_understanding",
            () -> new LoomOfUnderstandingBlock(BlockBehaviour.Properties.of()
                    .setId(BLOCKS.key("loom_of_understanding"))
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)));

    public static final RegistryObject<Block> WANDWRIGHTS_BENCH = BLOCKS.register("wandwrights_bench",
            () -> new WandwrightsBenchBlock(BlockBehaviour.Properties.of()
                    .setId(BLOCKS.key("wandwrights_bench"))
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.5F)
                    .sound(SoundType.WOOD), false));

    public static final RegistryObject<Block> ADVANCED_WANDWRIGHTS_BENCH = BLOCKS.register("advanced_wandwrights_bench",
            () -> new WandwrightsBenchBlock(BlockBehaviour.Properties.of()
                    .setId(BLOCKS.key("advanced_wandwrights_bench"))
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.5F)
                    .sound(SoundType.WOOD), true));
}
