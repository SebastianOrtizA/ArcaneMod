# Datagen Provider Patterns (Annotated Skeletons)

## Block Registration + Loot + Model + Lang + Tags

```java
// --- ModBlocks.java ---
public static final RegistryObject<Block> MY_BLOCK = BLOCKS.register("my_block",
    () -> new Block(BlockBehaviour.Properties.of()
        .strength(3.0F, 3.0F)
        .requiresCorrectToolForDrops()
        .setId(BLOCKS.key("my_block"))));

// --- ModItems.java (BlockItem) ---
public static final RegistryObject<Item> MY_BLOCK_ITEM = ITEMS.register("my_block",
    () -> new BlockItem(ModBlocks.MY_BLOCK.get(),
        new Item.Properties().setId(ITEMS.key("my_block")).useBlockDescriptionPrefix()));

// --- ArcaneBlockLootSubProvider ---
// Add to KNOWN_BLOCKS set, then in generate():
dropSelf(ModBlocks.MY_BLOCK.get());
// Or for ore-style drops:
// add(ModBlocks.MY_ORE.get(), createOreDrop(ModBlocks.MY_ORE.get(), ModItems.MY_DROP.get()));

// --- ArcaneModelProvider ---
// Add block to KNOWN_BLOCKS, add item to KNOWN_ITEMS, then in run():
// Block model (full cube with all-same texture):
blockModels.new BlockFamilyProvider(TextureMapping.cube(ModBlocks.MY_BLOCK.get()))
    .fullBlock(ModBlocks.MY_BLOCK.get(), ModelTemplates.CUBE_ALL);
// Block-item model (points at block model):
itemModels.accept(ModBlocks.MY_BLOCK.get().asItem(),
    ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(ModBlocks.MY_BLOCK.get())));

// --- ArcaneLanguageProvider ---
addBlock(ModBlocks.MY_BLOCK, "My Block");

// --- ArcaneBlockTagsProvider.addTags() ---
tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.MY_BLOCK.get());
tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.MY_BLOCK.get());
```

## Item Registration + Model + Lang + Recipe

```java
// --- ModItems.java ---
public static final RegistryObject<Item> MY_ITEM = ITEMS.register("my_item",
    () -> new Item(new Item.Properties()
        .stacksTo(16)
        .setId(ITEMS.key("my_item"))));

// --- ArcaneItemModelGenerators.run() ---
generateFlatItem(ModItems.MY_ITEM.get(), ModelTemplates.FLAT_ITEM);
// For held tools: ModelTemplates.FLAT_HANDHELD_ITEM

// --- ArcaneLanguageProvider ---
addItem(ModItems.MY_ITEM, "My Item");

// --- ArcaneRecipeProvider.Runner.buildRecipes() ---
// Shaped recipe:
ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MY_ITEM.get())
    .pattern("WSW")
    .pattern(" S ")
    .define('W', ModItems.WYRD_DUST.get())
    .define('S', Items.STICK)
    .unlockedBy("has_wyrd_dust", has(ModItems.WYRD_DUST.get()))
    .save(output);

// Shapeless recipe:
ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MY_ITEM.get(), 4)
    .requires(ModBlocks.MY_BLOCK.get())
    .unlockedBy("has_my_block", has(ModBlocks.MY_BLOCK.get()))
    .save(output);

// Smelting recipe:
SimpleCookingRecipeBuilder.smelting(
        Ingredient.of(ModBlocks.MY_ORE.get()),
        RecipeCategory.MISC,
        ModItems.MY_ITEM.get(),
        0.7F,  // experience
        200)   // cooking time in ticks
    .unlockedBy("has_my_ore", has(ModBlocks.MY_ORE.get()))
    .save(output, Identifier.fromNamespaceAndPath("arcanemod", "my_item_from_smelting"));
```

## Entity Registration + Loot + Lang

```java
// --- ModEntityTypes.java ---
public static final RegistryObject<EntityType<MyEntity>> MY_ENTITY =
    ENTITY_TYPES.register("my_entity",
        () -> EntityType.Builder.of(MyEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .build(ENTITY_TYPES.key("my_entity")));

// --- ArcaneEntityLootSubProvider ---
// In getKnownEntityTypes():
return Stream.of(ModEntityTypes.MY_ENTITY.get());
// In generate():
add(ModEntityTypes.MY_ENTITY.get(),
    LootTable.lootTable()
        .withPool(LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1.0F))
            .add(LootItem.lootTableItem(Items.ROTTEN_FLESH)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F)))
                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(registries,
                    UniformGenerator.between(0.0F, 1.0F))))));

// --- ArcaneLanguageProvider ---
addEntityType(ModEntityTypes.MY_ENTITY, "My Entity");
```

## Datagen Coordinator (ArcaneDataGenerators)

```java
// Client providers:
generator.getVanillaPack(event.includeClient())
    .addProvider(MyClientProvider::new);

// Server providers:
generator.getVanillaPack(event.includeServer())
    .addProvider(output -> new MyServerProvider(output, event.getLookupProvider()));

// Loot tables (wraps sub-providers):
generator.getVanillaPack(event.includeServer())
    .addProvider(output -> new LootTableProvider(output, Set.of(),
        List.of(
            new LootTableProvider.SubProviderEntry(MyBlockLoot::new, LootContextParamSets.BLOCK),
            new LootTableProvider.SubProviderEntry(MyEntityLoot::new, LootContextParamSets.ENTITY)
        ), event.getLookupProvider()));
```
