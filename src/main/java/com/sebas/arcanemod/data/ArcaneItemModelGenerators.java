package com.sebas.arcanemod.data;

import com.sebas.arcanemod.item.ModItems;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.resources.Identifier;

import java.util.function.BiConsumer;

/**
 * {@code generateFlatItem} is {@code protected} on the vanilla base class — subclassing (rather
 * than calling it from {@link ArcaneModelProvider} directly) is the only way to reach it. Overrides
 * the public {@code run()} entirely rather than calling {@code super.run()}, which hardcodes flat
 * models for every vanilla item by name.
 */
class ArcaneItemModelGenerators extends ItemModelGenerators {
    ArcaneItemModelGenerators(ItemModelOutput itemModelOutput, BiConsumer<Identifier, ModelInstance> modelOutput) {
        super(itemModelOutput, modelOutput);
    }

    @Override
    public void run() {
        generateFlatItem(ModItems.WAND.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        generateFlatItem(ModItems.RESONOMETER.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        generateFlatItem(ModItems.CODEX_ARCANUM.get(), ModelTemplates.FLAT_ITEM);
        generateFlatItem(ModItems.WYRD_DUST.get(), ModelTemplates.FLAT_ITEM);
        generateFlatItem(ModItems.WYRDSTONE.get(), ModelTemplates.FLAT_ITEM);

        // Stage 1: Wand components
        generateFlatItem(ModItems.WOOD_CORE.get(), ModelTemplates.FLAT_ITEM);
        generateFlatItem(ModItems.WYRDSTONE_CORE.get(), ModelTemplates.FLAT_ITEM);
        generateFlatItem(ModItems.QUICKSILVER_CORE.get(), ModelTemplates.FLAT_ITEM);
        generateFlatItem(ModItems.VOIDGLASS_CORE.get(), ModelTemplates.FLAT_ITEM);
        generateFlatItem(ModItems.COPPER_CAP.get(), ModelTemplates.FLAT_ITEM);
        generateFlatItem(ModItems.IRON_CAP.get(), ModelTemplates.FLAT_ITEM);
        generateFlatItem(ModItems.PRISM_CAP.get(), ModelTemplates.FLAT_ITEM);
        generateFlatItem(ModItems.QUARTZ_CAP.get(), ModelTemplates.FLAT_ITEM);
        generateFlatItem(ModItems.LEATHER_BINDING.get(), ModelTemplates.FLAT_ITEM);
        generateFlatItem(ModItems.SILK_BINDING.get(), ModelTemplates.FLAT_ITEM);
        generateFlatItem(ModItems.WYRDTHREAD_BINDING.get(), ModelTemplates.FLAT_ITEM);
        generateFlatItem(ModItems.VOIDWEAVE_BINDING.get(), ModelTemplates.FLAT_ITEM);
        generateFlatItem(ModItems.RUBY_INLAY.get(), ModelTemplates.FLAT_ITEM);
        generateFlatItem(ModItems.SAPPHIRE_INLAY.get(), ModelTemplates.FLAT_ITEM);
        generateFlatItem(ModItems.EMERALD_INLAY.get(), ModelTemplates.FLAT_ITEM);
        generateFlatItem(ModItems.DIAMOND_INLAY.get(), ModelTemplates.FLAT_ITEM);

        // Stage 1: Modular wand/staff and focus
        generateFlatItem(ModItems.MODULAR_WAND.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        generateFlatItem(ModItems.MODULAR_STAFF.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        generateFlatItem(ModItems.SPELL_FOCUS.get(), ModelTemplates.FLAT_ITEM);
    }
}
