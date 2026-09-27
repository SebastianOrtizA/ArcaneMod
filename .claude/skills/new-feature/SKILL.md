---
name: new-feature
description: >
  Load this skill when implementing a new block, item, entity, particle,
  capability, network packet, GUI screen, or any other game element from
  scratch. Provides the step-by-step workflow tying together registration,
  datagen, textures, and validation. Load when: the task involves adding
  something new to the mod, implementing a plan/stage item, or when you see
  "add", "new", "implement", "create" combined with a game element noun
  (block, item, entity, mob, particle, capability, packet, screen, GUI).
  Always load BEFORE starting implementation — this is the entry point
  workflow that references the other project skills.
---

# New Feature Workflow

This skill is the orchestrator — it tells you *what* to do in order. For the *how*, it references
the other project skills (`api-research`, `datagen`, `build-validate`, `placeholder-texture`).

## Pre-Implementation

1. **Read the plan**: check the relevant `plans/stage-N-*.md` for design context and requirements
2. **Load `api-research`**: verify any MC/Forge APIs you'll be using against the actual jars
3. **Find the closest existing pattern**: look at how similar features are already implemented in
   this codebase (e.g., to add a new item, look at `WandItem` or `ResonometerItem`)

## New Block

1. **Block class** in `com.sebas.arcanemod.block/`
   - Extend `Block` or appropriate subclass
   - Override behavior methods as needed

2. **Register in `ModBlocks`**
   - `DeferredRegister`/`RegistryObject` pattern
   - Include `.setId(BLOCKS.key("name"))` in `Properties`

3. **BlockItem in `ModItems`**
   - `.setId(ITEMS.key("name"))` and `.useBlockDescriptionPrefix()`

4. **Block entity** (if needed)
   - Class with `saveAdditional(ValueOutput)` / `loadAdditional(ValueInput)`
   - Register in `ModBlockEntities`: `new BlockEntityType<>(factory, Set.of(block))`
   - Sync: override `getUpdateTag` + `getUpdatePacket` (see api-quirks section 4)

5. **Creative tab**: add to `ModCreativeTabs.ARCANE_TAB` `displayItems`

6. **Datagen**: load `datagen` skill, add to all relevant providers (loot, model, lang, tags)

7. **Texture**: load `placeholder-texture` skill, create 16x16 PNG

8. **Validate**: load `build-validate` skill — compile -> datagen -> server boot -> check logs

## New Item

1. **Item class** in `com.sebas.arcanemod.item/`
   - Extend `Item` or appropriate subclass

2. **Register in `ModItems`**
   - `.setId(ITEMS.key("name"))`, `.stacksTo(N)` if needed

3. **Per-item state** (if needed)
   - Use `DataComponentType` (NOT capability — ItemStack caps don't persist)
   - Register in `ModDataComponents`
   - `.persistent(codec).networkSynchronized(streamCodec).build()`

4. **Creative tab**: add to `ModCreativeTabs.ARCANE_TAB`

5. **Datagen**: load `datagen` skill (model, lang, recipe)

6. **Texture**: load `placeholder-texture` skill, 16x16 PNG

7. **Validate**: load `build-validate` skill

## New Entity

1. **Entity class** in `com.sebas.arcanemod.entity/`

2. **Register in `ModEntityTypes`**
   - `EntityType.Builder.of(Ctor::new, MobCategory).sized(w, h).build(ENTITY_TYPES.key("name"))`

3. **Client-only renderer** — MUST be in its own class (dist separation)
   - `@Mod.EventBusSubscriber(value = Dist.CLIENT)` or registered separately
   - Never inline client rendering into the entity class

4. **`finalizeSpawn`** — if the entity needs natural-spawn equipment, call explicitly:
   ```java
   mob.finalizeSpawn(levelAccessor, level.getCurrentDifficultyAt(pos), spawnReason, null);
   ```
   `EntityType#create()` does NOT call this.

5. **Datagen**: load `datagen` skill (loot, lang)

6. **Texture**: load `placeholder-texture` skill (match vanilla counterpart dimensions)

7. **Validate**: load `build-validate` skill

## New Particle

1. **Register `SimpleParticleType`** in `ModParticles` (common, both dists — needs registry ID
   for network sync)

2. **Client-only sprite registration** in `ParticleEvents`
   - `@Mod.EventBusSubscriber(dist = Dist.CLIENT)` class
   - Subscribe to `RegisterParticleProvidersEvent`
   - `.registerSpriteSet(type, provider)` — vanilla's `SpellParticle.Provider` works for
     swirling-glow effects

3. **Particle JSON**: `assets/arcanemod/particles/<name>.json`
   ```json
   {"textures": ["arcanemod:<name>"]}
   ```

4. **Texture**: load `placeholder-texture` skill, 8x8 PNG at `textures/particle/<name>.png`

5. **Validate**: load `build-validate` skill

## New Capability

1. **Interface** (e.g., `IMyData`) + **implementation class**

2. **Capabilities holder class**
   - `CapabilityManager.get(new CapabilityToken<>() {})`

3. **`@AutoRegisterCapability`** on the interface

4. **Event handler** for `AttachCapabilitiesEvent`
   - `.LevelChunks` for chunk capabilities
   - `.Entities` for entity/player capabilities

5. **Persistence rules**:
   - **Chunk caps**: wire `event.addListener(data::invalidate)` — auto-persists via
     `writeCapsToNBT`/`readCapsFromNBT`
   - **Player caps**: do NOT wire invalidate listener. Add `PlayerEvent.Clone` handler:
     ```java
     original.reviveCaps();
     try { /* read old data */ }
     finally { original.invalidateCaps(); }
     ```
   - Add login sync via `PlayerEvent.PlayerLoggedInEvent`

6. **Sync packet** (if client needs the data): see New Network Packet below

## New Network Packet

1. **Packet class** (record or class) with:
   - `public static final StreamCodec<...> STREAM_CODEC = ...`
   - Handler method

2. **Register in `ModNetwork.register()`**:
   - Clientbound: `.play().clientbound().addMain(Class, StreamCodec, handler)`
   - Serverbound: `.play().serverbound().addMain(Class, StreamCodec, handler)`

3. **Sending**:
   - To specific player: `CHANNEL.send(msg, PacketDistributor.PLAYER.with(player))`
   - Client -> server: `CHANNEL.send(msg, PacketDistributor.SERVER.noArg())`

4. **Handler**: `context.getSender()` returns the sender (nullable on client side)

## New GUI Screen

1. **Screen class** in `client/gui/` (client-only package)
   - Compute layout from `this.width`/`this.height`/font metrics in `init()`, not fixed pixels
   - Remember ARGB colors (alpha byte matters — `0xFFFFFF` is invisible)
   - Mouse click: `mouseClicked(MouseButtonEvent event, boolean doubleClick)`
   - Don't rebuild widgets every tick (breaks tooltip hover timers)

2. **`ClientXOpener` class** in `client/` to isolate the `new Screen()` call
   - This is the dist-separation pattern — the opener class is only loaded on client

3. **Trigger from Item/Block**:
   ```java
   if (level.isClientSide()) ClientXOpener.open();
   ```

4. No `AbstractContainerMenu`/`MenuType` needed unless the screen has server-synced item slots

## Dist Separation Rules (Apply to All Features)

- Client-only code MUST live in its own class — never inline into Block/Item/Entity methods
- JVM bytecode verification loads referenced types when the **containing class** loads, not when
  the method runs — `if (isClientSide())` does NOT save you
- Use `@Mod.EventBusSubscriber(value = Dist.CLIENT)` for client-only event subscribers
- **Test with `./gradlew runServer`** (dedicated server) — the only reliable way to catch these
  bugs (singleplayer's integrated server runs in CLIENT dist)

## Post-Implementation

Load `build-validate` skill and run the full cycle:
1. `compileJava` — fix any compile errors
2. `runData` — if any datagen providers were changed
3. `runServer` — dedicated server boot to catch dist/registry/worldgen issues
4. Check `run/logs/latest.log` for errors
