# Stage 0 — Core Systems
## Codex Arcanum · Developer Implementation Plan

**Priority:** This stage must be completed before ALL other stages. Every mechanic in the mod depends on these systems.

**Mod framework:** Minecraft Java Edition mod (Forge or NeoForge — decide before starting). Use Mojang mappings.

---

## Phase 0.A Status: COMPLETE (Weave density, Wyrdstone, Weave Wellspring)

Implemented and tuned through an iterative session. Project-wide conventions (mod ID, package,
verified API quirks, workflow) live in `CLAUDE.md` at the repo root, not here — this section only
covers Phase 0.A's actual design decisions and current tuning.

Phases 0.B (Facets & Resonometer), 0.C (Fray), and 0.D (Loom of Understanding) are also complete —
see their own Status sections below. Only datagen (0.9) remains unstarted; see "Deferred" at the
end of the 0.D section.

### Deviations from the plan as originally written
- **Mod ID stayed `arcanemod`** (package `com.sebas.arcanemod`), not `codexarcanum` — the
  existing pre-Stage-0 code (wand, Codex item, lectern ritual) already used it.
- **Wellspring capacity is randomized per-instance, not a flat 500.** Each Wellspring rolls its
  own max once, lazily, on its first tick: `200 + random(0, round(baseDensity * 600))`. A
  Wellspring in a dense biome can end up meaningfully stronger than one in a sparse one, but
  it's a roll, not a guarantee. See `WeaveWellspringBlockEntity.rollMaxWyrd`.
- **No networking packets were needed for this phase.** Wand Wyrd rides a data component
  (`ModDataComponents.STORED_WYRD`) which persists and syncs automatically; the Wellspring's
  charge rides `getUpdateTag`/`getUpdatePacket`. `network/` package is still empty — first
  needed in Phase 0.B (Resonometer scan results).
- **Wellspring worldgen placement uses `surface_structures`, not `vegetal_decoration`.** Trees
  are placed in `vegetal_decoration`; being in the same step meant our feature ran after trees
  and could place a Wellspring on top of a tree trunk (hidden in/above canopy). Also uses
  `MOTION_BLOCKING_NO_LEAVES` as the heightmap type, not `MOTION_BLOCKING` (which treats leaves
  as ground).
- **Rarity is one `rarity_filter` roll per chunk, not vanilla's multi-attempt `count` style**
  (how trees/flowers get several tries per chunk). Deliberately kept to one roll to guarantee at
  most one natural Wellspring per chunk — see the tuning table below for why "as common as
  trees" isn't achievable under this approach without dropping that guarantee.
- **Biome density tiers are data-driven via three custom tags**, not a single blanket
  `#minecraft:is_overworld` target: `arcanemod:high_density_biomes` (mushroom/forest/jungle),
  `arcanemod:low_density_biomes` (desert/plains/badlands/ocean/deep ocean), and
  `arcanemod:normal_density_biomes` (everything else, built using Forge's tag `"remove"` field —
  `values: [#minecraft:is_overworld]` minus the other two tags — so modded biomes fall into it
  automatically rather than needing manual enumeration).

### Current tuning (all easily re-tunable constants, expect to revisit before Stage 0 ships)

| What | Value | Where |
|---|---|---|
| Weave density regen | `(base-current) * 0.01`, every 200 ticks; halved above 50 regional Fray, zero above 80 | `WeaveEvents.RECOVERY_FRACTION` |
| Wellspring base production rate | `2.0` Wyrd/tick at density 1.0, ancient, no interference | `WeaveWellspringBlockEntity.BASE_RATE` |
| Wellspring capacity | `200` floor + `random(0, round(density * 600))` | `WeaveWellspringBlockEntity.MIN_CAPACITY` / `CAPACITY_DENSITY_BONUS` |
| Min density to produce | `0.1` (feedback-loop cutoff from the design doc) | `MIN_DENSITY_TO_PRODUCE` |
| Proximity interference | `0.85^(otherWellsprings)`, rescanned every 100 ticks | `INTERFERENCE_FACTOR` |
| Wand channel rate | `1` Wyrd/tick (cobblestone wand's 100 cap takes ~2 holds from empty) | `WYRD_PER_CHANNEL_TICK` |
| Channel duration | effectively unbounded (`72000` ticks) — ends via `stopUsingItem()` when the wand's full or the Wellspring's dry, not a timer | `WandItem.CHANNEL_DURATION_TICKS` |
| Density drain from channeling | `0.01` × Wyrd actually drawn | `DENSITY_DRAIN_PER_WYRD` |
| Wellspring rarity (high/normal/low density biomes) | `1-in-30` / `1-in-100` / `1-in-100` | `worldgen/placed_feature/weave_wellspring_placed_*.json` |

---

## Phase 0.B Status: COMPLETE (Facets & Resonometer)

Implements 0.4 (Facet System) and 0.6 (Resonometer) as specced, plus a substantially larger
data-coverage effort than the plan originally called for.

### Deviations from the plan as originally written
- **Vanilla Facet data is computed programmatically, not hand-authored per-item JSON.** The plan's
  `data/codexarcanum/facets/*.json` format still exists and still works (loaded as
  `jsonOverrides`), but authoring one file per vanilla block/item/mob was abandoned as
  impractical. Instead `core/facet/vanilla/` computes a default `FacetSignature` for **every**
  entry in `BuiltInRegistries.BLOCK`/`ITEM`/`ENTITY_TYPE` (~1780 entries total) via three layers,
  checked in order: hand-curated exact-id overrides (`VanillaFacetOverrides`, ~280 entries for
  ores/nether/end/food/mob-drops/iconic items where a generic rule wouldn't "make sense" for that
  specific item), then vanilla tag-based rules (`VanillaBlockFacets`/`VanillaItemFacets`/
  `VanillaEntityFacets`, e.g. all `#minecraft:logs` get the same Facet lean), then a minimal
  fallback (`TERRA 1` for blocks/items, `VITA 1` for entities). `FacetRegistry` layers a mod's own
  `jsonOverrides` on top of these `vanillaDefaults`.
- **This required discovering a load-order constraint not in the original plan**: tag membership
  (`Holder#is(TagKey)`) and item default components (`Item#components()`) are *not* available from
  a standalone datagen run or from this build's (dead) `TagsUpdatedEvent` — see CLAUDE.md's
  "Tag membership … are each bound at their own, later-than-you'd-guess point" entry. The fix was
  computing all vanilla defaults inside `FacetDataLoader`'s own `AddReloadListenerEvent` reload
  listener, which is guaranteed to run after vanilla's tag-binding listener. Component-based
  values (originally planned: derive a food item's Facet amount from its `FoodProperties`
  nutrition) were dropped entirely — components bind too late even for a reload listener — in
  favor of ~30 hardcoded food overrides in `VanillaFacetOverrides`.
- **`CompoundFacet` and `PlayerFacetKnowledge` match the plan's shape exactly** (`Set<Facet>
  knownFacets`, `Set<CompoundFacet> knownCompounds`, `Set<Identifier> scannedEntries`) — no
  deviation there.
- **Resonometer targeting needed more raycast nuance than "a simple 8-block raycast"**:
  - Dropped items are their own fix: `ItemEntity` is excluded by `Entity#isPickable()` by default
    (predicate widened to `isPickable() || instanceof ItemEntity`), and separately has a
    `getPickRadius()` of `0` (worked around with an explicit `entityMargin` on the
    `ProjectileUtil.getEntityHitResult` overload used).
  - Water needed **two** `level.clip(...)` calls, one `ClipContext.Fluid.NONE` (solid-only) and
    one `ANY` (fluid-only) — picking whichever hit is closer, and discarding a fluid hit at ~0
    distance (the water at the player's own eyes while swimming) so looking *through* water at
    the seafloor still resolves to the floor, not the water surface at the camera.
  - `onUseTick` had to become forgiving of a single missed frame (a moving/tiny target flickering
    in and out of the ray for one tick used to cancel the whole channel) — it now just does
    nothing on a miss tick rather than calling `stopUsingItem()`.
- **`PlayerFacetKnowledge` persistence across death** needed the same capability
  reviveCaps/invalidateCaps + never-invalidate-own-`LazyOptional` pairing documented in CLAUDE.md
  — this pattern is shared verbatim by 0.C's `PlayerFrayData`.

### Current tuning
| What | Value | Where |
|---|---|---|
| Resonometer scan duration | 30 ticks (1.5s) | `ResonometerItem.SCAN_DURATION_TICKS` |
| Resonometer range | 8 blocks | `ResonometerItem.RANGE` |
| Entity pick margin (for tiny/dropped-item targets) | `0.3` | `ResonometerItem.ENTITY_PICK_MARGIN` |
| Vanilla Facet coverage | ~1780 defaults computed at reload-listener time | `VanillaFacetData.compute()` |

### Debug commands added
`/arcane facets <id>` (look up a registered signature), `/arcane scan <id>` (force-record a scan
without the item), `/arcane knowledge` (dump the calling player's known Facets/compounds/scans).

---

## Phase 0.C Status: COMPLETE (Fray — regional, personal, Frayed mobs, client rendering)

Implements 0.5 (Fray System) as specced, including its client-rendering sub-section.

### Deviations from the plan as originally written
- **Regional Fray's onset condition is density-based, not "per Wyrd drawn while overdrawing"**:
  the plan said "when `currentDensity` drops below 0.2, add 1 regional Fray per Wyrd drawn"; the
  implementation adds Fray equal to the Wyrd actually drawn on any channel tick that pushes
  density below `WeaveWellspringBlockEntity.FRAY_ONSET_DENSITY` (`0.2`), capped at
  `MAX_REGIONAL_FRAY` (`100`) — functionally the same rule, just phrased around "this tick's
  drain crossed the line" rather than a running per-Wyrd counter.
- **Regional Fray recovery-rate gating matches the plan's numbers exactly** (halved above 50,
  halted above 80) — implemented in `WeaveEvents.regenerate()`, no deviation.
- **Personal Fray only has one of the plan's three sources implemented**: lingering in a Frayed
  chunk (regional Fray > 50). Forbidden spells and Duskbound equipment don't exist yet (later
  stages), so there's nothing to wire those sources to — `FrayEvents` explicitly calls this out
  rather than stubbing dead code for them.
- **Frayed mob spawning replaces the natural spawn rather than modifying spawn weights**: hooks
  `MobSpawnEvent.FinalizeSpawn`, checks the spawning chunk's regional Fray against
  `FRAY_SPAWN_THRESHOLD` (50) / `FRAY_HIGH_THRESHOLD` (80) for a 50%/85% replace chance
  (`BASE_REPLACE_CHANCE`/`HIGH_REPLACE_CHANCE`), cancels the original spawn, and spawns the
  Frayed variant at the same position instead. Stat boost is +20% health / +10% attack damage
  (plan said "+20% HP, +10% damage" — matches), computed by reading the vanilla mob's own
  `AttributeSupplier` base values and scaling them, not hardcoded numbers, so a future MC bump to
  zombie health doesn't silently desync the Frayed variant.
  - Needed `EntityType#create()` followed by an explicit `Mob#finalizeSpawn(...)` call —
    `create()` alone does not hand out default equipment (a Frayed Skeleton spawned without a bow
    until this was added). Same fix applied to the `/arcane frayed <mob>` debug command.
- **Client-side rendering went through two more design passes than "fog tint + vignette"
  implies**, driven by in-game feedback:
  - Regional Fray affects both fog *color* (`ViewportEvent.ComputeFogColor`, tinted toward a
    near-black violet) **and** fog *distance* (`ViewportEvent.RenderFog`, pulling the near/far
    planes in to as little as 25% of normal at full severity) — color alone read as "a hazy
    horizon," not an oppressive nearby effect.
  - Personal Fray's vignette is a pre-baked radial-alpha PNG blitted full-screen and tinted, not
    `fillGradient`-drawn edge bands — `GuiGraphicsExtractor#fillGradient` only interpolates along
    a fixed vertical axis regardless of the target rectangle's shape, so tall/thin left/right edge
    bands rendered as two solid-color bars instead of a fade.
  - Regional Fray's effect on rendering bleeds smoothly into neighboring chunks rather than
    cutting off at the exact chunk border: `FrayEvents.effectiveRegionalFrayForRendering` scans a
    chunk neighborhood (radius `RENDER_BLEED_RADIUS_CHUNKS`, `3`), attenuating each neighbor's own
    Fray value linearly by chunk distance (full strength at 0, zero at the radius) and taking the
    max across the neighborhood — this is a rendering-only concept; personal Fray accumulation
    still only checks the exact chunk the player stands in.
- **Discovered this build's `Cancellable` mechanism differs from every other event handler in the
  project**: `ViewportEvent.RenderFog` requires cancellation to apply its changes, but this
  eventbus version has no generic cancel method on `Cancellable`/`MutableEvent` — a
  `@SubscribeEvent`-annotated listener cancels a `Cancellable` event by returning `boolean`
  instead of `void` (`true` = cancel/apply). See CLAUDE.md for the full citation trail.

### Current tuning
| What | Value | Where |
|---|---|---|
| Regional Fray onset density | below `0.2` density on a channel tick | `WeaveWellspringBlockEntity.FRAY_ONSET_DENSITY` |
| Regional Fray cap | `100` | `WeaveWellspringBlockEntity.MAX_REGIONAL_FRAY` |
| Regen halved / halted thresholds | `> 50` / `> 80` regional Fray | `WeaveEvents.regenerate()` |
| Personal Fray linger threshold / cadence | regional Fray `> 50`, checked every 200 ticks | `FrayEvents.LINGER_FRAY_THRESHOLD`/`LINGER_CHECK_INTERVAL_TICKS` |
| Personal Fray decay | `-1` every 6000 ticks (5 min) | `FrayEvents.DECAY_INTERVAL_TICKS` |
| Personal Fray cap | `100` | `FrayEvents.MAX_PERSONAL_FRAY` |
| Frayed mob spawn threshold / high threshold | regional Fray `> 50` / `> 80` | `FrayedMobEvents.FRAY_SPAWN_THRESHOLD`/`FRAY_HIGH_THRESHOLD` |
| Frayed mob replace chance (normal / high Fray) | `50%` / `85%` | `FrayedMobEvents.BASE_REPLACE_CHANCE`/`HIGH_REPLACE_CHANCE` |
| Frayed mob stat boost | `+20%` health, `+10%` attack damage | `FrayedMobEvents.HEALTH_MULTIPLIER`/`DAMAGE_MULTIPLIER` |
| Fog tint threshold / max blend | regional Fray `> 20`, up to `90%` blend at 100 | `FrayRenderEvents.REGIONAL_FRAY_THRESHOLD`/`MAX_FOG_COLOR_BLEND` |
| Fog distance pull-in at full severity | to `25%` of normal | `FrayRenderEvents.MIN_FOG_DISTANCE_FACTOR` |
| Vignette thresholds (low / high) | personal Fray `20` / `50` | `FrayRenderEvents.VIGNETTE_LOW_THRESHOLD`/`VIGNETTE_HIGH_THRESHOLD` |
| Regional Fray render-bleed radius | `3` chunks, linear falloff to `0` | `FrayEvents.RENDER_BLEED_RADIUS_CHUNKS` |

### Debug commands added
`/arcane fray` (report personal Fray), `/arcane fray add <amount>`, `/arcane frayed
<zombie\|skeleton\|spider>` (force-spawn a Frayed variant at the player, with correct
`finalizeSpawn` equipment).

### Deferred to later work (not started)
- **0.9 Datagen** — everything so far (0.A/0.B/0.C/0.D) is hand-written JSON. Worth converting
  once there's enough content that hand-authoring hurts.
- Personal Fray's other two sources (Forbidden spells, Duskbound equipment) — no-ops until those
  systems exist in later stages.

---

## Phase 0.D Status: COMPLETE (Loom of Understanding — research tree, puzzle, research-driven Codex)

Implements 0.7 (Loom of Understanding) as specced, across four checkpoints, plus the Codex rework
0.7 explicitly calls out as a later step.

### Deviations from the plan as originally written
- **A node's own id is derived from its JSON file's location, not a duplicated `"id"` field
  inside it** — the plan's own example JSON (§0.7) includes `"id": "sensing_the_weave"` alongside
  the file; dropped as redundant (same reasoning as `CompoundFacet`'s design, just applied one
  step further). `ResearchDataLoader` extends `SimpleJsonResourceReloadListener<ResearchNode>`
  directly (`FileToIdConverter.json("research")`) rather than needing `FacetDataLoader`'s more
  involved custom-scan approach — there's no vanilla-coverage computation here that would need
  tag binding, so the simpler base class works unmodified.
- **Most of Section 1's six nodes complete from a real gameplay trigger, not the Loom's puzzle at
  all** — `awakening` (free, granted on login), `wyrdstone_refining`/`sensing_the_weave`/
  `a_crude_focus` (`PlayerEvent.ItemCraftedEvent`/`ItemSmeltedEvent` for the matching item),
  `the_first_wellspring` (a successful Wyrd transfer in `WandItem#onUseTick`), `first_threads`
  (crafting the Loom itself). This reads as a deliberate simplification of "guided, little to no
  puzzle ambiguity" (design doc §2.3) rather than routing genuinely trigger-based unlocks through
  a Facet-connecting puzzle that doesn't fit their fictional framing (there's no natural pattern
  for "you smelted Wyrdstone"). A node's own `pattern` field being empty is what the Loom's list
  screen reads as "complete on click, no puzzle" versus "open the puzzle board first" — this
  routing rule falls directly out of the data, not a hardcoded per-node exception.
- **Added a 7th Section 1 node, `threadwork`** (connect Ignis + Terra), specifically to exercise
  the puzzle mechanic in-game — none of the design doc's six canonical Section 1 nodes actually
  involve connecting Facets, so without it nothing would ever open `LoomPuzzleScreen`. Gated
  behind `first_threads` (needs the Loom itself to attempt). Worth revisiting once real Section
  2+ puzzle content exists — either keep it as a legitimate "practice the mechanic" tutorial node
  or cut it.
- **No `AbstractContainerMenu`/`MenuType` for the Loom's screens** — deliberately skipped the
  vanilla container/menu system entirely. Neither screen needs server-synced item slots: the
  research tree and a player's completed set are already synced independently (see Networking
  below), so opening the screen needs no server round trip at all. `LoomOfUnderstandingBlock`
  overrides `useWithoutItem` and opens the screen with `if (level.isClientSide()) ClientLoomOpener
  .open();` — the same dedicated-client-opener-class pattern `CodexArcanumItem`/
  `ClientCodexOpener` already established for item use, just from a block interaction instead
  (`useWithoutItem` fires symmetrically on both logical sides, exactly like `Item#use`).
- **The puzzle doesn't validate itself server-side** — `LoomPuzzleScreen` compares the player's
  drawn connections against the node's target pattern entirely client-side; solving it just sends
  the same `RequestResearchPacket` the plain list's button sends, and the server only re-checks
  prerequisites, the same as it would for a no-puzzle node. Treated as a single-player-facing
  research minigame, not something worth a synced-puzzle-state anti-cheat protocol.
- **Codex pages are ordered by prerequisite-chain depth, not an explicit order field** —
  `CodexResearchPages.depthOf` recursively measures the longest prerequisite chain (memoized,
  cycle-guarded) to reproduce the design doc's intended "story order" per section without adding
  data just for that.

### Networking
| Packet | Direction | Purpose |
|---|---|---|
| `SyncResearchPacket` | S→C | A player's completed research ids — login and every new completion |
| `SyncResearchTreePacket` | S→C | The whole research tree's metadata (login only) — a standalone client never loads `data/` JSON, so the Loom/Codex need every node's name/description/prerequisites/pattern mirrored, not just which ones are done |
| `RequestResearchPacket` | C→S | The Loom's first client -> server packet: "the player wants to complete this node" (list click or solved puzzle) — server re-checks prerequisites before completing |

### Debug commands added
`/arcane loom` (place one at the player), `/arcane research` (list every node with `[done]`/
`[available]`/`[locked]` against the caller's progress), `/arcane research complete <id>`,
`/arcane research forget <id>` (un-completes a node — the only way to re-open/re-test a puzzle
without a fresh player).

### Deferred to later work (not started)
- **0.9 Datagen** — see 0.C's identical note; now also covers the Loom's hand-written block/item
  assets and the `research/` JSON.
- Recipe diagrams / item-block info on Codex pages (design doc: "Pages display: lore text, recipe
  diagrams, item/block info") — pages are plain text for now; only lore text is wired up.
- Sections 2-7 have zero research nodes (that's Stage 1+ content) — their Codex tabs render a
  single "Not Yet Written" placeholder page until later stages add nodes for them.
- Difficulty 2+ puzzle behavior (partial/no hints) is implemented in `LoomPuzzleScreen` (hints
  only show at difficulty <= 1) but untested — no node past difficulty 1 exists yet to exercise it.

---

## 0.1 Project Scaffold

### Tasks
- [ ] Initialize the mod project with the chosen mod loader (Forge/NeoForge)
- [ ] Set up the mod ID: `codexarcanum`
- [ ] Set up the package structure:
  ```
  com.codexarcanum
  ├── core/          # Core systems (this stage)
  │   ├── wyrd/
  │   ├── facet/
  │   ├── fray/
  │   └── weave/
  ├── block/
  ├── item/
  ├── entity/
  ├── gui/
  ├── worldgen/
  ├── data/          # Data generators
  ├── network/       # Packet handling
  └── client/        # Client-side rendering
  ```
- [ ] Register the mod's creative tab
- [ ] Set up deferred registers for blocks, items, entities, block entities, menus, sounds
- [ ] Set up networking channel for client-server sync packets

---

## 0.2 Weave Density System

### What it does
Every chunk has an invisible floating-point `weaveDensity` value (0.0–1.0) that determines Wyrd regeneration speed and ritual/alchemy effectiveness in that area.

### Data storage
- [ ] Create a `ChunkWeaveData` capability (or saved data attachment) that persists per-chunk:
  - `float weaveDensity` — base density, set by biome at chunk gen, never changes
  - `float currentDensity` — runtime density, reduced by Wyrd overdraw, recovers toward base over time
- [ ] Attach to chunks via `AttachCapabilitiesEvent<LevelChunk>` (Forge) or data attachments (NeoForge)
- [ ] Serialize to NBT so it persists across saves

### Biome density mapping
- [ ] Create a config-driven map of biome → base density:
  - Forest biomes: 0.7–0.8
  - Mushroom fields: 0.9
  - Plains/desert: 0.3–0.4
  - Ocean: 0.2
  - Near Loomhold structures (computed at gen-time): +0.2 bonus, capped at 1.0
- [ ] Default density for unregistered biomes: 0.5

### Regeneration tick
- [ ] Every N ticks (configurable, suggest 200 = 10 seconds), each loaded chunk's `currentDensity` ticks toward `baseDensity`:
  - Recovery rate: `(baseDensity - currentDensity) * 0.01` per tick — slow, asymptotic
  - If regional Fray > 50 in this chunk, recovery rate is halved
  - If regional Fray > 80, recovery is zero

### Networking
- [ ] Sync `currentDensity` to nearby clients when it changes significantly (>0.05 delta) — clients need it for visual overlays later

---

## 0.3 Wyrd — Energy Resource

### What it does
Wyrd is the mod's mana. Stored in items (wands, batteries), drawn from Wellsprings.

### Data model
- [ ] Create an `IWyrdStorage` interface:
  ```java
  int getWyrd();
  int getMaxWyrd();
  int receiveWyrd(int amount, boolean simulate);
  int extractWyrd(int amount, boolean simulate);
  ```
- [ ] Implement `WyrdStorage` (default impl) backed by NBT — attach to ItemStacks via capability/data component

### Weave Wellspring Block
- [ ] Register block: `weave_wellspring`
- [ ] Block entity: `WeaveWellspringBlockEntity`
  - Stores: `int storedWyrd`, `int maxWyrd` (default: 500), `boolean isAncient` (true for worldgen, false for player-built)
  - Tick logic:
    - Read chunk's `currentDensity`
    - Accumulate Wyrd: `wyrdPerTick = density * (isAncient ? 1.0 : 0.6) * baseRate`
    - Cap at `maxWyrd`
  - Proximity interference: check for other Wellsprings in same chunk + adjacent chunks, apply `0.85^(count-1)` multiplier
- [ ] Particle rendering: spawn spiral particles above the block when `storedWyrd > 0`; particle density proportional to `storedWyrd / maxWyrd`
- [ ] Right-click interaction (with a wand): start a 3-second channel (hold right-click):
  - Transfer Wyrd from Wellspring to wand's `IWyrdStorage`
  - Drain chunk's `currentDensity` slightly per Wyrd drawn: `densityDrain = wyrdDrawn * 0.001`
  - Play channeling sound + particle beam from Wellspring to player
  - If `currentDensity` drops below 0.1, Wellspring produces nothing (feedback loop)
- [ ] Model: stone pedestal (custom JSON model, ~1.5 blocks tall)
- [ ] Worldgen placement: scatter in biomes with density > 0.5, denser near Loomhold structures. Use `PlacedFeature` with biome modifier

### Wyrdstone Ore
- [ ] Register block: `wyrdstone_ore` (stone variant), `deepslate_wyrdstone_ore`
- [ ] Drops: `wyrdstone` item (raw crystal) — fortune-compatible
- [ ] Worldgen: ore veins, denser near Loomhold structures, Y levels -64 to 32
  - Vein size: 4–8 blocks
  - Per chunk: 2–4 veins (normal), 6–10 veins (near Loomhold)
- [ ] Register item: `wyrdstone` (raw crystal)
- [ ] Register item: `wyrd_dust` — obtained by crafting (shapeless: 1 wyrdstone → 2 wyrd_dust) or smelting

---

## 0.4 Facet System

### What it does
Every block, item, and mob has a Facet signature — a set of tags describing its magical properties. Facets are the mod's aspect/essentia equivalent.

### Data model
- [ ] Create `Facet` enum with 12 primals:
  ```
  IGNIS, AQUA, TERRA, AER, LUX, UMBRA, VITA, MORTIS, ORDO, PERDO, MOTUS, COGNITIO
  ```
- [ ] Create `CompoundFacet` registry for paired combinations:
  - `METALLUM` = IGNIS + TERRA
  - `VICTUS` = VITA + AQUA
  - `HISTORIA` = ORDO + COGNITIO
  - `TENEBRAE` = UMBRA + MORTIS
  - (Add more as needed per section)
- [ ] Create `FacetSignature` — an immutable map of `Facet → int` (amount of each facet present)
- [ ] Create a `FacetRegistry` — a static map of `ResourceLocation → FacetSignature` that maps every vanilla block/item/mob to its facets
  - Implement via JSON data files: `data/codexarcanum/facets/*.json`
  - Format: `{ "item": "minecraft:coal", "facets": { "ignis": 2, "terra": 1 } }`
  - Load via `AddReloadListenerEvent`
- [ ] Ship default facet data for all vanilla blocks, items, and mobs (data generation task — large but straightforward)

### Player Facet Knowledge
- [ ] Track which Facets a player has discovered via `PlayerFacetKnowledge` capability:
  - `Set<ResourceLocation> scannedEntries` — which items/blocks/mobs have been scanned
  - `Set<Facet> knownFacets` — which primal facets have been encountered
  - `Set<CompoundFacet> knownCompounds` — which compound facets have been encountered
- [ ] Persist to player NBT, sync to client

---

## 0.5 Fray System

### Regional Fray
- [ ] Add to `ChunkWeaveData`:
  - `int regionalFray` (0–100)
- [ ] Fray sources (called from other systems):
  - Wyrd overdraw: when `currentDensity` drops below 0.2, add `1` regional Fray per Wyrd drawn
  - Forbidden rituals/spells: add amount specified by the ritual/spell definition (Stage 5)
- [ ] Fray effects (tick-driven):
  - 0–20: no effect
  - 21–50: trigger terrain desaturation (client-side shader/overlay — see rendering below); slow Wyrd regen by 50%
  - 51–80: halt Wyrd regen; enable Frayed mob spawning (see below)
  - 81–100: increase Frayed mob spawn rate; only clearable via Rite of Cleansing (Stage 3)
- [ ] Regional Fray does NOT decay on its own — intentional design choice

### Personal Fray
- [ ] Create `PlayerFrayData` capability:
  - `int personalFray` (0–100)
- [ ] Fray sources:
  - Casting Forbidden spells: amount per spell definition
  - Wearing Duskbound equipment: passive drain per tick while equipped
  - Lingering in Frayed chunks (regionalFray > 50): slow accumulation
- [ ] Fray effects:
  - 0–20: no effect, optional Codex flavor text
  - 21–50: client-side screen-edge static/vignette effect + faint whisper sounds
  - 51–80: trigger Unwoven ambush spawns near player (timer-based, not every tick)
  - 81–100: increase ambush frequency; enable Codex page "mis-writing" (cosmetic text glitch)
- [ ] Natural decay: when not practicing Forbidden arts, personal Fray decays by 1 every 5 minutes (configurable)
- [ ] Sync to client for rendering effects

### Frayed Mob Spawning
- [ ] Create a custom spawn rule that checks regional Fray of the chunk
- [ ] "Frayed" mobs are corrupted variants of vanilla mobs (zombie, skeleton, spider):
  - Retextured (desaturated + glowing eyes)
  - Slightly stronger stats (+20% HP, +10% damage)
  - Drop Facet-rich items (for alchemy use later)
- [ ] Register entities: `frayed_zombie`, `frayed_skeleton`, `frayed_spider`
- [ ] Extend vanilla mob classes, override textures, apply stat modifiers

### Client-side Fray Rendering
- [ ] Regional Fray terrain overlay: when player is in a chunk with Fray > 20, apply a desaturation post-processing effect or tint to nearby blocks
  - Consider a simple fog color shift or shader uniform
- [ ] Personal Fray screen effects:
  - 21–50: subtle dark vignette at screen edges, pulsing slowly
  - 51+: vignette intensifies + add a faint particle overlay
  - Play ambient whisper sounds at random intervals (register sound events)

---

## 0.6 Resonometer

### What it does
The player's scanning tool. Point at a block/item/mob, hold right-click for ~1.5 seconds, and the Facet signature is revealed and logged to the Codex.

### Implementation
- [ ] Register item: `resonometer`
- [ ] Recipe: 2 wyrd_dust + 1 glass pane + 1 iron ingot (shaped)
- [ ] Use mechanic:
  - On right-click hold, start a 1.5-second use timer (`getUseDuration`)
  - Raycast to find the targeted block or entity (range: 8 blocks)
  - On completion:
    - Look up target's `FacetSignature` from `FacetRegistry`
    - Add to player's `PlayerFacetKnowledge`
    - Send packet to client with the scan result
    - Display result as a tooltip overlay near the crosshair (HUD rendering)
    - Play scan-complete sound + particle burst on the target
- [ ] If target has no registered Facets, show "No magical resonance detected"
- [ ] Scanning the same target again shows the cached result instantly (no hold delay)

---

## 0.7 Loom of Understanding — Research Table

### What it does
The mod's research table. A block with a GUI where the player connects discovered Facet nodes with "threads" to unlock Codex pages and recipes.

### Block
- [ ] Register block: `loom_of_understanding`
- [ ] Block entity: `LoomOfUnderstandingBlockEntity` — stores current research state if player leaves mid-puzzle
- [ ] Model: a table with glowing thread spools (custom JSON model)
- [ ] Right-click opens the research GUI

### Research Data
- [ ] Define research nodes in JSON: `data/codexarcanum/research/*.json`
  ```json
  {
    "id": "sensing_the_weave",
    "section": 1,
    "display_name": "Sensing the Weave",
    "description": "Craft the Resonometer",
    "required_facets": ["ignis", "cognitio"],
    "difficulty": 1,
    "prerequisites": ["awakening"],
    "unlocks": ["resonometer_recipe"],
    "lore_text": "Thale's note about sensing..."
  }
  ```
- [ ] Create `ResearchTree` — loaded from JSON, defines all research nodes and their prerequisites
- [ ] Create `PlayerResearchData` capability:
  - `Set<ResourceLocation> completedResearch`
  - `Map<ResourceLocation, ResearchProgress> inProgressResearch`

### Research GUI (Screen + Menu)
- [ ] Register menu type: `loom_of_understanding_menu`
- [ ] Build a custom `AbstractContainerScreen`:
  - Display available research nodes as clickable icons arranged in a tree/web layout
  - Locked nodes (missing prerequisites) shown greyed out
  - Clicking an available node opens the puzzle sub-view
- [ ] **Puzzle mechanic**: a grid where known Facet nodes are placed, and the player draws "threads" between them:
  - Each research node has a target pattern (which Facets must connect to which)
  - Player drags threads between Facet icons
  - Correct connections light up; completing the pattern unlocks the research
  - Difficulty 1 (Section 1): pattern is shown as a hint, almost no puzzle
  - Difficulty 2–3 (Sections 2–3): partial hints
  - Difficulty 4+ (Sections 4–7): no hints, player must experiment or find clues
- [ ] On research completion:
  - Add to `PlayerResearchData.completedResearch`
  - Unlock associated recipes (add to player's recipe book or mod-internal unlock system)
  - Trigger Codex page fill-in animation (if Codex is open)
  - Play unlock sound + particle effect

### Codex Arcanum Item (Guidebook UI)
- [ ] Register item: `codex_arcanum`
- [ ] Right-click opens the Codex GUI
- [ ] Codex GUI is a book-style interface:
  - 7 sections (tabs), matching the design document
  - Each section shows research nodes the player has completed as filled pages
  - Locked/undiscovered nodes show as blank/torn pages
  - Pages display: lore text, recipe diagrams, item/block info
- [ ] The Codex is the mod's central reference — design for expandability (each stage adds pages)
- [ ] Obtaining the Codex: found in a structure loot chest near spawn OR craftable (1 book + 1 wyrd_dust)

---

## 0.8 Networking Summary

All custom packets needed for this stage:

| Packet | Direction | Purpose |
|--------|-----------|---------|
| `SyncWeaveDataPacket` | S→C | Sync chunk's currentDensity + regionalFray to nearby clients |
| `SyncPlayerFrayPacket` | S→C | Sync player's personalFray to their client |
| `SyncFacetKnowledgePacket` | S→C | Sync discovered facets to client |
| `SyncResearchPacket` | S→C | Sync completed/available research to client |
| `ScanResultPacket` | S→C | Send Resonometer scan result to client for HUD display |
| `ResearchCompletePacket` | C→S | Player completed a research puzzle |
| `WyrdChannelStartPacket` | C→S | Player started channeling from Wellspring |

---

## 0.9 Data Generation Status: MOSTLY COMPLETE (lang, recipes, loot tables, mining tags, models — worldgen stays hand-written)

- [x] Language file (en_us): all item/block/entity/GUI names — `data/ArcaneLanguageProvider`
- [x] Recipes: Wyrdstone → Wyrd Dust (crafting + smelting), Resonometer, Wand, Loom of Understanding — `data/ArcaneRecipeProvider`
- [x] Tags: `minecraft:mineable/pickaxe`, `minecraft:needs_iron_tool` for both ores (+ Wellspring in the pickaxe tag only) — `data/ArcaneBlockTagsProvider`
- [x] Loot tables: both ores (fortune/silk-touch aware), Wellspring, Loom (`dropSelf`), all 3 Frayed mobs — `data/ArcaneBlockLootSubProvider`/`ArcaneEntityLootSubProvider`
- [x] Block/item models: Wyrdstone ore, Deepslate Wyrdstone ore, Loom of Understanding (blocks), Wand, Resonometer, Codex Arcanum, Wyrd Dust, Wyrdstone, Cursed Lectern (items) — `data/ArcaneModelProvider`/`ArcaneItemModelGenerators`. **Weave Wellspring's model/blockstate/item stay hand-written** — its two-box pedestal shape has no matching template in vanilla's model-datagen system (see Deviations below); this is a genuine content gap, not something datagen was skipped for.
- [ ] Worldgen features: Wyrdstone ore veins, Wellspring placement — **deliberately left hand-written**. Uses a third, unrelated datagen API (dynamic-registry `RegistrySetBuilder`/bootstrap contexts) and covers a small, stable set of content (2 configured features, 4 placed features, 4 biome modifiers, 5 biome tags) that doesn't grow with new items/blocks the way recipes/loot/models do — hand-authoring doesn't "hurt" here the way it started to elsewhere. Revisit if worldgen content grows substantially in a later stage.

### Deviations from the plan as originally written
- **This MC version replaced Forge's old `ItemModelProvider`/`BlockStateProvider` DSL with vanilla's own first-party model-datagen system** (`net.minecraft.client.data.models`) — that Forge package doesn't exist in this build at all. The new system is validation-heavy: `ModelProvider`'s internal collectors require *every* block/item in `BuiltInRegistries` to have a generated entry, which Forge patches into a per-mod-scopable `getKnownBlocks()`/`getKnownItems()` override point (confirmed by reading `ModelProvider.java.patch`, not the plain decompiled class) — the same pattern Forge uses for `BlockLootSubProvider`/`EntityLootSubProvider`'s otherwise-identical "validate against the whole shared registry" problem.
- **`ArcaneModelProvider` doesn't extend vanilla's `ModelProvider`** — that class's `run()` calls `BlockModelGenerators`/`ItemModelGenerators`'s own `.run()`, which hardcodes generation for every vanilla block/item by name. Instead it builds the same three collector objects directly (`ModelProvider.ItemInfoCollector`/`BlockStateGeneratorCollector`/`SimpleModelCollector`, all public nested classes) and calls only the generic, reusable helper methods for this mod's own content — see CLAUDE.md for the exact API shape (`BlockFamilyProvider`, `generateFlatItem`, etc.).
- **Recipes gained unlock-advancement criteria they didn't have before** — hand-written recipes had no `unlockedBy`; the datagen recipe builders require at least one criterion to avoid an "unlockable recipe" validation failure, so each recipe now unlocks via having its main ingredient (e.g. `has(Items.STICK)` for the wand). Purely additive — recipes still work identically without ever completing the advancement, this only affects the recipe-book unlock toast.
- **Recipe folder confirmed to stay singular (`recipe/`) through datagen too** — `PackOutput.createRegistryElementsPathProvider(Registries.RECIPE)` derives from the same (Forge-patched) registry key path segment the loader itself uses, so generated recipes land in the same place the hand-written ones did.

### Workflow addition
Any change to a `data/Arcane*Provider` class needs `./gradlew runData` re-run to regenerate `src/generated/resources` before the change is visible in `runServer`/`runClient` — datagen doesn't run automatically as part of those tasks. Re-running `runData` is safe/idempotent (`--existing src/main/resources` merges with hand-written content; regenerated files overwrite their previous generated versions via the hash cache in `src/generated/resources/.cache`).

---

## 0.10 Testing Checklist

- [ ] Wyrdstone ore generates underground and is mineable
- [ ] Wyrdstone smelts/crafts into Wyrd Dust
- [ ] Weave Wellsprings spawn in the world and accumulate Wyrd over time
- [ ] Resonometer scans blocks and reveals Facets
- [ ] Loom of Understanding opens and displays research tree
- [ ] A research node can be completed via the puzzle
- [ ] Codex Arcanum opens and shows completed research as pages
- [ ] Chunk Weave density varies by biome
- [ ] Fray values persist across save/load
- [ ] Frayed mobs spawn in high-Fray chunks
- [ ] Client-side Fray effects render (vignette, desaturation)
- [ ] Wellspring Wyrd channeling works with right-click hold
