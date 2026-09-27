---
name: datagen
description: >
  Load this skill when adding or modifying data-generated content: block
  models, item models, blockstates, loot tables, recipes, tags, or language
  entries. Load when: adding a new block, item, entity, or recipe; changing
  drop tables; adding translation strings; modifying models; or when you
  see "datagen", "model", "loot", "recipe", "tag", "lang", "translation",
  "provider", "generated resources", or any data-generation task. This
  version uses vanilla's own model system (NOT the old Forge DSL) and has
  specific JSON schema quirks that differ from every published tutorial.
---

# Datagen for Codex Arcanum

## Existing Provider Classes

All providers are in `com.sebas.arcanemod.data` and wired in `ArcaneDataGenerators.gatherData()`.

| Provider | Side | What it generates |
|----------|------|-------------------|
| `ArcaneLanguageProvider` | Client | `en_us.json` translations |
| `ArcaneModelProvider` | Client | Block models, blockstates, block-item models |
| `ArcaneItemModelGenerators` | Client | Standalone item models (extends `ItemModelGenerators`) |
| `ArcaneRecipeProvider` + `.Runner` | Server | Crafting & smelting recipes |
| `ArcaneBlockTagsProvider` | Server | Block tags (mineable, tool tier) |
| `ArcaneBlockLootSubProvider` | Server | Block drop tables |
| `ArcaneEntityLootSubProvider` | Server | Entity drop tables |

## Forge-Patched Scoping (CRITICAL)

`BlockLootSubProvider`, `EntityLootSubProvider`, and vanilla's `ModelProvider` all validate against
the **entire shared registry** by default. A mod-scoped provider that only adds its own content
fails immediately with errors like `Missing loottable 'minecraft:blocks/stone'`.

Forge patches override points onto each one — **only visible in `.java.patch` files** in the
Forge sources jar:

- `getKnownBlocks()` returns `Iterable<Block>` — override to return only this mod's blocks
- `getKnownEntityTypes()` returns `Stream<EntityType<?>>` — override for this mod's entities
- For models: build collectors with mod-scoped `Supplier<Stream<T>>`

See `references/provider-patterns.md` for skeleton templates.

## Adding a New Block to Datagen

1. **Loot**: Add to `ArcaneBlockLootSubProvider`
   - Add block to `KNOWN_BLOCKS` set
   - Add loot generation in `generate()` (e.g., `dropSelf(block)` or `createOreDrop(...)`)

2. **Model**: Add to `ArcaneModelProvider`
   - Add block to `KNOWN_BLOCKS` set
   - In `run()`: generate blockstate + block model (skip for custom geometry)
   - Add block-item model line for the `BlockItem`

3. **Language**: Add to `ArcaneLanguageProvider`
   - `addBlock(ModBlocks.NEW_BLOCK, "Display Name")`

4. **Tags**: Add to `ArcaneBlockTagsProvider.addTags()` if needed
   - Mineable tag (`BlockTags.MINEABLE_WITH_PICKAXE`, etc.)
   - Tool tier tag (`BlockTags.NEEDS_IRON_TOOL`, etc.)

5. **Creative tab**: Add to `ModCreativeTabs.ARCANE_TAB` `displayItems`

## Adding a New Item to Datagen

1. **Model**: Add to `ArcaneModelProvider.KNOWN_ITEMS` and `ArcaneItemModelGenerators.run()`
   - `generateFlatItem(ModItems.NEW_ITEM, ModelTemplates.FLAT_ITEM)` for standard items
   - `ModelTemplates.FLAT_HANDHELD_ITEM` for held tools (parent `item/handheld`)

2. **Language**: `addItem(ModItems.NEW_ITEM, "Display Name")` in `ArcaneLanguageProvider`

3. **Recipe**: Add to `ArcaneRecipeProvider.Runner.buildRecipes()` if craftable

4. **Creative tab**: Add to `ModCreativeTabs.ARCANE_TAB` `displayItems`

## Adding a New Entity to Datagen

1. **Loot**: Add to `ArcaneEntityLootSubProvider`
   - Add to `getKnownEntityTypes()` stream
   - Add loot generation in `generate()`

2. **Language**: `addEntityType(ModEntityTypes.NEW_ENTITY, "Display Name")` in
   `ArcaneLanguageProvider`

## Model System Notes

This version uses vanilla's own `net.minecraft.client.data.models` system — the old Forge DSL
(`net.minecraftforge.client.model.generators`) does not exist.

- `ModelProvider.ItemInfoCollector`/`BlockStateGeneratorCollector`/`SimpleModelCollector` are
  `public static` nested classes, constructible directly with mod-scoped suppliers
- `BlockModelGenerators.fullBlock()` is on `BlockFamilyProvider` (instance method):
  ```java
  blockModels.new BlockFamilyProvider(TextureMapping.cube(block))
      .fullBlock(block, ModelTemplates.CUBE_ALL);
  ```
- `generateFlatItem` is `protected` — subclass `ItemModelGenerators` and override `run()`
- Block-item model (points at block model):
  ```java
  itemModels.accept(block.asItem(),
      ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(block)));
  ```
- Don't call `ItemInfoCollector.generateDefaultBlockModels()` — it iterates the whole unscoped
  registry. Replicate manually for this mod's blocks only.
- Custom geometry (like the Weave Wellspring) stays hand-authored JSON — no template covers it.

## Recipe JSON Quirks

If hand-authoring or debugging generated recipes:

- Folder: `data/<modid>/recipe/` (singular, NOT `recipes/`)
- Key/ingredient values: plain strings (`"minecraft:stick"`), not `{"item": "..."}` objects
- Shaped empty slots: literal space `" "`, not `.`
- Smelting cooking-time: `"cookingtime"` (no underscore)

## Running Datagen

```bash
JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot" ./gradlew runData --console=plain
```

Run after any provider class change. Output goes to `src/generated/resources/`. Safe to re-run
anytime — hash cache makes it a no-op for unchanged providers.
