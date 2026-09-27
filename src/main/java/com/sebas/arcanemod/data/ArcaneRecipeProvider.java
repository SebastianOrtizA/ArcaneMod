package com.sebas.arcanemod.data;

import com.sebas.arcanemod.item.ModItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.concurrent.CompletableFuture;

import static com.sebas.arcanemod.ArcaneMod.MODID;

/**
 * Generates {@code data/arcanemod/recipe/*.json} — replaces the five hand-written recipe files
 * of the same names (deleted once confirmed equivalent). See {@link Runner} for why this needs a
 * nested class rather than a plain {@code DataProvider.Factory}: {@code RecipeProvider} needs the
 * resolved {@code HolderLookup.Provider} (for {@code has(ItemLike)} criteria), which only exists
 * behind {@code GatherDataEvent#getLookupProvider()}'s {@code CompletableFuture}.
 */
public class ArcaneRecipeProvider extends RecipeProvider {
    public ArcaneRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        HolderGetter<Item> items = this.registries.lookupOrThrow(Registries.ITEM);

        // --- Stage 0 recipes ---

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.WAND.get())
                .pattern(" C ")
                .pattern(" S ")
                .pattern(" C ")
                .define('C', Items.COBBLESTONE)
                .define('S', Items.STICK)
                .unlockedBy("has_stick", has(Items.STICK))
                .save(output, key("wand"));

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.RESONOMETER.get())
                .pattern("DP")
                .pattern("ID")
                .define('D', ModItems.WYRD_DUST.get())
                .define('P', Items.GLASS_PANE)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_wyrd_dust", has(ModItems.WYRD_DUST.get()))
                .save(output, key("resonometer"));

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.LOOM_OF_UNDERSTANDING.get())
                .pattern("SSS")
                .pattern(" D ")
                .pattern("PPP")
                .define('S', Items.STRING)
                .define('D', ModItems.WYRD_DUST.get())
                .define('P', Items.OAK_PLANKS)
                .unlockedBy("has_wyrd_dust", has(ModItems.WYRD_DUST.get()))
                .save(output, key("loom_of_understanding"));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, ModItems.WYRD_DUST.get(), 2)
                .requires(ModItems.WYRDSTONE.get())
                .unlockedBy("has_wyrdstone", has(ModItems.WYRDSTONE.get()))
                .save(output, key("wyrd_dust_from_crafting"));

        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModItems.WYRDSTONE.get()),
                        RecipeCategory.MISC, CookingBookCategory.MISC,
                        ModItems.WYRD_DUST.get(), 0.1F, 200)
                .unlockedBy("has_wyrdstone", has(ModItems.WYRDSTONE.get()))
                .save(output, key("wyrd_dust_from_smelting"));

        // Codex recipe (fallback acquisition)
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, ModItems.CODEX_ARCANUM.get())
                .requires(Items.BOOK)
                .requires(ModItems.WYRD_DUST.get())
                .unlockedBy("has_wyrd_dust", has(ModItems.WYRD_DUST.get()))
                .save(output, key("codex_arcanum"));

        // --- Stage 1: Wandwright's Bench ---

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.WANDWRIGHTS_BENCH.get())
                .pattern("W W")
                .pattern(" C ")
                .pattern("P P")
                .define('W', ModItems.WYRDSTONE.get())
                .define('C', Items.CRAFTING_TABLE)
                .define('P', Items.OAK_PLANKS)
                .unlockedBy("has_wyrdstone", has(ModItems.WYRDSTONE.get()))
                .save(output, key("wandwrights_bench"));

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.ADVANCED_WANDWRIGHTS_BENCH.get())
                .pattern("WDW")
                .pattern("WBW")
                .pattern("WDW")
                .define('W', ModItems.WYRDSTONE.get())
                .define('D', ModItems.WYRD_DUST.get())
                .define('B', ModItems.WANDWRIGHTS_BENCH.get())
                .unlockedBy("has_wandwrights_bench", has(ModItems.WANDWRIGHTS_BENCH.get()))
                .save(output, key("advanced_wandwrights_bench"));

        // --- Stage 1: Wand Cores ---

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.WOOD_CORE.get())
                .pattern(" S ")
                .pattern(" D ")
                .pattern(" S ")
                .define('S', Items.STICK)
                .define('D', ModItems.WYRD_DUST.get())
                .unlockedBy("has_wyrd_dust", has(ModItems.WYRD_DUST.get()))
                .save(output, key("wood_core"));

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.WYRDSTONE_CORE.get())
                .pattern(" W ")
                .pattern(" D ")
                .pattern(" W ")
                .define('W', ModItems.WYRDSTONE.get())
                .define('D', ModItems.WYRD_DUST.get())
                .unlockedBy("has_wyrdstone", has(ModItems.WYRDSTONE.get()))
                .save(output, key("wyrdstone_core"));

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.QUICKSILVER_CORE.get())
                .pattern(" I ")
                .pattern("RD ")
                .pattern(" I ")
                .define('I', Items.IRON_INGOT)
                .define('R', Items.REDSTONE)
                .define('D', ModItems.WYRD_DUST.get())
                .unlockedBy("has_wyrd_dust", has(ModItems.WYRD_DUST.get()))
                .save(output, key("quicksilver_core"));

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.VOIDGLASS_CORE.get())
                .pattern(" O ")
                .pattern("DED")
                .pattern(" O ")
                .define('O', Items.OBSIDIAN)
                .define('E', Items.ENDER_PEARL)
                .define('D', ModItems.WYRD_DUST.get())
                .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
                .save(output, key("voidglass_core"));

        // --- Stage 1: Wand Caps ---

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.COPPER_CAP.get())
                .pattern("C C")
                .pattern(" D ")
                .define('C', Items.COPPER_INGOT)
                .define('D', ModItems.WYRD_DUST.get())
                .unlockedBy("has_wyrd_dust", has(ModItems.WYRD_DUST.get()))
                .save(output, key("copper_cap"));

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.IRON_CAP.get())
                .pattern("I I")
                .pattern(" D ")
                .define('I', Items.IRON_INGOT)
                .define('D', ModItems.WYRD_DUST.get())
                .unlockedBy("has_wyrd_dust", has(ModItems.WYRD_DUST.get()))
                .save(output, key("iron_cap"));

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.PRISM_CAP.get())
                .pattern("A P")
                .pattern(" D ")
                .define('A', Items.AMETHYST_SHARD)
                .define('P', Items.PRISMARINE_SHARD)
                .define('D', ModItems.WYRD_DUST.get())
                .unlockedBy("has_amethyst", has(Items.AMETHYST_SHARD))
                .save(output, key("prism_cap"));

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.QUARTZ_CAP.get())
                .pattern("Q Q")
                .pattern(" D ")
                .define('Q', Items.QUARTZ)
                .define('D', ModItems.WYRD_DUST.get())
                .unlockedBy("has_quartz", has(Items.QUARTZ))
                .save(output, key("quartz_cap"));

        // --- Stage 1: Wand Bindings ---

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.LEATHER_BINDING.get())
                .pattern("L L")
                .pattern(" S ")
                .define('L', Items.LEATHER)
                .define('S', Items.STRING)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(output, key("leather_binding"));

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.SILK_BINDING.get())
                .pattern("S S")
                .pattern(" D ")
                .define('S', Items.STRING)
                .define('D', ModItems.WYRD_DUST.get())
                .unlockedBy("has_wyrd_dust", has(ModItems.WYRD_DUST.get()))
                .save(output, key("silk_binding"));

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.WYRDTHREAD_BINDING.get())
                .pattern("D D")
                .pattern(" S ")
                .define('D', ModItems.WYRD_DUST.get())
                .define('S', Items.STRING)
                .unlockedBy("has_wyrd_dust", has(ModItems.WYRD_DUST.get()))
                .save(output, key("wyrdthread_binding"));

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.VOIDWEAVE_BINDING.get())
                .pattern("D O")
                .pattern(" S ")
                .define('D', ModItems.WYRD_DUST.get())
                .define('O', Items.OBSIDIAN)
                .define('S', Items.STRING)
                .unlockedBy("has_obsidian", has(Items.OBSIDIAN))
                .save(output, key("voidweave_binding"));

        // --- Stage 1: Inlays ---

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, ModItems.RUBY_INLAY.get())
                .requires(Items.REDSTONE_BLOCK)
                .requires(ModItems.WYRD_DUST.get())
                .unlockedBy("has_wyrd_dust", has(ModItems.WYRD_DUST.get()))
                .save(output, key("ruby_inlay"));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, ModItems.SAPPHIRE_INLAY.get())
                .requires(Items.LAPIS_BLOCK)
                .requires(ModItems.WYRD_DUST.get())
                .unlockedBy("has_wyrd_dust", has(ModItems.WYRD_DUST.get()))
                .save(output, key("sapphire_inlay"));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, ModItems.EMERALD_INLAY.get())
                .requires(Items.EMERALD)
                .requires(ModItems.WYRD_DUST.get())
                .unlockedBy("has_wyrd_dust", has(ModItems.WYRD_DUST.get()))
                .save(output, key("emerald_inlay"));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, ModItems.DIAMOND_INLAY.get())
                .requires(Items.DIAMOND)
                .requires(ModItems.WYRD_DUST.get())
                .unlockedBy("has_wyrd_dust", has(ModItems.WYRD_DUST.get()))
                .save(output, key("diamond_inlay"));

        // --- Stage 1: Spell Focus ---

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, ModItems.SPELL_FOCUS.get())
                .pattern(" A ")
                .pattern(" D ")
                .define('A', Items.AMETHYST_SHARD)
                .define('D', ModItems.WYRD_DUST.get())
                .unlockedBy("has_wyrd_dust", has(ModItems.WYRD_DUST.get()))
                .save(output, key("spell_focus"));
    }

    private static ResourceKey<Recipe<?>> key(String path) {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MODID, path));
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ArcaneRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Arcane Mod Recipes";
        }
    }
}
