package com.sebas.arcanemod.data;

import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.block.ModBlocks;
import com.sebas.arcanemod.entity.ModEntityTypes;
import com.sebas.arcanemod.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * Generates {@code assets/arcanemod/lang/en_us.json} — replaces the hand-written file of the same
 * name (deleted once this was confirmed to produce equivalent output).
 * <p>
 * Blocks that also have a {@code BlockItem} (ore blocks, the Wellspring, the Loom) only get an
 * {@code addBlock} entry, not a separate {@code addItem} one — {@code BlockItem#getDescriptionId()}
 * delegates to the block's own translation key, so a second entry would just be an unused
 * duplicate (this matches what the hand-written file already did).
 */
public class ArcaneLanguageProvider extends LanguageProvider {
    public ArcaneLanguageProvider(PackOutput output) {
        super(output, ArcaneMod.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // --- Stage 0 ---
        addItem(ModItems.WAND, "Cobblestone Wand");
        addItem(ModItems.CODEX_ARCANUM, "Codex Arcanum");
        addItem(ModItems.CURSED_LECTERN, "Cursed Lectern");
        addItem(ModItems.WYRDSTONE, "Wyrdstone");
        addItem(ModItems.WYRD_DUST, "Wyrd Dust");
        addItem(ModItems.RESONOMETER, "Resonometer");

        addBlock(ModBlocks.WYRDSTONE_ORE, "Wyrdstone Ore");
        addBlock(ModBlocks.DEEPSLATE_WYRDSTONE_ORE, "Deepslate Wyrdstone Ore");
        addBlock(ModBlocks.WEAVE_WELLSPRING, "Weave Wellspring");
        addBlock(ModBlocks.LOOM_OF_UNDERSTANDING, "Loom of Understanding");

        addEntityType(ModEntityTypes.FRAYED_ZOMBIE, "Frayed Zombie");
        addEntityType(ModEntityTypes.FRAYED_SKELETON, "Frayed Skeleton");
        addEntityType(ModEntityTypes.FRAYED_SPIDER, "Frayed Spider");

        // --- Stage 1: Blocks ---
        addBlock(ModBlocks.WANDWRIGHTS_BENCH, "Wandwright's Bench");
        addBlock(ModBlocks.ADVANCED_WANDWRIGHTS_BENCH, "Advanced Wandwright's Bench");

        // --- Stage 1: Wand Components ---
        addItem(ModItems.WOOD_CORE, "Wood Core");
        addItem(ModItems.WYRDSTONE_CORE, "Wyrdstone Core");
        addItem(ModItems.QUICKSILVER_CORE, "Quicksilver Core");
        addItem(ModItems.VOIDGLASS_CORE, "Voidglass Core");

        addItem(ModItems.COPPER_CAP, "Copper Cap");
        addItem(ModItems.IRON_CAP, "Iron Cap");
        addItem(ModItems.PRISM_CAP, "Prism Cap");
        addItem(ModItems.QUARTZ_CAP, "Quartz Cap");

        addItem(ModItems.LEATHER_BINDING, "Leather Binding");
        addItem(ModItems.SILK_BINDING, "Silk Binding");
        addItem(ModItems.WYRDTHREAD_BINDING, "Wyrdthread Binding");
        addItem(ModItems.VOIDWEAVE_BINDING, "Voidweave Binding");

        addItem(ModItems.RUBY_INLAY, "Ruby Inlay");
        addItem(ModItems.SAPPHIRE_INLAY, "Sapphire Inlay");
        addItem(ModItems.EMERALD_INLAY, "Emerald Inlay");
        addItem(ModItems.DIAMOND_INLAY, "Diamond Inlay");

        // --- Stage 1: Wand/Staff/Focus ---
        addItem(ModItems.MODULAR_WAND, "Modular Wand");
        addItem(ModItems.MODULAR_STAFF, "Modular Staff");
        addItem(ModItems.SPELL_FOCUS, "Spell Focus");

        addEntityType(ModEntityTypes.SPARK_BOLT, "Spark Bolt");

        // --- UI / Tooltips ---
        add("creativetab.arcanemod.arcane_tab", "Arcane Mod");
        add("tooltip.arcanemod.wyrd_charge", "Wyrd: %s / %s");
        add("hud.arcanemod.no_resonance", "No magical resonance detected");

        add("container.arcanemod.wandwrights_bench", "Wandwright's Bench");

        // Wand component tooltips
        add("tooltip.arcanemod.core_capacity", "Capacity: %s");
        add("tooltip.arcanemod.core_efficiency", "Efficiency: %s");
        add("tooltip.arcanemod.wand_core", "Core: %s");
        add("tooltip.arcanemod.wand_cap", "Cap: %s");
        add("tooltip.arcanemod.wand_binding", "Binding: %s");
        add("tooltip.arcanemod.wand_inlay", "Inlay: %s");
        add("tooltip.arcanemod.foci_slots", "Foci Slots: %s");

        // Cap effects
        add("tooltip.arcanemod.cap_effect.copper", "+25% Wyrd draw speed, +10% cast speed");
        add("tooltip.arcanemod.cap_effect.iron", "+50% durability, no elemental bias");
        add("tooltip.arcanemod.cap_effect.prism", "Single-target spells hit up to 3 targets at 50% power");
        add("tooltip.arcanemod.cap_effect.quartz", "+50% spell range");

        // Binding effects
        add("tooltip.arcanemod.binding_effect.leather", "No bonus (baseline)");
        add("tooltip.arcanemod.binding_effect.silk", "-15% cooldown reduction");
        add("tooltip.arcanemod.binding_effect.wyrdthread", "+20% Wyrd capacity bonus");
        add("tooltip.arcanemod.binding_effect.voidweave", "+15% Fray resistance");

        // Inlay effects
        add("tooltip.arcanemod.inlay_effect.ruby", "+10% Wyrd regen near Wellspring");
        add("tooltip.arcanemod.inlay_effect.sapphire", "10% chance to not consume Wyrd on cast");
        add("tooltip.arcanemod.inlay_effect.emerald", "+1 Foci slot");
        add("tooltip.arcanemod.inlay_effect.diamond", "+25% durability");

        // Focus tooltips
        add("tooltip.arcanemod.focus_spell", "Spell: %s");
        add("tooltip.arcanemod.focus_charges", "Charges: %s / %s");
        add("tooltip.arcanemod.focus_inert", "Inert — needs recharging");
        add("spell.arcanemod.spark", "Spark");

        // Messages
        add("message.arcanemod.focus_slots_full", "No empty focus slots!");

        // Keybinds
        add("key.category.arcanemod.arcane", "Codex Arcanum");
        add("key.arcanemod.cycle_focus", "Cycle Focus");
    }
}
