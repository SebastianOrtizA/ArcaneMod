package com.sebas.arcanemod.data;

import com.sebas.arcanemod.block.ModBlocks;
import com.sebas.arcanemod.item.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generates blockstates/models/item-model-definitions for this mod's simple, template-shaped
 * content — replaces the hand-written files for the same blocks/items (deleted once confirmed
 * equivalent).
 * <p>
 * {@code weave_wellspring} is deliberately NOT in {@link #KNOWN_BLOCKS} — its two-box pedestal
 * shape has no matching {@code ModelTemplates}/{@code TexturedModel} constant (those only cover
 * template-shaped shapes: full cubes, columns, slabs, etc.), so its model/blockstate/item stay
 * hand-authored in {@code src/main/resources}. This is a real content gap, not laziness: vanilla
 * itself hand-writes custom block geometry rather than forcing it through the template system.
 * <p>
 * Does NOT extend vanilla's own {@code ModelProvider} — that class's {@code run()} constructs a
 * {@code BlockModelGenerators}/{@code ItemModelGenerators} and calls their {@code run()}, which
 * hardcodes generation for literally every vanilla block/item by name. Instead this builds the
 * same three collectors directly (all public nested classes of {@code ModelProvider}) and calls
 * only the generic, reusable helper methods for this mod's own content.
 */
public class ArcaneModelProvider implements DataProvider {
    private static final List<Block> KNOWN_BLOCKS = List.of(
            ModBlocks.WYRDSTONE_ORE.get(),
            ModBlocks.DEEPSLATE_WYRDSTONE_ORE.get(),
            ModBlocks.LOOM_OF_UNDERSTANDING.get(),
            ModBlocks.WANDWRIGHTS_BENCH.get(),
            ModBlocks.ADVANCED_WANDWRIGHTS_BENCH.get()
    );

    private static final List<Item> KNOWN_ITEMS = List.of(
            ModItems.WAND.get(),
            ModItems.CODEX_ARCANUM.get(),
            ModItems.CURSED_LECTERN.get(),
            ModItems.WYRDSTONE.get(),
            ModItems.WYRD_DUST.get(),
            ModItems.RESONOMETER.get(),
            ModItems.WYRDSTONE_ORE.get(),
            ModItems.DEEPSLATE_WYRDSTONE_ORE.get(),
            ModItems.LOOM_OF_UNDERSTANDING.get(),
            ModItems.WANDWRIGHTS_BENCH.get(),
            ModItems.ADVANCED_WANDWRIGHTS_BENCH.get(),
            ModItems.WOOD_CORE.get(),
            ModItems.WYRDSTONE_CORE.get(),
            ModItems.QUICKSILVER_CORE.get(),
            ModItems.VOIDGLASS_CORE.get(),
            ModItems.COPPER_CAP.get(),
            ModItems.IRON_CAP.get(),
            ModItems.PRISM_CAP.get(),
            ModItems.QUARTZ_CAP.get(),
            ModItems.LEATHER_BINDING.get(),
            ModItems.SILK_BINDING.get(),
            ModItems.WYRDTHREAD_BINDING.get(),
            ModItems.VOIDWEAVE_BINDING.get(),
            ModItems.RUBY_INLAY.get(),
            ModItems.SAPPHIRE_INLAY.get(),
            ModItems.EMERALD_INLAY.get(),
            ModItems.DIAMOND_INLAY.get(),
            ModItems.MODULAR_WAND.get(),
            ModItems.MODULAR_STAFF.get(),
            ModItems.SPELL_FOCUS.get()
    );

    private final PackOutput.PathProvider blockStatePath;
    private final PackOutput.PathProvider itemInfoPath;
    private final PackOutput.PathProvider modelPath;

    public ArcaneModelProvider(PackOutput output) {
        this.blockStatePath = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.itemInfoPath = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
        this.modelPath = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        ModelProvider.ItemInfoCollector itemModels = new ModelProvider.ItemInfoCollector(KNOWN_ITEMS::stream);
        ModelProvider.BlockStateGeneratorCollector blockStates = new ModelProvider.BlockStateGeneratorCollector(KNOWN_BLOCKS::stream);
        ModelProvider.SimpleModelCollector simpleModels = new ModelProvider.SimpleModelCollector();

        BlockModelGenerators blockModels = new BlockModelGenerators(blockStates, itemModels, simpleModels);
        for (Block block : KNOWN_BLOCKS) {
            // fullBlock(Block, ModelTemplate) is an instance method on BlockFamilyProvider, not a
            // static/top-level helper — BlockModelGenerators only exposes it via the protected
            // family(Block) convenience, so constructing the (public) provider directly with a
            // plain cube-all texture mapping is what a mod has to do instead.
            blockModels.new BlockFamilyProvider(TextureMapping.cube(block)).fullBlock(block, ModelTemplates.CUBE_ALL);
            // Mirrors vanilla's own (unscoped, hence not reused directly) default-block-item-model
            // step: a BlockItem with no explicit override just points at its own block's model.
            itemModels.accept(block.asItem(), ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(block)));
        }

        new ArcaneItemModelGenerators(itemModels, simpleModels).run();

        // Reuses vanilla's own lectern block model wholesale — not a flat-textured item at all,
        // so it goes through neither generateFlatItem nor the block-item default step above.
        itemModels.accept(ModItems.CURSED_LECTERN.get(),
                ItemModelUtils.plainModel(Identifier.withDefaultNamespace("block/lectern")));

        blockStates.validate();
        itemModels.finalizeAndValidate();

        return CompletableFuture.allOf(
                blockStates.save(cache, blockStatePath),
                simpleModels.save(cache, modelPath),
                itemModels.save(cache, itemInfoPath)
        );
    }

    @Override
    public String getName() {
        return "Arcane Mod Models";
    }
}
