package com.sebas.arcanemod.data;

import com.sebas.arcanemod.block.ModBlocks;
import com.sebas.arcanemod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

/**
 * Generates {@code data/arcanemod/loot_table/blocks/*.json} — replaces the four hand-written
 * files of the same names (deleted once confirmed equivalent).
 */
public class ArcaneBlockLootSubProvider extends BlockLootSubProvider {
    private static final List<Block> KNOWN_BLOCKS = List.of(
            ModBlocks.WYRDSTONE_ORE.get(),
            ModBlocks.DEEPSLATE_WYRDSTONE_ORE.get(),
            ModBlocks.WEAVE_WELLSPRING.get(),
            ModBlocks.LOOM_OF_UNDERSTANDING.get(),
            ModBlocks.WANDWRIGHTS_BENCH.get(),
            ModBlocks.ADVANCED_WANDWRIGHTS_BENCH.get()
    );

    protected ArcaneBlockLootSubProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.VANILLA_SET, registries);
    }

    /**
     * Forge patches this override point onto {@code BlockLootSubProvider} specifically so a mod's
     * completeness validation (every "known" block must have produced a table) doesn't walk the
     * WHOLE shared block registry — vanilla's own default is {@code BuiltInRegistries.BLOCK}
     * (every block from every loaded mod), which fails immediately on vanilla's own blocks having
     * no entry from a mod-scoped run at all. Confirmed by reading the actual
     * {@code BlockLootSubProvider.java.patch} in the Forge sources jar, not the plain decompiled
     * class — the patch is what adds this method.
     */
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return KNOWN_BLOCKS;
    }

    @Override
    protected void generate() {
        // createOreDrop handles the silk-touch (drop the block)/fortune (scale the raw drop) split
        // automatically — same shape as vanilla's own iron_ore/deepslate_iron_ore tables.
        add(ModBlocks.WYRDSTONE_ORE.get(), createOreDrop(ModBlocks.WYRDSTONE_ORE.get(), ModItems.WYRDSTONE.get()));
        add(ModBlocks.DEEPSLATE_WYRDSTONE_ORE.get(), createOreDrop(ModBlocks.DEEPSLATE_WYRDSTONE_ORE.get(), ModItems.WYRDSTONE.get()));

        dropSelf(ModBlocks.WEAVE_WELLSPRING.get());
        dropSelf(ModBlocks.LOOM_OF_UNDERSTANDING.get());
        dropSelf(ModBlocks.WANDWRIGHTS_BENCH.get());
        dropSelf(ModBlocks.ADVANCED_WANDWRIGHTS_BENCH.get());
    }
}
