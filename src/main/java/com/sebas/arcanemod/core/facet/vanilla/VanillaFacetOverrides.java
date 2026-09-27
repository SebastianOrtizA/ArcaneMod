package com.sebas.arcanemod.core.facet.vanilla;

import com.sebas.arcanemod.core.facet.Facet;
import com.sebas.arcanemod.core.facet.FacetSignature;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

import static com.sebas.arcanemod.core.facet.Facet.*;

/**
 * Hand-curated Facet signatures for standout blocks/items — the highest-priority rule in
 * {@link VanillaFacetData}'s chain. Everything here got individual thought about what that
 * specific thing represents; the tag-driven rules in {@link VanillaBlockFacets}/{@link
 * VanillaItemFacets} only cover what isn't listed here.
 * <p>
 * Organized by real-world/in-game category purely for editing sanity — it's all one flat map at
 * runtime. Amounts are 1–3: 1 = a trace, 2 = a clear presence, 3 = a defining property.
 */
final class VanillaFacetOverrides {
    private static final Map<Identifier, FacetSignature> MAP = new HashMap<>();

    private VanillaFacetOverrides() {}

    static Map<Identifier, FacetSignature> get() {
        return MAP;
    }

    private static void mc(String path, Facet a, int aAmt) {
        put("minecraft", path, of(a, aAmt));
    }

    private static void mc(String path, Facet a, int aAmt, Facet b, int bAmt) {
        put("minecraft", path, of(a, aAmt, b, bAmt));
    }

    private static void mc(String path, Facet a, int aAmt, Facet b, int bAmt, Facet c, int cAmt) {
        put("minecraft", path, of(a, aAmt, b, bAmt, c, cAmt));
    }

    private static void put(String namespace, String path, FacetSignature signature) {
        MAP.put(Identifier.fromNamespaceAndPath(namespace, path), signature);
    }

    private static FacetSignature of(Facet a, int aAmt) {
        return FacetSignature.of(Map.of(a, aAmt));
    }

    private static FacetSignature of(Facet a, int aAmt, Facet b, int bAmt) {
        return FacetSignature.of(Map.of(a, aAmt, b, bAmt));
    }

    private static FacetSignature of(Facet a, int aAmt, Facet b, int bAmt, Facet c, int cAmt) {
        return FacetSignature.of(Map.of(a, aAmt, b, bAmt, c, cAmt));
    }

    static {
        // --- Ores, ingots, gems (block form covers the ore, item form covers what it drops) ---
        mc("coal_ore", IGNIS, 2, TERRA, 1);
        mc("deepslate_coal_ore", IGNIS, 2, TERRA, 1);
        mc("coal", IGNIS, 2);
        mc("charcoal", IGNIS, 1);
        mc("iron_ore", TERRA, 2, IGNIS, 1);
        mc("deepslate_iron_ore", TERRA, 2, IGNIS, 1);
        mc("raw_iron", TERRA, 1, IGNIS, 1);
        mc("raw_iron_block", TERRA, 2, IGNIS, 1);
        mc("iron_ingot", TERRA, 1, IGNIS, 1);
        mc("iron_nugget", TERRA, 1);
        mc("iron_block", TERRA, 2, IGNIS, 1);
        mc("copper_ore", TERRA, 2, MOTUS, 1);
        mc("deepslate_copper_ore", TERRA, 2, MOTUS, 1);
        mc("raw_copper", TERRA, 1, MOTUS, 1);
        mc("raw_copper_block", TERRA, 2, MOTUS, 1);
        mc("copper_ingot", TERRA, 1, MOTUS, 1);
        mc("copper_block", TERRA, 2, MOTUS, 1);
        mc("gold_ore", TERRA, 1, LUX, 2);
        mc("deepslate_gold_ore", TERRA, 1, LUX, 2);
        mc("nether_gold_ore", IGNIS, 1, LUX, 2);
        mc("raw_gold", LUX, 2);
        mc("raw_gold_block", LUX, 2, TERRA, 1);
        mc("gold_ingot", LUX, 2);
        mc("gold_nugget", LUX, 1);
        mc("gold_block", LUX, 3);
        mc("diamond_ore", TERRA, 1, LUX, 2);
        mc("deepslate_diamond_ore", TERRA, 1, LUX, 2);
        mc("diamond", LUX, 2, ORDO, 1);
        mc("diamond_block", LUX, 3, ORDO, 1);
        mc("emerald_ore", TERRA, 1, COGNITIO, 2);
        mc("deepslate_emerald_ore", TERRA, 1, COGNITIO, 2);
        mc("emerald", COGNITIO, 2, LUX, 1);
        mc("emerald_block", COGNITIO, 2, LUX, 2);
        mc("lapis_ore", COGNITIO, 2, LUX, 1);
        mc("deepslate_lapis_ore", COGNITIO, 2, LUX, 1);
        mc("lapis_lazuli", COGNITIO, 2);
        mc("lapis_block", COGNITIO, 2, LUX, 1);
        mc("redstone_ore", MOTUS, 2, TERRA, 1);
        mc("deepslate_redstone_ore", MOTUS, 2, TERRA, 1);
        mc("redstone", MOTUS, 2);
        mc("redstone_block", MOTUS, 3);
        mc("ancient_debris", IGNIS, 2, TERRA, 2, UMBRA, 1);
        mc("netherite_scrap", IGNIS, 2, UMBRA, 1);
        mc("netherite_ingot", IGNIS, 2, UMBRA, 2, ORDO, 1);
        mc("netherite_block", IGNIS, 2, UMBRA, 2, ORDO, 2);
        mc("amethyst_block", COGNITIO, 1, LUX, 2);
        mc("budding_amethyst", COGNITIO, 1, LUX, 2);
        mc("amethyst_cluster", COGNITIO, 1, LUX, 2);
        mc("amethyst_shard", COGNITIO, 1, LUX, 1);
        mc("nether_quartz_ore", ORDO, 2, IGNIS, 1);
        mc("quartz", ORDO, 2);
        mc("quartz_block", ORDO, 2, LUX, 1);
        mc("glowstone", LUX, 2);
        mc("glowstone_dust", LUX, 2);

        // --- Nether ---
        mc("netherrack", IGNIS, 2, TERRA, 1);
        mc("nether_bricks", IGNIS, 1, ORDO, 1);
        mc("soul_sand", UMBRA, 2, MORTIS, 1);
        mc("soul_soil", UMBRA, 2, MORTIS, 1);
        mc("soul_torch", UMBRA, 2, LUX, 1);
        mc("soul_lantern", UMBRA, 2, LUX, 1);
        mc("soul_campfire", UMBRA, 2, LUX, 1);
        mc("shroomlight", LUX, 2, VITA, 1);
        mc("nether_wart_block", UMBRA, 1, VITA, 1);
        mc("warped_wart_block", UMBRA, 1, VITA, 1);
        mc("basalt", TERRA, 2, UMBRA, 1);
        mc("polished_basalt", TERRA, 2, UMBRA, 1);
        mc("smooth_basalt", TERRA, 2, UMBRA, 1);
        mc("blackstone", TERRA, 2, UMBRA, 1);
        mc("magma_block", IGNIS, 2, TERRA, 1);
        mc("obsidian", TERRA, 2, IGNIS, 1);
        mc("crying_obsidian", TERRA, 1, MORTIS, 1, LUX, 1);
        mc("respawn_anchor", ORDO, 2, VITA, 1, IGNIS, 1);
        mc("nether_star", LUX, 3, COGNITIO, 2, ORDO, 2);
        mc("wither_rose", MORTIS, 3, PERDO, 1);
        mc("wither_skeleton_skull", MORTIS, 2, UMBRA, 2);
        mc("nether_wart", IGNIS, 1, UMBRA, 1);
        mc("blaze_powder", IGNIS, 3);
        mc("blaze_rod", IGNIS, 3);
        mc("ghast_tear", AQUA, 1, MORTIS, 1, UMBRA, 1);
        mc("magma_cream", IGNIS, 2, TERRA, 1);

        // --- The End ---
        mc("end_stone", COGNITIO, 1, TERRA, 2);
        mc("end_stone_bricks", COGNITIO, 1, TERRA, 2);
        mc("purpur_block", COGNITIO, 1, ORDO, 1);
        mc("purpur_pillar", COGNITIO, 1, ORDO, 1);
        mc("end_rod", LUX, 2, ORDO, 1);
        mc("ender_pearl", MOTUS, 2, UMBRA, 1);
        mc("eye_of_ender", MOTUS, 2, COGNITIO, 1);
        mc("dragon_egg", COGNITIO, 3, VITA, 1, UMBRA, 1);
        mc("dragon_head", COGNITIO, 2, UMBRA, 1);
        mc("dragon_breath", UMBRA, 2, PERDO, 2);
        mc("shulker_shell", ORDO, 2, UMBRA, 1);
        mc("chorus_fruit", MOTUS, 3, UMBRA, 1);
        mc("chorus_flower", MOTUS, 1, VITA, 1, UMBRA, 1);
        mc("chorus_plant", MOTUS, 1, VITA, 1, UMBRA, 1);
        mc("end_crystal", COGNITIO, 2, UMBRA, 1, LUX, 1);

        // --- Redstone & mechanisms ---
        mc("redstone_torch", MOTUS, 1, LUX, 1);
        mc("repeater", MOTUS, 2, ORDO, 2);
        mc("comparator", MOTUS, 2, ORDO, 2);
        mc("piston", MOTUS, 2, ORDO, 1);
        mc("sticky_piston", MOTUS, 2, ORDO, 1);
        mc("observer", COGNITIO, 1, MOTUS, 1);
        mc("hopper", MOTUS, 1, ORDO, 1);
        mc("dispenser", MOTUS, 1, ORDO, 1);
        mc("dropper", MOTUS, 1, ORDO, 1);
        mc("daylight_detector", LUX, 1, COGNITIO, 1);
        mc("target", MOTUS, 1, ORDO, 1);
        mc("lodestone", MOTUS, 1, ORDO, 2);
        mc("compass", MOTUS, 2, COGNITIO, 1);
        mc("recovery_compass", MOTUS, 2, MORTIS, 1, COGNITIO, 1);
        mc("clock", COGNITIO, 2, MOTUS, 1);
        mc("rail", MOTUS, 2);
        mc("powered_rail", MOTUS, 2);
        mc("detector_rail", MOTUS, 2);
        mc("activator_rail", MOTUS, 2);
        mc("minecart", MOTUS, 2, ORDO, 1);
        mc("tnt", IGNIS, 3, PERDO, 3);
        mc("tnt_minecart", IGNIS, 2, PERDO, 2, MOTUS, 1);

        // --- Food, brewing, misc consumables ---
        mc("golden_apple", VITA, 3, LUX, 2);
        mc("enchanted_golden_apple", VITA, 4, LUX, 3, COGNITIO, 1);
        mc("golden_carrot", VITA, 2, LUX, 1);
        mc("cake", VITA, 2);
        mc("rotten_flesh", MORTIS, 2, PERDO, 1);
        mc("spider_eye", MORTIS, 1, UMBRA, 1);
        mc("fermented_spider_eye", MORTIS, 2, PERDO, 2);
        mc("poisonous_potato", MORTIS, 1, VITA, 1);
        mc("pufferfish", MORTIS, 1, AQUA, 1);
        mc("suspicious_stew", COGNITIO, 1, VITA, 1);
        mc("honey_bottle", VITA, 2);
        mc("milk_bucket", VITA, 2);
        mc("glistering_melon_slice", VITA, 1, LUX, 1);
        mc("rabbit_foot", MOTUS, 2);
        mc("phantom_membrane", AER, 2, UMBRA, 1);
        mc("turtle_helmet", AQUA, 1, ORDO, 1);
        mc("scute", AQUA, 1, ORDO, 1);
        mc("gunpowder", PERDO, 2, IGNIS, 1);
        mc("glass_bottle", ORDO, 1);
        mc("potion", AQUA, 1, COGNITIO, 1);
        mc("splash_potion", AQUA, 1, COGNITIO, 1);
        mc("lingering_potion", AQUA, 1, COGNITIO, 1);
        mc("experience_bottle", COGNITIO, 3);

        // --- Mob drops & misc materials ---
        mc("bone", MORTIS, 1, TERRA, 1);
        mc("bone_block", MORTIS, 1, TERRA, 1);
        mc("bone_meal", VITA, 1, TERRA, 1);
        mc("string", UMBRA, 1, MOTUS, 1);
        mc("cobweb", UMBRA, 1, MOTUS, 2);
        mc("feather", AER, 2);
        mc("leather", VITA, 1, TERRA, 1);
        mc("egg", VITA, 2);
        mc("slime_ball", VITA, 1, MOTUS, 1);
        mc("slime_block", VITA, 1, MOTUS, 1);
        mc("ink_sac", UMBRA, 1, PERDO, 1);
        mc("glow_ink_sac", LUX, 1, UMBRA, 1);
        mc("glow_berries", LUX, 1, VITA, 1);
        mc("glow_lichen", LUX, 1, VITA, 1);
        mc("honeycomb", VITA, 1, ORDO, 1);
        mc("honeycomb_block", VITA, 1, ORDO, 1);
        mc("nautilus_shell", AQUA, 2, COGNITIO, 1);
        mc("heart_of_the_sea", AQUA, 3, COGNITIO, 2);
        mc("trident", AQUA, 2, MOTUS, 2);
        mc("prismarine_shard", AQUA, 2, LUX, 1);
        mc("prismarine_crystals", AQUA, 2, LUX, 1);
        mc("prismarine", AQUA, 1, TERRA, 1, LUX, 1);
        mc("prismarine_bricks", AQUA, 1, TERRA, 1, LUX, 1);
        mc("dark_prismarine", AQUA, 1, TERRA, 1, LUX, 1);
        mc("sea_lantern", LUX, 2, AQUA, 1);
        mc("sponge", AQUA, 2, VITA, 1);
        mc("wet_sponge", AQUA, 2, VITA, 1);
        mc("kelp", VITA, 1, AQUA, 2);
        mc("seagrass", VITA, 1, AQUA, 2);
        mc("sea_pickle", VITA, 1, AQUA, 2);

        // --- Liquids / weather-adjacent ---
        mc("water", AQUA, 3);
        mc("water_bucket", AQUA, 2);
        mc("lava", IGNIS, 3);
        mc("lava_bucket", IGNIS, 2);
        mc("powder_snow_bucket", AQUA, 1, TERRA, 1);
        mc("ice", AQUA, 2, TERRA, 1);
        mc("packed_ice", AQUA, 2, TERRA, 1);
        mc("blue_ice", AQUA, 2, TERRA, 1);
        mc("snow", AQUA, 1, TERRA, 1);
        mc("snow_block", AQUA, 1, TERRA, 1);
        mc("powder_snow", AQUA, 1, TERRA, 1);
        mc("jack_o_lantern", LUX, 2, UMBRA, 1);

        // --- Iconic equipment ---
        mc("elytra", AER, 3, MOTUS, 2);
        mc("totem_of_undying", VITA, 3, MORTIS, 1, ORDO, 1);
        mc("book", COGNITIO, 2);
        mc("writable_book", COGNITIO, 2);
        mc("written_book", COGNITIO, 2);
        mc("knowledge_book", COGNITIO, 3);
        mc("bookshelf", COGNITIO, 2, TERRA, 1);
        mc("chiseled_bookshelf", COGNITIO, 2, TERRA, 1);
        mc("lectern", COGNITIO, 2, ORDO, 1);
        mc("enchanting_table", COGNITIO, 3, LUX, 1);
        mc("beacon", LUX, 3, ORDO, 2);
        mc("conduit", AQUA, 3, ORDO, 1);
        mc("spyglass", LUX, 1, COGNITIO, 1);
        mc("map", COGNITIO, 2);
        mc("filled_map", COGNITIO, 2);
        mc("name_tag", ORDO, 1, COGNITIO, 1);
        mc("lead", ORDO, 1, MOTUS, 1);
        mc("saddle", MOTUS, 1, ORDO, 1);
        mc("bow", MOTUS, 2, AER, 1);
        mc("crossbow", MOTUS, 2, ORDO, 1);
        mc("mace", MOTUS, 2, PERDO, 1);
        mc("shield", ORDO, 2);

        // --- Common foods (item components aren't bound during resource reload, so these can't
        // be derived from nutrition dynamically — see VanillaItemFacets) ---
        mc("apple", VITA, 1);
        mc("bread", VITA, 1);
        mc("cookie", VITA, 1);
        mc("melon_slice", VITA, 1);
        mc("carrot", VITA, 1);
        mc("potato", VITA, 1);
        mc("baked_potato", VITA, 2);
        mc("beetroot", VITA, 1);
        mc("beetroot_soup", VITA, 2);
        mc("mushroom_stew", VITA, 2);
        mc("rabbit_stew", VITA, 2);
        mc("pumpkin_pie", VITA, 2);
        mc("sweet_berries", VITA, 1);
        mc("dried_kelp", VITA, 1);
        mc("beef", VITA, 1);
        mc("cooked_beef", VITA, 2);
        mc("porkchop", VITA, 1);
        mc("cooked_porkchop", VITA, 2);
        mc("mutton", VITA, 1);
        mc("cooked_mutton", VITA, 2);
        // Deliberately NOT overriding "chicken"/"rabbit"/"cod"/"salmon" here — those item ids
        // are spelled identically to their source mob's EntityType id, and this map is checked
        // first for every registry (see VanillaFacetData), so an entry here would shadow
        // VanillaEntityFacets' own (differently-tuned) values for the live animal. The raw/cooked
        // meat items fall through to the ItemTags.MEAT/FISHES tag rules instead.
        mc("cod", VITA, 1, AQUA, 1);
        mc("cooked_cod", VITA, 2);
        mc("salmon", VITA, 1, AQUA, 1);
        mc("cooked_salmon", VITA, 2);
        mc("tropical_fish", VITA, 1, AQUA, 1);

        // --- Basic crafting staples — easy to overlook since none of them are "iconic," but
        // they're some of the most commonly-held items in the game ---
        mc("stick", VITA, 1, TERRA, 1);
        mc("bowl", VITA, 1, TERRA, 1);
        mc("flint", TERRA, 1, IGNIS, 1);
        mc("flint_and_steel", IGNIS, 2, ORDO, 1);
        mc("brick", TERRA, 2);
        mc("bricks", TERRA, 2, ORDO, 1);
        mc("netherbrick", IGNIS, 1, TERRA, 1);
        mc("paper", COGNITIO, 1);
        mc("clay", TERRA, 1, AQUA, 1);
        mc("clay_ball", TERRA, 1, AQUA, 1);
        mc("gravel", TERRA, 1);
        mc("glass", ORDO, 1, LUX, 1);
        mc("glass_pane", ORDO, 1, LUX, 1);
        mc("sugar", VITA, 1);
        mc("wheat", VITA, 1);
        mc("shears", MOTUS, 1, ORDO, 1);
        mc("fishing_rod", MOTUS, 1, AQUA, 1);
        mc("flower_pot", TERRA, 1, ORDO, 1);
        mc("brewing_stand", COGNITIO, 1, IGNIS, 1);
        mc("cauldron", AQUA, 1, ORDO, 1);
        mc("crafting_table", ORDO, 1, TERRA, 1);
        mc("furnace", IGNIS, 2, ORDO, 1);
        mc("chest", ORDO, 1, TERRA, 1);
        mc("ladder", TERRA, 1, MOTUS, 1);

        // --- Seeds, spawners, misc leftovers not caught by any tag rule ---
        mc("wheat_seeds", VITA, 1);
        mc("beetroot_seeds", VITA, 1);
        mc("melon_seeds", VITA, 1);
        mc("pumpkin_seeds", VITA, 1);
        mc("torchflower_seeds", VITA, 1);
        mc("pitcher_pod", VITA, 1);
        mc("enchanted_book", COGNITIO, 3);
        mc("spawner", VITA, 1, MOTUS, 1, COGNITIO, 1);
        mc("turtle_egg", AQUA, 1, VITA, 1, ORDO, 1);

        // --- Our own items ---
        put("arcanemod", "wyrdstone", of(COGNITIO, 2, TERRA, 1));
        put("arcanemod", "wyrd_dust", of(COGNITIO, 1));
    }
}
