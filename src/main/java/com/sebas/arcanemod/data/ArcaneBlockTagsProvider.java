package com.sebas.arcanemod.data;

import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.VanillaBlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/**
 * Generates the {@code mineable/pickaxe} and {@code needs_iron_tool} entries for the two ore
 * blocks — replaces the two hand-written {@code data/minecraft/tags/block/*.json} files (deleted
 * once confirmed equivalent).
 * <p>
 * Extends vanilla's own {@code VanillaBlockTagsProvider} despite the name — Forge patches a
 * mod-facing constructor onto it (`(output, lookupProvider, modId, existingFileHelper)`) rather
 * than shipping a separate generic base class in this MC version. {@code addTags} is overridden
 * completely rather than calling {@code super.addTags(...)}, which would regenerate ALL of
 * vanilla's own block tags under this mod's provider run — this only needs to contribute the two
 * ore blocks to two already-vanilla-defined tags.
 */
public class ArcaneBlockTagsProvider extends VanillaBlockTagsProvider {
    public ArcaneBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ArcaneMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.WYRDSTONE_ORE.getKey(), ModBlocks.DEEPSLATE_WYRDSTONE_ORE.getKey(), ModBlocks.WEAVE_WELLSPRING.getKey());
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.WYRDSTONE_ORE.getKey(), ModBlocks.DEEPSLATE_WYRDSTONE_ORE.getKey());
        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.WANDWRIGHTS_BENCH.getKey(), ModBlocks.ADVANCED_WANDWRIGHTS_BENCH.getKey());
    }

    @Override
    public String getName() {
        return "Arcane Mod Block Tags";
    }
}
