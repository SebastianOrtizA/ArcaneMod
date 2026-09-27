# Stage 3 — Rituals & Alchemy
## Codex Arcanum · Developer Implementation Plan

**Depends on:** Stage 1 (wands, Wyrd Dust), partial overlap with Stage 2 (some catalyst items are alchemy outputs)
**Covers:** Codex Sections 4 (Rituals) and 5 (Alchemy)

---

## PART A — RITUALS (Section 4)

---

## 3A.1 Ritual Infrastructure Blocks

### Anchor Brazier
- [ ] Register block: `anchor_brazier`
- [ ] Block entity: `AnchorBrazierBlockEntity`
  - `int storedWyrd`, `int maxWyrd` (default: 1000)
  - Tick logic: if a Weave Wellspring exists within 8 blocks, slowly draw Wyrd from it (10 Wyrd/sec)
  - Can be manually charged: right-click with a wand to transfer Wyrd from wand → brazier
- [ ] Visual: stone bowl with animated Wyrd flame; flame intensity scales with stored Wyrd
- [ ] Recipe: 4 stone_bricks + 2 wyrdstone + 1 wyrd_dust (shaped, bowl pattern)
- [ ] Provides Wyrd to active rituals within 16-block radius

### Ritual Pedestal
- [ ] Register block: `ritual_pedestal`
- [ ] Block entity: `RitualPedestalBlockEntity`
  - `ItemStack heldItem` — single item slot, placed/removed by right-click
  - Render the held item floating and slowly rotating above the pedestal
- [ ] Recipe: 3 stone_bricks + 1 wyrdstone (shaped, T-pattern)
- [ ] Model: short stone pillar with a flat top

### Etching Tool
- [ ] Register item: `etching_tool`
- [ ] Recipe: 1 iron_ingot + 1 wyrd_dust + 1 stick (shaped)
- [ ] Use: right-click on the ground to place `ritual_etching` blocks — cosmetic floor markings that define the ritual circle's pattern
- [ ] `ritual_etching` block: flat overlay block (like carpets), stores pattern type in blockstate
- [ ] Pattern types: `circle_small` (3×3), `circle_medium` (5×5), `circle_large` (7×7)
- [ ] Shift-right-click to cycle pattern type before placing

---

## 3A.2 Ritual System

### Ritual Circle Detection
- [ ] When a player activates a central pedestal (right-click with wand while it holds an item):
  1. Scan for surrounding `ritual_pedestal` blocks in the expected pattern shape
  2. Check for `ritual_etching` blocks forming a valid pattern
  3. Count surrounding pedestals and read their held items
  4. Look up the combination in the `RitualRegistry`:
     - Central item + surrounding catalyst items → ritual ID
  5. Check for Anchor Braziers within 16 blocks with sufficient Wyrd
  6. If valid: start the ritual

### Ritual Registry
- [ ] JSON-driven ritual definitions: `data/codexarcanum/rituals/*.json`
  ```json
  {
    "id": "codexarcanum:rite_of_ember_edge",
    "display_name": "Rite of the Ember Edge",
    "circle_size": "small",
    "pedestal_count": 4,
    "central_item": { "tag": "forge:swords" },
    "catalysts": [
      { "item": "minecraft:coal", "count": 1 },
      { "item": "minecraft:blaze_powder", "count": 1 },
      { "item": "codexarcanum:wyrd_dust", "count": 1 },
      { "item": "minecraft:coal", "count": 1 }
    ],
    "wyrd_cost": 200,
    "duration_ticks": 1200,
    "result_type": "enchant_item",
    "result_data": { "enchantment": "codexarcanum:ember_edge", "level": 1 },
    "fray_cost_regional": 0,
    "fray_cost_personal": 0,
    "required_research": "the_first_circle"
  }
  ```

### Ritual Execution
- [ ] Create `ActiveRitual` state object tracked by the central pedestal's block entity:
  - `ritualId`, `progressTicks`, `totalTicks`, `wyrdConsumed`, `status`
- [ ] Tick logic:
  1. Each tick, draw Wyrd from nearby Anchor Braziers (proportional to `wyrdCost / durationTicks`)
  2. If insufficient Wyrd: stall (no progress, no loss — resume when power returns)
  3. Play ambient ritual particles: items on pedestals emit Facet-colored particles spiraling toward center
  4. Ambient sound: low hum that rises in pitch as the ritual progresses
  5. On completion:
     - Apply ritual result to the central item (enchant, transmute, etc.)
     - Consume catalyst items from pedestals
     - Play completion burst of particles + chime sound
     - Add Regional Fray if `fray_cost_regional > 0`
- [ ] **Backlash (wrong catalysts)**:
  - If catalysts don't match any registered ritual for the given central item → backlash
  - Explosion: small (power 1.5, no block damage), centered on central pedestal
  - Add 5 Regional Fray to the chunk
  - Scatter catalyst items as drops
  - Play alarming sound + dark particle burst

### Ritual Result Types
- [ ] `enchant_item` — apply a custom enchantment to the central item
- [ ] `transmute_item` — replace the central item with a different item/stack
- [ ] `transmute_bulk` — convert a stack on the central pedestal into a different stack
- [ ] `place_block` — create a block in the world (used for Wellspring Raising)
- [ ] `area_effect` — apply an effect to the surrounding area (used for Cleansing)

---

## 3A.3 Custom Enchantments

Register these as custom enchantments (or custom item modifiers if enchantment system is too restrictive):

| Enchantment ID | Name | Effect | Applied to |
|---------------|------|--------|-----------|
| `ember_edge` | Ember Edge | +3 fire damage on hit, brief ignite | Swords, axes |
| `bedrock_grasp` | Bedrock's Grasp | Right-click with pickaxe highlights nearby ores for 5s (within 8 blocks, glowing outline) | Pickaxes |
| `feather_step` | Feather Step | -50% fall damage, +10% move speed | Boots |
| `wyrdweaving` | Wyrdweaving | +25% Wyrd capacity on wand/staff | Wands, staves |

---

## 3A.4 Ritual Definitions

| Ritual ID | Name | Circle | Catalysts | Wyrd | Duration | Result |
|-----------|------|--------|-----------|------|----------|--------|
| `rite_of_ember_edge` | Rite of the Ember Edge | small (4 pedestals) | 2 coal, 1 blaze_powder, 1 wyrd_dust | 200 | 60s | Enchant: ember_edge |
| `rite_of_bedrocks_grasp` | Rite of Bedrock's Grasp | small (4 pedestals) | 2 iron_ore, 1 gold_nugget, 1 wyrd_dust | 250 | 60s | Enchant: bedrock_grasp |
| `rite_of_feather_step` | Rite of the Feather Step | small (4 pedestals) | 2 feathers, 1 phantom_membrane, 1 wyrd_dust | 200 | 60s | Enchant: feather_step |
| `rite_of_wyrdweaving` | Rite of Wyrdweaving | medium (8 pedestals) | 4 wyrd_dust, 2 wyrdstone, 1 amethyst, 1 gold_ingot | 400 | 90s | Enchant: wyrdweaving |
| `rite_of_transmutation` | Rite of Transmutation | medium (8 pedestals) | Facet-matched materials (varies) | 300 | 45s | Transmute central stack |
| `rite_of_wellspring_raising` | Rite of Wellspring Raising | large (12 pedestals) | 4 wyrdstone_block, 4 wyrd_dust, 2 stone_bricks, 1 diamond, 1 gold_block | 800 | 180s | Place: weave_wellspring (player-built, weaker) |
| `rite_of_cleansing` | Rite of Cleansing | medium (8 pedestals) | 4 glowstone_dust, 2 golden_apple, 1 diamond, 1 wyrd_dust | 500 | 120s | Area: set chunk Regional Fray to 0 |

### Transmutation Recipes
- [ ] Define sub-recipes for Rite of Transmutation:

| Input | Output | Catalyst Facets |
|-------|--------|----------------|
| 16 cobblestone | 16 stone_bricks | TERRA |
| 8 iron_ore | 4 gold_ore | METALLUM |
| 16 sand | 16 glass | IGNIS, TERRA |
| 8 rotten_flesh | 4 leather | VITA, PERDO |
| 4 gravel | 4 flint | TERRA, PERDO |

---

## 3A.5 Research Nodes for Section 4

| Node ID | Name | Prerequisites | Unlocks | Difficulty |
|---------|------|---------------|---------|------------|
| `the_first_circle` | The First Circle | a_crude_focus | Brazier, Pedestal, Etching Tool recipes; Rite of Ember Edge | 2 |
| `pedestal_attunement` | Pedestal Attunement | the_first_circle | Medium circles (8 pedestals) | 3 |
| `catalyst_reagents` | Catalyst Reagents | pedestal_attunement | Rite of Bedrock's Grasp, Feather Step, Wyrdweaving | 3 |
| `rite_of_transmutation` | Rite of Transmutation | catalyst_reagents | Transmutation recipes | 3 |
| `rite_of_wellspring_raising` | Rite of Wellspring Raising | rite_of_transmutation | Player-built Wellsprings | 4 |
| `rite_of_cleansing` | Rite of Cleansing | pedestal_attunement | Regional Fray removal | 3 |
| `greater_circles` | Greater Circles | rite_of_wellspring_raising | Large circles (12 pedestals), endgame rites | 4 |

---

## PART B — ALCHEMY (Section 5)

---

## 3B.1 Essence System

### Essence Data Model
- [ ] Create `Essence` type — directly mapped to Facets:
  - Each primal Facet produces a corresponding Essence (Essence of Ignis, Essence of Vita, etc.)
  - Compound Facets produce compound Essences (Essence of Metallum, etc.)
- [ ] Create `EssenceStack` — amount + type, similar to FluidStack:
  ```java
  public record EssenceStack(Facet facet, int amount) {}
  ```
- [ ] Maximum Essence per container slot: 1000 units

### Reagent → Essence Mapping
- [ ] JSON-driven: `data/codexarcanum/alchemy/reagents/*.json`
  ```json
  {
    "item": "minecraft:coal",
    "essence_output": { "ignis": 20, "terra": 5 },
    "dust_yield": 2
  }
  ```
- [ ] Ship default mappings for ~40 vanilla items (ores, organic materials, mob drops)

---

## 3B.2 Alchemy Blocks

### Mortar & Pestle
- [ ] Register block: `mortar_and_pestle`
- [ ] Block entity: `MortarAndPestleBlockEntity`
  - Input slot: 1 item
  - Output slot: 1 item (alchemical dust)
  - Processing: right-click 4 times (each click plays a grinding animation and sound); after 4 clicks, input consumed, output produced
  - No GUI screen — interaction is physical (right-click in-world)
- [ ] Recipe: 3 stone + 1 stick (shaped)
- [ ] Output: `alchemical_dust` item with NBT storing the original item's Facet signature

### Distillation Rig
- [ ] Register block: `distillation_rig`
- [ ] Block entity: `DistillationRigBlockEntity`
  - Input slot: 1 alchemical_dust
  - Fuel: Wyrd (drawn from wand right-click or connected Brazier)
  - Output: Essence stored internally (up to 500 units per Facet)
  - Processing time: 5 seconds per dust
  - Tick logic: if has dust + Wyrd → process → extract Essence matching the dust's Facet signature
  - Output extraction: right-click with a Vial to fill it with stored Essence
- [ ] Register item: `essence_vial` — holds one EssenceStack (up to 100 units)
  - Empty vial recipe: 3 glass (bottle-shape)
  - Filled vials are colored by Facet (tint layer on item model)
- [ ] Recipe: 4 iron_ingot + 1 glass + 2 wyrd_dust (shaped)
- [ ] Model: an alembic-like apparatus, with visible bubbling particles when active

### Essence Reservoir
- [ ] Register block: `essence_reservoir`
- [ ] Block entity: `EssenceReservoirBlockEntity`
  - Stores one type of Essence, up to 5000 units
  - Right-click with a filled Vial to deposit; right-click with empty Vial to withdraw
  - Visual: a large jar with colored liquid (color matches stored Essence Facet)
  - Comparator output: signal strength proportional to fill level
- [ ] Recipe: 4 glass + 2 iron_ingot + 1 wyrdstone + 1 wyrd_dust

### Essence Conduit
- [ ] Register block: `essence_conduit`
- [ ] Block entity: `EssenceConduitBlockEntity`
  - Connects Distillation Rigs → Reservoirs → Cauldrons
  - Transfers Essence automatically between adjacent alchemy blocks (10 units/tick)
  - Direction: from blocks with more Essence to blocks with less (like fluid flow)
  - Visual: glass tube with visible colored particles flowing through it
- [ ] Recipe: 4 glass_pane + 1 wyrd_dust (shaped, yields 8)

### Cauldron of Making
- [ ] Register block: `cauldron_of_making`
- [ ] Block entity: `CauldronOfMakingBlockEntity`
  - Essence input: draws from connected Reservoirs/Conduits or from Vials placed in GUI slots
  - Material input slots: 3 item slots for dust/reagents/water buckets
  - Output slot: 1 slot for the crafted result
  - Wyrd input: draws from nearby Brazier
  - Recipes defined in JSON: `data/codexarcanum/alchemy/recipes/*.json`
- [ ] GUI: register menu type `cauldron_of_making_menu`
  - Layout:
    ```
    [Essence Slot 1] [Essence Slot 2] [Essence Slot 3]
    [Material Slot 1] [Material Slot 2] [Material Slot 3]
                    [ OUTPUT ]
    Wyrd gauge: [████████░░]
    ```
  - Show required Essence types and amounts for the selected recipe
  - Arrow progress indicator during crafting
- [ ] Recipe: 1 cauldron + 4 wyrdstone + 2 wyrd_dust + 1 blaze_rod

---

## 3B.3 Alchemy Recipes

| Recipe ID | Name | Essence Inputs | Material Inputs | Wyrd | Output |
|-----------|------|---------------|----------------|------|--------|
| `essence_of_vita` | Essence of Vita | — | 4 saplings or 4 wheat | 10 | 50 units Vita Essence |
| `essence_of_ignis` | Essence of Ignis | — | 4 coal or 2 blaze_powder | 10 | 50 units Ignis Essence |
| `draught_of_clear_sight` | Draught of Clear Sight | 30 Lux, 30 Cognitio | 1 glass_bottle, 1 wyrd_dust | 50 | 1 draught_of_clear_sight |
| `salve_of_mending` | Salve of Mending | 50 Vita | 2 wyrd_dust, 1 honey_bottle | 80 | 1 salve_of_mending |
| `philosophers_clay` | Philosopher's Clay | 100 Terra, 50 Metallum | 4 clay, 2 wyrd_dust | 200 | 4 philosopher's_clay |
| `draught_of_unbinding` | Draught of Unbinding | 50 Lux, 50 Ordo | 1 glass_bottle, 1 golden_apple, 1 wyrd_dust | 150 | 1 draught_of_unbinding |
| `concentrated_essence` | Concentrated Essence | 200 of any single type | 1 wyrd_dust | 100 | 1 concentrated_essence_block |

### Consumable Items
- [ ] `draught_of_clear_sight` — potion-type item, on drink: grants 60s of Weave density + Facet visibility (like Resonometer sight, but passive and AoE). Custom mob effect: `clear_sight_effect`
- [ ] `salve_of_mending` — right-click to use: heals 6 HP + repairs 50 durability of held item in off-hand. Consumable, single use
- [ ] `philosophers_clay` — crafting material: can be used in a crafting table as `1 philosopher's_clay + 8 of any common ore → 8 of the next-tier ore` (iron_ore → gold_ore, gold_ore → diamond — but expensive in Essence)
- [ ] `draught_of_unbinding` — potion-type item, on drink: reduces Personal Fray by 25 points (clamped at 0). Custom mob effect: `unbinding_effect`

### Materialization Recipes
Late-game quality-of-life: convert concentrated Essence back into blocks.

| Essence Type | Amount | + Water | Output |
|-------------|--------|---------|--------|
| Terra (concentrated) | 100 | yes | 16 stone |
| Terra + Metallum | 100 + 50 | no | 4 iron_ore |
| Aqua (concentrated) | 100 | yes | 4 clay_block |
| Vita (concentrated) | 100 | yes | 8 oak_log |
| Ignis (concentrated) | 100 | no | 4 magma_block |

---

## 3B.4 Research Nodes for Section 5

| Node ID | Name | Prerequisites | Unlocks | Difficulty |
|---------|------|---------------|---------|------------|
| `grinding_and_distilling` | Grinding & Distilling | a_crude_focus | Mortar & Pestle, basic Rig recipes | 2 |
| `the_reservoir` | The Reservoir | grinding_and_distilling | Reservoir recipe | 2 |
| `essence_conduits` | Essence Conduits | the_reservoir | Conduit recipe (automation) | 3 |
| `concentration` | Concentration | essence_conduits | Concentrated Essence recipe | 3 |
| `materialization` | Materialization | concentration | Essence → block recipes | 4 |
| `the_philosophers_clay` | The Philosopher's Clay | materialization | Philosopher's Clay recipe | 4 |
| `draught_of_unbinding` | Draught of Unbinding | concentration | Unbinding recipe | 3 |

---

## 3B.5 Networking

| Packet | Direction | Purpose |
|--------|-----------|---------|
| `RitualStartPacket` | S→C | Broadcast ritual start to nearby clients (particles/sound) |
| `RitualProgressPacket` | S→C | Update ritual progress for particle intensity |
| `RitualCompletePacket` | S→C | Broadcast completion burst |
| `BacklashPacket` | S→C | Broadcast backlash explosion |
| `EssenceSyncPacket` | S→C | Sync Rig/Reservoir Essence contents to client for rendering |
| `CauldronProgressPacket` | S→C | Sync Cauldron crafting progress |

---

## 3B.6 Data Generation

- [ ] Block/item models: Anchor Brazier, Ritual Pedestal, Etching Tool, ritual_etching (per pattern), Mortar & Pestle, Distillation Rig, Essence Reservoir (with color tint per Facet), Essence Conduit, Cauldron of Making, Essence Vials (colored), all consumable items, Philosopher's Clay
- [ ] Recipes: all blocks and items above
- [ ] Ritual JSON definitions
- [ ] Alchemy recipe JSON definitions
- [ ] Reagent-to-Essence mapping JSONs
- [ ] Custom enchantment registrations
- [ ] Mob effect registrations (clear_sight, unbinding)
- [ ] Sound events: grinding, distilling, ritual ambient, ritual completion, backlash explosion
- [ ] Particle types: ritual spiral, essence flow, ritual completion burst, backlash
- [ ] Language file entries

---

## 3B.7 Testing Checklist

### Rituals
- [ ] Anchor Brazier charges from wand and auto-charges near Wellsprings
- [ ] Ritual Pedestals accept and display items
- [ ] Etching Tool places valid circle patterns
- [ ] Activating a valid ritual circle starts the ritual with correct particles/sound
- [ ] Ritual consumes Wyrd from Braziers over time
- [ ] Ritual stalls (without losing progress) when Wyrd runs out
- [ ] Ritual completes and applies its result correctly
- [ ] Wrong catalysts cause backlash
- [ ] Rite of Wellspring Raising creates a functional (weaker) Wellspring
- [ ] Rite of Cleansing sets Regional Fray to 0
- [ ] Rite of Transmutation converts items correctly

### Alchemy
- [ ] Mortar & Pestle grinds items into alchemical dust
- [ ] Distillation Rig extracts Essence from dust
- [ ] Essence Vials fill and empty correctly
- [ ] Reservoirs store Essence and interact with Vials
- [ ] Conduits transfer Essence between connected blocks
- [ ] Cauldron of Making GUI shows recipes and crafts items
- [ ] Draught of Clear Sight grants passive Facet sight
- [ ] Salve of Mending heals and repairs
- [ ] Philosopher's Clay transmutes ores
- [ ] Draught of Unbinding reduces Personal Fray
- [ ] Materialization recipes produce correct blocks
- [ ] Essence colors render correctly per Facet
