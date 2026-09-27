# Stage 2 — Sorcery
## Codex Arcanum · Developer Implementation Plan

**Depends on:** Stage 1 (Introduction & Wands) — player needs a wand with at least one Focus slot
**Covers:** Codex Section 3 (Sorcery)

---

## 2.1 Spell System Architecture

### Spell Registry
- [ ] Create a `Spell` data class:
  ```java
  public record Spell(
      ResourceLocation id,
      String displayName,
      SpellCategory category,
      int wyrdCost,
      int cooldownTicks,
      float baseDamage,       // 0 for non-damage spells
      float range,
      int duration,           // 0 for instant
      boolean channeled,      // true = staff-only
      List<Facet> requiredFacets,
      ResourceLocation focusRecipe
  ) {}
  ```
- [ ] Create `SpellCategory` enum:
  ```
  BOLT, NOVA, WALL, UTILITY, MOVEMENT, ENCHANT, CURSE
  ```
- [ ] Create `SpellRegistry` — loads spell definitions from JSON:
  ```
  data/codexarcanum/spells/*.json
  ```
- [ ] Example spell JSON:
  ```json
  {
    "id": "codexarcanum:emberlance",
    "display_name": "Emberlance",
    "category": "bolt",
    "wyrd_cost": 15,
    "cooldown_ticks": 30,
    "base_damage": 8.0,
    "range": 24.0,
    "duration": 0,
    "channeled": false,
    "required_facets": ["ignis", "motus"],
    "focus_recipe": "codexarcanum:focus_emberlance"
  }
  ```

### Spell Casting Flow
- [ ] When the player right-clicks with a wand/staff:
  1. Get the active Focus from the wand's data
  2. Look up the Focus's spell ID in SpellRegistry
  3. Check: enough Wyrd? Cooldown expired? Focus has charges?
  4. Execute the spell's effect (see per-spell implementations below)
  5. Deduct Wyrd from wand, decrement Focus charges, start cooldown
  6. Apply cast efficiency modifier from wand core
  7. Apply cap modifiers (Prism Cap splits, Quartz Cap range boost, etc.)
- [ ] Cooldown tracking: per-player map of `spellId → lastCastTick`, checked before casting
- [ ] Network: send `SpellCastPacket` (C→S) with spell ID and target info; server validates and executes; send `SpellEffectPacket` (S→C) for client-side particles/sounds

### Cap Modifier Integration
- [ ] Copper Cap: reduce cooldown by 10%
- [ ] Prism Cap: after the primary spell hits, clone it at 50% damage to up to 2 additional nearby targets within 5 blocks
- [ ] Quartz Cap: multiply spell range by 1.5
- [ ] Iron Cap: no spell modifier (durability only)

---

## 2.2 Spell Implementations

### Bolt Spells

#### Spark (basic — already partially in Stage 1)
- [ ] Finalize entity: `spark_bolt`
- [ ] Damage: 3, Range: 16, Wyrd cost: 5, Cooldown: 10 ticks
- [ ] Facets: IGNIS
- [ ] Projectile: fast, small, orange particle trail
- [ ] On hit: fire particles, damage entity, no block interaction

#### Emberlance
- [ ] Register entity: `emberlance_bolt`
- [ ] Damage: 8, Range: 24, Wyrd cost: 15, Cooldown: 30 ticks
- [ ] Facets: IGNIS, MOTUS
- [ ] Projectile: longer, faster, leaves a trail of ember particles
- [ ] On hit: damage + 3-second burn effect (like Fire Aspect)
- [ ] Visual: distinct from Spark — more like a fiery spear than a small ball

### Nova Spells

#### Emberburst
- [ ] Damage: 6 (AoE), Radius: 4 blocks, Wyrd cost: 25, Cooldown: 60 ticks
- [ ] Facets: IGNIS × 2 (need double Ignis Facet knowledge)
- [ ] Mechanic: on cast, create an expanding ring of fire particles centered on the target point (raycast impact or caster if no target)
- [ ] All entities within radius take damage + short burn
- [ ] Blocks within radius briefly show fire particles (cosmetic, not actual fire unless config allows)
- [ ] Sound: a deep "whump" explosion

### Wall Spells

#### Aegis of Threads
- [ ] Wyrd cost: 20, Duration: 10 seconds, Cooldown: 120 ticks
- [ ] Facets: ORDO, TERRA
- [ ] Mechanic: creates a 3-wide × 3-tall barrier of translucent magical blocks in front of the caster
  - Barrier blocks: register `aegis_barrier` — non-solid to the caster, solid to everything else
  - Barrier has HP (absorbs 30 damage total before breaking)
  - Fades with a dissolving particle effect when duration expires or HP depleted
  - Block entity tracks remaining HP and duration; tick to decrement
- [ ] Visual: shimmering, thread-like texture, semi-transparent

### Utility Spells

#### Excavate
- [ ] Wyrd cost: 10, Cooldown: 20 ticks
- [ ] Facets: TERRA, MOTUS
- [ ] Mechanic: instantly break a 3×3×1 area of blocks in front of the caster (facing direction)
  - Only breaks blocks with hardness ≤ stone (no obsidian, no ores harder than iron)
  - Drops items normally (uses `Block.playerWillDestroy` for each)
  - Tool-level: equivalent to iron pickaxe
- [ ] Sound: crumbling stone effect

#### Clarity
- [ ] Wyrd cost: 15, Cooldown: 200 ticks
- [ ] Facets: VITA, LUX
- [ ] Mechanic: instantly clears all negative potion effects from the caster
  - Iterate `player.getActiveEffects()`, remove those where `effect.isBeneficial() == false`
  - Flash of white particles around the player

### Movement Spells

#### Stepthread
- [ ] Wyrd cost: 12, Cooldown: 40 ticks
- [ ] Facets: MOTUS, AER
- [ ] Mechanic: short-range blink (12 blocks in look direction)
  - Raycast 12 blocks in the player's look direction
  - Teleport to the first solid surface found (or max distance if open air)
  - Ensure destination is safe: 2-block air gap above the landing point
  - If no safe landing found, spell fails (Wyrd not consumed, cooldown not triggered)
  - Sound: whoosh + thread-snapping sound
  - Particles: thread-like lines from origin to destination, fading quickly

### Enchant Spells

#### Warding Word
- [ ] Wyrd cost: 18, Duration: 15 seconds, Cooldown: 300 ticks
- [ ] Facets: ORDO, VITA
- [ ] Mechanic: grants self (or targeted ally within 8 blocks) a custom effect:
  - Register mob effect: `warding_word_effect`
  - Effect: 30% damage reduction from all sources
  - Visual: faint golden shield particles orbiting the buffed entity
- [ ] Targeting: if looking at an ally entity within range, apply to them; otherwise apply to self

### Curse Spells

#### Binding Word
- [ ] Wyrd cost: 14, Duration: 4 seconds, Cooldown: 80 ticks
- [ ] Facets: TERRA, PERDO
- [ ] Mechanic: roots the target mob in place
  - Register mob effect: `binding_word_effect`
  - Effect: sets movement speed to 0, prevents jumping, prevents knockback
  - Visual: dark tendrils rising from ground around the target's feet
  - Breaks early if target takes > 10 damage while rooted
- [ ] Target: raycast, single entity, range 16

---

## 2.3 Spell Chaining

### Mechanic
- [ ] Staff foci can be configured to fire in sequence:
  - If a staff has foci in slots [A, B, C], the player can toggle "chain mode" (keybind)
  - In chain mode: right-click fires Focus A, then auto-queues Focus B after a 0.5-second window, then C
  - Each spell in the chain costs its own Wyrd
- [ ] Named combos: specific pairs/triples that produce a bonus effect:
  - Define combos in JSON: `data/codexarcanum/spell_chains/*.json`
  ```json
  {
    "id": "cascading_flame",
    "display_name": "Cascading Flame",
    "sequence": ["codexarcanum:spark", "codexarcanum:emberburst"],
    "bonus": "damage_multiplier",
    "bonus_value": 1.3,
    "description": "The initial Spark ignites the Emberburst for 30% more damage"
  }
  ```
- [ ] On combo detection:
  - Display combo name as a title overlay ("Cascading Flame!")
  - Apply the bonus effect to the final spell in the chain
  - Play a satisfying chime sound

### Combos to Define
| Combo Name | Sequence | Bonus |
|-----------|----------|-------|
| Cascading Flame | Spark → Emberburst | +30% Emberburst damage |
| Threaded Strike | Stepthread → Emberlance | Emberlance pierces through first target |
| Warded Advance | Warding Word → Stepthread | Stepthread leaves a brief Aegis barrier at origin |
| Binding Flame | Binding Word → Emberlance | Rooted target takes +50% from the Emberlance |

---

## 2.4 Focus Recipes (per spell)

Each spell needs a Focus recipe at the Wandwright's Bench:

| Spell | Focus Recipe |
|-------|-------------|
| Spark | 1 amethyst_shard + 1 coal + 1 wyrd_dust |
| Emberlance | 1 amethyst_shard + 1 blaze_powder + 2 wyrd_dust |
| Emberburst | 1 amethyst_shard + 2 blaze_powder + 3 wyrd_dust |
| Aegis of Threads | 1 amethyst_shard + 1 iron_ingot + 1 cobblestone + 2 wyrd_dust |
| Excavate | 1 amethyst_shard + 1 iron_pickaxe + 2 wyrd_dust |
| Stepthread | 1 amethyst_shard + 1 ender_pearl + 2 wyrd_dust |
| Clarity | 1 amethyst_shard + 1 golden_apple + 2 wyrd_dust |
| Warding Word | 1 amethyst_shard + 1 golden_ingot + 2 wyrd_dust |
| Binding Word | 1 amethyst_shard + 1 cobweb + 1 iron_ingot + 2 wyrd_dust |

- [ ] Register all Focus recipes as custom recipe types at the Wandwright's Bench
- [ ] Each Focus item stores the spell ID it was crafted for

---

## 2.5 Research Nodes for Section 3

| Node ID | Name | Prerequisites | Required Facets | Unlocks | Difficulty |
|---------|------|---------------|----------------|---------|------------|
| `spark_mastery` | Spark Mastery | focus_crafting | IGNIS | Spark Focus recipe | 2 |
| `excavate` | Excavate | spark_mastery | TERRA, MOTUS | Excavate Focus recipe | 2 |
| `stepthread` | Stepthread | excavate | MOTUS, AER | Stepthread Focus recipe | 2 |
| `emberlance` | Emberlance | spark_mastery | IGNIS, MOTUS | Emberlance Focus recipe | 3 |
| `clarity` | Clarity | stepthread | VITA, LUX | Clarity Focus recipe | 3 |
| `aegis_of_threads` | Aegis of Threads | clarity | ORDO, TERRA | Aegis Focus recipe | 3 |
| `warding_word` | Warding Word | aegis_of_threads | ORDO, VITA | Warding Word Focus recipe | 3 |
| `emberburst` | Emberburst | emberlance | IGNIS × 2 | Emberburst Focus recipe | 3 |
| `binding_word` | Binding Word | warding_word | TERRA, PERDO | Binding Word Focus recipe | 3 |
| `spell_chaining` | Spell Chaining | emberburst, binding_word | MOTUS, ORDO | Chain mode + combo list | 4 |

- [ ] Write JSON research data for all nodes
- [ ] Write lore text (Thale's spellbook voice — anecdotes about surviving Loomhold collapses)

---

## 2.6 Networking

| Packet | Direction | Purpose |
|--------|-----------|---------|
| `SpellCastPacket` | C→S | Player attempts to cast spell (spell ID, target info) |
| `SpellEffectPacket` | S→C | Broadcast spell visual/sound to nearby clients |
| `ComboTriggerPacket` | S→C | Combo detected — show combo name overlay |
| `CooldownSyncPacket` | S→C | Sync remaining cooldowns to client for HUD display |
| `ChainModeTogglePacket` | C→S | Player toggled chain mode on/off |

---

## 2.7 HUD Elements

- [ ] **Wyrd bar**: show current Wyrd / max Wyrd of held wand, above or beside hotbar
- [ ] **Active Focus indicator**: show which Focus is selected and its remaining charges
- [ ] **Cooldown overlay**: dim the Focus icon when on cooldown, show remaining seconds
- [ ] **Chain mode indicator**: when chain mode is active, show a small "chain" icon
- [ ] **Combo popup**: when a combo triggers, show the name as a title text for 2 seconds

---

## 2.8 Data Generation

- [ ] Spell JSON definitions for all 9 spells
- [ ] Spell chain/combo definitions
- [ ] Focus recipes
- [ ] Research node JSON entries
- [ ] Entity renderers: spark_bolt, emberlance_bolt
- [ ] Mob effects: warding_word_effect, binding_word_effect
- [ ] Sound events: spell casts (per category), combo chime, Stepthread whoosh
- [ ] Particle types: ember trail, fire burst, barrier shimmer, thread lines, binding tendrils
- [ ] Language file entries

---

## 2.9 Testing Checklist

- [ ] Each spell can be researched at the Loom in order
- [ ] Each spell's Focus can be crafted at the Wandwright's Bench
- [ ] Foci can be slotted into wands and staves
- [ ] Each spell fires correctly and its effect works as described
- [ ] Wyrd is deducted on cast; cooldowns are enforced
- [ ] Cap modifiers apply: Prism splits, Quartz extends range, Copper speeds cooldown
- [ ] Spell chaining works on staves with multiple foci
- [ ] Named combos trigger and display the combo name
- [ ] Channeled spells (future) only work on staves
- [ ] Aegis barrier blocks damage from enemies but not the caster
- [ ] Stepthread safely teleports without suffocating the player
- [ ] Binding Word roots targets and breaks on sufficient damage
- [ ] HUD displays Wyrd bar, active Focus, and cooldowns
