# Stage 1 — Introduction & Wands
## Codex Arcanum · Developer Implementation Plan

**Depends on:** Stage 0 (Core Systems) fully complete
**Covers:** Codex Sections 1 (Introduction) and 2 (Wands)

---

## 1.1 Section 1 — Introduction Items & Blocks

### Cobblestone Wand
- [ ] Register item: `cobblestone_wand`
- [ ] Recipe: shaped — 1 stick (bottom), 2 cobblestone (middle + top) in a vertical line
- [ ] Properties:
  - Wyrd capacity: 50 (very low)
  - Can cast only `Spark` spell (see Stage 2, but register the basic bolt here as the intro spell)
  - Can interact with magical blocks: right-click Wellspring to draw Wyrd, right-click Loom to activate
  - No core/cap/binding system — this is a monolithic item
  - Durability: 64 uses
- [ ] Model: simple stick with a rough cobblestone tip (JSON model or 2D sprite)
- [ ] Spark spell (basic version):
  - Right-click fires a small projectile entity (`spark_bolt`)
  - Deals 3 damage (1.5 hearts), short range (16 blocks)
  - Costs 5 Wyrd per cast
  - Register projectile entity: `spark_bolt` — extends `AbstractHurtingProjectile` or custom `ThrowableProjectile`
  - Particle trail: small orange/yellow sparks
  - On impact: small fire particle burst, damage to hit entity

### Codex Arcanum Acquisition
- [ ] The Codex Arcanum item (registered in Stage 0) needs an acquisition path:
  - **Option A — Structure loot**: place in a custom small structure ("Loomkeeper's Satchel") that generates near world spawn
    - Create structure: `loomkeeper_satchel` — a buried chest or a small stone shelf, 3–5 blocks, with the Codex inside
    - Structure placement: within 200 blocks of world spawn, in forest/plains biomes
  - **Option B — Recipe**: shapeless recipe: 1 book + 1 wyrd_dust → codex_arcanum
  - Implement both: structure for exploration players, recipe as fallback
- [ ] First-open event: when the Codex is right-clicked for the first time, auto-unlock the `awakening` research node and show a welcome page with Thale's introduction text

### Research Nodes for Section 1
Implement these research entries (JSON data, unlockable via Loom of Understanding):

| Node ID | Name | Prerequisites | Unlocks | Difficulty |
|---------|------|---------------|---------|------------|
| `awakening` | Awakening | none (auto) | Codex basic pages | 0 |
| `sensing_the_weave` | Sensing the Weave | awakening | Resonometer recipe | 1 |
| `wyrdstone_refining` | Wyrdstone Refining | awakening | Wyrd Dust recipe | 1 |
| `first_threads` | First Threads | sensing_the_weave | Loom of Understanding recipe | 1 |
| `the_first_wellspring` | The First Wellspring | first_threads | Wellspring interaction tutorial | 1 |
| `a_crude_focus` | A Crude Focus | the_first_wellspring | Cobblestone Wand recipe + Spark | 1 |

- [ ] Write JSON research data for each node
- [ ] Write lore text for each node (Thale's voice — warm, slightly wry, teaching tone)
- [ ] `a_crude_focus` completion triggers a Codex notification pointing to Section 2 (Wands)

---

## 1.2 Section 2 — Wandwright's Bench

### Block
- [ ] Register block: `wandwrights_bench`
- [ ] Block entity: `WandwrightsBenchBlockEntity`
  - No internal inventory storage — it's a pure crafting station (items in → item out)
- [ ] Recipe: 2 wyrdstone + 1 crafting_table + 2 planks (shaped)
- [ ] Model: a workbench with visible tool slots and a glowing core socket (custom JSON model or blockstate)

### Wandwright's Bench GUI
- [ ] Register menu type: `wandwrights_bench_menu`
- [ ] Screen layout:
  ```
  [ Core Slot ] [ Cap Slot ] [ Binding Slot ]  →  [ Output Slot ]
                [ Inlay Slot (optional) ]
  ```
- [ ] Validation logic:
  - Core is required
  - Cap is required
  - Binding is required
  - Inlay is optional (empty = no inlay perk)
  - Output produces the assembled wand/staff item with NBT/components encoding the parts
- [ ] Disassembly mode: shift-right-click an assembled wand on the bench to break it back into parts (with durability loss on parts)

---

## 1.3 Modular Wand System

### Wand Item
- [ ] Register item: `modular_wand`
- [ ] This is a single item ID whose behavior changes entirely based on its stored components
- [ ] Data storage (NBT or data components):
  ```
  {
    "core": "codexarcanum:wyrdstone_core",
    "cap": "codexarcanum:copper_cap",
    "binding": "codexarcanum:leather_binding",
    "inlay": null,
    "foci": [null, null],  // slots, count depends on tier
    "wyrd": 0,
    "maxWyrd": 200
  }
  ```
- [ ] Dynamic properties computed from parts:
  - `maxWyrd` = core base capacity × binding multiplier
  - `castEfficiency` = core efficiency rating (Wyrd cost multiplier per spell)
  - `castSpeed` = cap modifier
  - `durability` = cap durability rating
  - `fociSlotCount` = 2 for wands, 4 for staves
  - `elementalAffinity` = cap's affinity (if any)
  - `passivePerk` = inlay effect (if socketed)

### Wand Components — Items

#### Cores
- [ ] Register items and define stats:

| Item ID | Display Name | Base Capacity | Efficiency | Notes |
|---------|-------------|---------------|------------|-------|
| `wood_core` | Wood Core | 100 | 1.0× | Recipe: 2 sticks + 1 wyrd_dust |
| `wyrdstone_core` | Wyrdstone Core | 200 | 0.85× | Recipe: 2 wyrdstone + 1 wyrd_dust |
| `quicksilver_core` | Quicksilver Core | 80 | 0.6× | Recipe: 2 iron + 1 redstone + 1 wyrd_dust |
| `voidglass_core` | Voidglass Core | 400 | 1.3× | Recipe: 2 obsidian + 1 ender_pearl + 2 wyrd_dust. Passive: +0.1 Fray/min while equipped |

*(Bloodwood Core is Stage 5 — Forbidden Knowledge)*

#### Caps
- [ ] Register items and define stats:

| Item ID | Display Name | Effect | Recipe |
|---------|-------------|--------|--------|
| `copper_cap` | Copper Cap | +25% Wyrd draw speed, +10% cast speed | 2 copper_ingot + 1 wyrd_dust |
| `iron_cap` | Iron Cap | +50% durability, no elemental bias | 2 iron_ingot + 1 wyrd_dust |
| `prism_cap` | Prism Cap | Single-target spells hit up to 3 targets at 50% power | 2 amethyst_shard + 1 prismarine_shard + 1 wyrd_dust |
| `quartz_cap` | Quartz Cap | +50% spell range | 2 quartz + 1 wyrd_dust |

*(Sable Cap is Stage 5 — Forbidden Knowledge)*

#### Bindings
- [ ] Register items and define stats:

| Item ID | Display Name | Effect | Recipe |
|---------|-------------|--------|--------|
| `leather_binding` | Leather Binding | No bonus (baseline) | 2 leather + 1 string |
| `silk_binding` | Silk Binding | -15% cooldown reduction | 2 string + 1 wyrd_dust |
| `wyrdthread_binding` | Wyrdthread Binding | +20% Wyrd capacity bonus | 2 wyrd_dust + 1 string |
| `voidweave_binding` | Voidweave Binding | +15% Fray resistance | 2 wyrd_dust + 1 obsidian + 1 string |

#### Inlays (Optional Socket Gems)
- [ ] Register items:

| Item ID | Display Name | Effect | Recipe |
|---------|-------------|--------|--------|
| `ruby_inlay` | Ruby Inlay | +10% Wyrd regen while holding wand near Wellspring | 1 redstone_block + 1 wyrd_dust |
| `sapphire_inlay` | Sapphire Inlay | 10% chance to not consume Wyrd on cast | 1 lapis_block + 1 wyrd_dust |
| `emerald_inlay` | Emerald Inlay | +1 Foci slot | 1 emerald + 1 wyrd_dust |
| `diamond_inlay` | Diamond Inlay | +25% durability | 1 diamond + 1 wyrd_dust |

---

## 1.4 Staff System

### Staff Item
- [ ] Register item: `modular_staff`
- [ ] Same component system as wands but:
  - Base capacity multiplier: 1.5× the core's value
  - Foci slots: 4 (vs 2 for wands)
  - Two-handed: occupies both main hand and off-hand (prevent off-hand item use while wielding)
  - Unlocks channeled spells: spells with `channeled: true` can only be cast from staves
- [ ] Requires an upgraded bench to craft:
  - `advanced_wandwrights_bench` — recipe: 1 wandwrights_bench + 4 wyrdstone + 2 wyrd_dust

---

## 1.5 Foci System

### Focus Items
- [ ] Register item: `spell_focus`
- [ ] A Focus is a craftable item that holds one spell:
  - Data: `{ "spell": "codexarcanum:spark", "charges": 50, "maxCharges": 50 }`
  - Crafted at the Wandwright's Bench: 1 amethyst_shard + 1 wyrd_dust + spell-specific Facet items
  - Each spell has its own Focus recipe (defined per-spell in Stage 2+)
- [ ] Focus Slotting UI:
  - When holding a wand/staff, press a keybind (suggest `F`) to open the Focus management overlay
  - Shows the wand's available Focus slots (2–4 depending on tier)
  - Drag foci in/out from inventory
  - Active Focus is selected via scroll wheel or number keys (like hotbar sub-selection)
- [ ] Casting: right-click with wand → fires the active Focus's spell, consuming Wyrd from wand and reducing Focus charges
  - When charges deplete, Focus becomes inert (greyed out) until re-crafted or repaired at bench

---

## 1.6 Named Wands (World Loot)

### Loomkeeper's Wand
- [ ] Pre-assembled modular_wand with: Wyrdstone Core, Prism Cap, Silk Binding
- [ ] Added to Loomhold Outpost loot tables (when Loomholds are built in Stage 5)
- [ ] Custom name + lore text in item tooltip: "A common-issue research wand, still in working order."

### Duskbrand Staff
- [ ] Pre-assembled modular_staff with: Bloodwood Core, Sable Cap, Voidweave Binding
- [ ] Loomhold deep vault loot (Stage 5)
- [ ] Custom name + lore text: "The core hums with a sound that isn't quite a sound."
- [ ] Note: Bloodwood Core and Sable Cap items are registered in Stage 5; this loot entry references them

---

## 1.7 Research Nodes for Section 2

| Node ID | Name | Prerequisites | Unlocks | Difficulty |
|---------|------|---------------|---------|------------|
| `wandwrights_bench` | The Wandwright's Bench | a_crude_focus | Bench recipe | 2 |
| `core_attunement_wood` | Core Attunement: Wood | wandwrights_bench | Wood Core recipe | 1 |
| `core_attunement_wyrdstone` | Core Attunement: Wyrdstone | core_attunement_wood | Wyrdstone Core recipe | 2 |
| `core_attunement_quicksilver` | Core Attunement: Quicksilver | core_attunement_wyrdstone | Quicksilver Core recipe | 2 |
| `core_attunement_voidglass` | Core Attunement: Voidglass | core_attunement_quicksilver | Voidglass Core recipe | 3 |
| `cap_fitting_copper` | Cap Fitting: Copper | wandwrights_bench | Copper Cap recipe | 1 |
| `cap_fitting_iron` | Cap Fitting: Iron | cap_fitting_copper | Iron Cap recipe | 2 |
| `cap_fitting_quartz` | Cap Fitting: Quartz | cap_fitting_copper | Quartz Cap recipe | 2 |
| `cap_fitting_prism` | Cap Fitting: Prism | cap_fitting_iron, cap_fitting_quartz | Prism Cap recipe | 3 |
| `focus_crafting` | Focus Crafting | wandwrights_bench | Focus slotting mechanic | 2 |
| `the_long_reach` | The Long Reach | focus_crafting, core_attunement_wyrdstone | Staff crafting + channeled spells | 3 |
| `inlay_work` | Inlay Work | the_long_reach | Inlay socket recipes | 3 |

- [ ] Write JSON research data for all nodes
- [ ] Write lore text for each (Thale's workshop-manual voice, margin doodles referenced in descriptions)

---

## 1.8 Data Generation

- [ ] Block/item models: Wandwright's Bench, Cobblestone Wand, all cores/caps/bindings/inlays, modular_wand (with texture layers for each part), modular_staff, spell_focus
- [ ] Recipes: all items listed above
- [ ] Loot tables: Loomkeeper's Satchel structure chest
- [ ] Tags: `codexarcanum:wand_cores`, `codexarcanum:wand_caps`, `codexarcanum:wand_bindings`, `codexarcanum:wand_inlays`
- [ ] Language file entries for all new items/blocks/GUIs
- [ ] Worldgen: Loomkeeper's Satchel structure (SNBT or structure template)

---

## 1.9 Testing Checklist

- [ ] Cobblestone Wand is craftable and can fire Spark
- [ ] Codex Arcanum can be found in a Satchel or crafted
- [ ] First-open Codex triggers Awakening research
- [ ] All Section 1 research nodes unlock in sequence via the Loom
- [ ] Wandwright's Bench opens its GUI and accepts core + cap + binding
- [ ] Assembled wand has correct stats based on components
- [ ] Wand can draw Wyrd from Wellspring
- [ ] Foci can be crafted, slotted, and used to cast spells
- [ ] Staff requires Advanced Bench and allows 4 foci slots
- [ ] Inlays provide their passive perks when socketed
- [ ] Disassembly returns components at the bench
- [ ] Wand tooltip shows all component info and current Wyrd
