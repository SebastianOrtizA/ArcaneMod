# Verified API Quirks — MC 26.2 / Forge 65.1.3

All confirmed by reading the actual jars. Don't second-guess these without re-checking the source.

## 1. Renamed / Restructured Types

- **`ResourceLocation` -> `Identifier`** (`net.minecraft.resources.Identifier`)
- **`EntityType` vs `EntityTypes`**: two separate classes. `EntityType` is the generic type class;
  `EntityTypes` is the constants holder (e.g., `EntityTypes.ZOMBIE`). `EntityType.ZOMBIE` does NOT
  exist. Check this split pattern on other registries too.
- **`ChunkPos` is a record**: use `.x()` / `.z()`, not `.x` / `.z`. Has no constructor taking
  `BlockPos` — only `ChunkPos(int x, int z)`. To get the chunk for an entity, call
  `entity.chunkPosition()` (returns `ChunkPos` directly).
- **`Level.random` is `protected`**: use `Level#getRandom()` for external callers.

## 2. Registration & Construction

- **`BlockEntityType` has no builder**: construct directly with
  `new BlockEntityType<>(factory, Set.of(validBlocks...))`.
- **`EntityType#create(Level, EntitySpawnReason)` does NOT call `Mob#finalizeSpawn()`**: must call
  explicitly before `addFreshEntity` to get natural-spawn defaults (skeleton bow, zombie armor,
  etc.):
  `mob.finalizeSpawn(levelAccessor, level.getCurrentDifficultyAt(pos), spawnReason, null)`.
- **`MobSpawnEvent.FinalizeSpawn`** (Forge event) is the hook for mob variant swapping at
  natural-spawn time. `event.setSpawnCancelled(true)` stops the original; spawn replacement at
  same position (remember `finalizeSpawn` for the replacement too).

## 3. Capabilities vs Data Components

- **Classic Forge capabilities** (`Capability`/`CapabilityToken`/`LazyOptional`/
  `@AutoRegisterCapability`) for chunks, entities, block entities.
- **Except `ItemStack`**: caps do NOT persist or sync (Forge's `ItemStack.java.patch` re-gathers
  them each time). Use **`DataComponentType`** (`Registries.DATA_COMPONENT_TYPE`) for per-item
  state that needs save/reload or client sync.
- **Chunk capabilities auto-persist**: `LevelChunk.java.patch` wires `writeCapsToNBT`/
  `readCapsFromNBT` into chunk save/load. No manual `SavedData` needed.
- **Player capability + `PlayerEvent.Clone`**: the capability's `LazyOptional` must NEVER be
  invalidated on entity removal for players (skip `event.addListener(data::invalidate)` in the
  `AttachCapabilitiesEvent.Entities` handler for players). In `Clone`:
  `original.reviveCaps(); try { /* read old data */ } finally { original.invalidateCaps(); }`.

## 4. BlockEntity Save/Load

- Uses **`ValueOutput`/`ValueInput`**, not raw `CompoundTag`:
  `saveAdditional(ValueOutput)` / `loadAdditional(ValueInput)`.
- Convenience getters: `getIntOr`/`getFloatOr`/`getBooleanOr` (defaulted, no Optional unwrapping).
- `getUpdateTag(HolderLookup.Provider)` returns `CompoundTag` — easiest correct impl:
  `return saveCustomOnly(registries);`.
- `getUpdatePacket()` -> `ClientboundBlockEntityDataPacket.create(this)`.
- Forge's `IForgeBlockEntity` default `handleUpdateTag` calls `loadWithComponents` -> your
  `loadAdditional` — no override needed.

## 5. Recipe & Data JSON

- Recipe folder: **`data/<modid>/recipe/`** — singular, NOT `recipes/`.
- Recipe key/ingredient values: **plain strings** (`"minecraft:stick"`), not `{"item": "..."}`
  objects.
- Shaped recipe empty slots: literal space `" "`, not `.`.
- Smelting cooking-time field: `"cookingtime"` (no underscore).
- **Forge tag files** support a `remove` field (same syntax as `values`, including `"#tag"`
  references) — only in Forge's patch, not vanilla's `TagFile.java`.
- Biome modifier `"biomes"` field: accepts one tag string OR a list of concrete biome IDs — NOT a
  list of multiple `"#tag"` strings.

## 6. GUI & Rendering

- Colors are **full ARGB** (8 hex digits) — `0xFFFFFF` is invisible (alpha = 0).
- **`fillGradient`** interpolates vertically only — use alpha-gradient textures for radial effects.
  Blit via `gg.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, srcW, srcH, texW,
  texH, tintARGB)`.
- **No diagonal-line primitive** in `GuiGraphicsExtractor` — only `fill`/`horizontalLine`/
  `verticalLine`/`outline`. Step-interpolate + `fill()` small squares for diagonal lines.
- **Layout from `this.width`/`this.height`/font metrics**, not fixed pixel constants. GUI scale
  varies the logical screen size significantly. Compute in `init()`, clamp to min/max.
- **Don't rebuild widgets every tick** — recreating a `Button` with `Tooltip` 20x/sec resets the
  hover timer. Snapshot triggering data and rebuild only when it changes.
- **Mouse click**: `mouseClicked(MouseButtonEvent event, boolean doubleClick)`, not the classic
  `(double, double, int)`. Use `event.x()`/`event.y()`/`event.button()`.

## 7. Events

- **`Cancellable` is a bare marker interface** — no `setCanceled()` method. Cancel by declaring
  the `@SubscribeEvent` handler's return type as `boolean` instead of `void` — `true` cancels,
  `false` does not. Needed for `ViewportEvent.RenderFog` and similar.
- **`GatherDataEvent` is a mod-bus event** (`IModBusEvent`) — wire manually in mod constructor
  (`GatherDataEvent.getBus(modBusGroup).addListener(...)`), not via `@Mod.EventBusSubscriber`.
- **`TagsUpdatedEvent` is dead server-side**: the call in `RecipeManager.java.patch` is commented
  out. Don't build hooks around it without checking the patch file.
- **`ViewportEvent.ComputeFogColor`** only changes fog color, not distance. Pair with
  `ViewportEvent.RenderFog` (`scaleNearPlaneDistance`/`scaleFarPlaneDistance`, needs boolean-cancel)
  for atmospheric effects.

## 8. Tags & Components Binding Timing

- **Tags not bound during datagen** (`GatherDataEvent`/`DataProvider`) — `Holder#is(TagKey)` throws
  `IllegalStateException: Tags not bound`. Tag-dependent logic must run at real game time.
- **Safe tag check location**: `SimplePreparableReloadListener`'s `apply()` — appended after
  Minecraft's own listeners (tag binding included) via `AddReloadListenerEvent`.
- **`Item#components()` throws even later** — `NullPointerException: Components not bound yet` even
  inside reload listener `apply()`. No known safe hook short of `ServerAboutToStartEvent`. Simplest
  fix: hand-list what you need instead of deriving from component data (see `VanillaFacetOverrides`).

## 9. Client/Server Dist Separation

- **Client-only code must live in its own class** — never inline into Block/Item/Entity methods.
- JVM bytecode verification resolves referenced types when the **containing class** loads, not when
  the method runs. An `if (level.isClientSide())` guard does NOT save you.
- Split into a dedicated class (see `ClientCodexOpener`, `ParticleEvents`) loaded only on client.
- **`./gradlew runServer` (dedicated server)** is the only reliable way to catch these bugs — they
  don't surface in singleplayer (integrated server runs in CLIENT dist).

## 10. Item Use & Interaction

- **Hold-to-use** (channeling, bow): override `use()` to call `player.startUsingItem(hand)` +
  return `CONSUME`. Set `getUseDuration`/`getUseAnimation` (`ItemUseAnimation`: `BOW`, `SPYGLASS`,
  `SPEAR`, etc.). `onUseTick(Level, LivingEntity, ItemStack, int remainingTicks)` fires
  automatically. Call `player.stopUsingItem()` to end early. For indefinite channels, return huge
  `getUseDuration` (72000) and rely on per-tick check + `stopUsingItem()`.
- **Right-click fall-through**: clicking a block with an item that neither block interaction nor
  `useOn` consumes falls through to the item's `use()` — verified in `Minecraft#startUseItem()`.
- **Block screen opening**: `useWithoutItem(BlockState, Level, BlockPos, Player, BlockHitResult)`
  fires on both sides. Use `if (isClientSide()) ClientXOpener.open()` — no
  `AbstractContainerMenu`/`MenuType` needed unless the screen has server-synced item slots.

## 11. Worldgen

- **`MOTION_BLOCKING` counts leaves** — use `MOTION_BLOCKING_NO_LEAVES` for forest floor.
- **Decoration step ordering matters** — `vegetal_decoration` runs after/alongside trees. Use
  `surface_structures` for features needing undisturbed terrain.
- **`rarity_filter`** is a single roll per chunk (at-most-one guarantee). `count`-style placement
  is for multiple attempts per chunk (like trees/flowers).

## 12. Network

- Clientbound: `CHANNEL.play().clientbound().addMain(Class, StreamCodec, handler)`.
- Serverbound: `CHANNEL.play().serverbound().addMain(Class, StreamCodec, handler)`.
  Send with `CHANNEL.send(msg, PacketDistributor.SERVER.noArg())`.
- Handler gets sender via `CustomPayloadEvent.Context#getSender()` (nullable on client side).

## 13. Particle Registration

- Register `SimpleParticleType` via `DeferredRegister<ParticleType<?>>` (common, both dists).
- Client-only: `@Mod.EventBusSubscriber(dist = Dist.CLIENT)` class subscribing to
  `RegisterParticleProvidersEvent.registerSpriteSet(type, provider)`.
- Needs `assets/<modid>/particles/<name>.json` (`{"textures": ["<modid>:<name>"]}`) and the PNG
  at `assets/<modid>/textures/particle/<name>.png`.

## 14. Data Loading & Resource Listeners

- **Standalone client never loads `data/<modid>/...` JSON** — only `assets/`. Mirror needed data to
  client via sync packets sent on login.
- **`SimpleJsonResourceReloadListener<T>`** has protected two-arg constructor
  `(Codec<T>, FileToIdConverter)` — extend directly when file id = file location (the common case).
  Use the heavier `SimplePreparableReloadListener` + `scanDirectory` pattern only when you need
  extra logic in `apply()`.
- **Datagen provider registration**: `getVanillaPack(boolean toRun)` returns `PackGenerator` whose
  `addProvider` takes `DataProvider.Factory<T>` (`T create(PackOutput)`), not a raw instance.

## 15. Model Datagen System

- **Vanilla's `net.minecraft.client.data.models`** replaced Forge's old
  `net.minecraftforge.client.model.generators` DSL entirely — that package doesn't exist.
- `ModelProvider.ItemInfoCollector`/`BlockStateGeneratorCollector`/`SimpleModelCollector` are
  `public static` nested classes constructible directly with mod-scoped suppliers.
- `BlockModelGenerators.fullBlock(Block, ModelTemplate)` is on `BlockFamilyProvider` (instance
  method): `blockModels.new BlockFamilyProvider(TextureMapping.cube(block)).fullBlock(block,
  ModelTemplates.CUBE_ALL)`.
- `ItemModelGenerators.generateFlatItem(Item, ModelTemplate)` is `protected` — subclass and
  override `run()`.
- `ItemInfoCollector.generateDefaultBlockModels()` iterates the WHOLE registry — replicate the
  one-liner manually, scoped: `itemModels.accept(block.asItem(), ItemModelUtils.plainModel(
  ModelLocationUtils.getModelLocation(block)))`.
- Custom geometry (Weave Wellspring pedestal) stays hand-authored JSON — no template covers it.

## 16. Datagen Provider Scoping (CRITICAL)

- `BlockLootSubProvider`/`EntityLootSubProvider`/`ModelProvider` all validate against the WHOLE
  shared registry by default.
- Override `getKnownBlocks()` / `getKnownEntityTypes()` to return only this mod's content.
- These overrides are **only visible in the `.java.patch` files** in the Forge sources jar, not
  the plain decompiled class.
- Without scoping: `Missing loottable 'minecraft:blocks/stone'` errors.
