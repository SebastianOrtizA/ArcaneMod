# Stage 5 — Forbidden Knowledge & Loomholds
## Codex Arcanum · Developer Implementation Plan

**Depends on:** Stages 0–4 (Fray system from Stage 0, alchemy from Stage 3, equipment from Stage 4)
**Covers:** Codex Section 7 (Forbidden Knowledge) + §10 (Loomhold dungeon generation)

---

## PART A — LOOMHOLD STRUCTURES

---

## 5A.1 Structure Generation System

### Structure Templates
Loomholds are the mod's dungeons. Build them as Jigsaw structures using structure templates (SNBT files) for modularity.

- [ ] Create a `codexarcanum:loomhold` structure set with three tiers
- [ ] Use `JigsawStructure` for procedural generation from template pools

### Loomhold Outposts (Small, Surface/Shallow)
- [ ] Size: 5–10 rooms, single floor or shallow underground
- [ ] Template pool: `codexarcanum:loomhold_outpost`
  - Room types: entry hall, storage room, small study, collapsed corridor, trapped room
  - 3–5 room variants per type for variety
- [ ] Worldgen placement:
  - Biomes: forest, taiga, plains, mountains (anywhere with Weave density > 0.4)
  - Spacing: 24 chunks minimum between Outposts
  - Separation: 8 chunks
- [ ] Features:
  - Light puzzle/trap content: pressure plates → arrow dispensers, hidden chests behind pistons
  - 1–2 Weave Wellsprings nearby (within 3 chunks)
  - Boosted Weave density in structure chunks (+0.15)
- [ ] Loot tables: `codexarcanum:loomhold_outpost_chest`
  - Common: Wyrd Dust (2–6), Wyrdstone (1–3), vanilla valuables
  - Uncommon: Loomkeeper's Wand (pre-assembled), relic_thread_spool
  - Rare: Sable Page (5% chance — intentionally low, more common in Towers)
- [ ] Mob spawning: Frayed mobs spawn in dark rooms (use vanilla spawner blocks placed by structure)

### Loomhold Towers (Medium, Vertical)
- [ ] Size: 15–25 rooms, multi-floor vertical structure, partially collapsed
- [ ] Template pool: `codexarcanum:loomhold_tower`
  - Room types: spiral staircase, library (bookshelves + lecterns), laboratory (alchemy equipment ruins), vault door (locked, needs key/puzzle), observation platform (open roof)
  - Collapsed sections: some floors have gaps/rubble, requiring creative traversal
- [ ] Worldgen placement:
  - Biomes: dark_forest, stony_peaks, deep_dark-adjacent
  - Spacing: 48 chunks minimum
  - Separation: 16 chunks
- [ ] Features:
  - Denser Weave (+0.25 in structure chunks)
  - Frayed mob spawners + custom mob: `unwoven_researcher` (see 5A.3)
  - Mid-tier puzzle: locked vault doors that require finding a Wyrdstone Key item elsewhere in the structure
- [ ] Loot tables: `codexarcanum:loomhold_tower_chest`
  - Common: Wyrd Dust (4–10), Wyrdstone (3–6), relic items
  - Uncommon: relic_lens_fragment, relic_sigil_plate, named wand loot
  - Notable: Sable Page (25% per chest — first reliable source)
  - Rare: relic_core_fragment

### The Last Circle (Unique, Endgame)
- [ ] Size: 30–40 rooms, massive underground complex
- [ ] Generation: unique structure — only ONE generates per world
  - Placed 1000–3000 blocks from world spawn
  - In a high-density biome (mushroom_fields, dark_forest, or deep_dark)
  - The Codex gives a compass-like hint toward it in late-game pages
- [ ] Template pool: `codexarcanum:the_last_circle`
  - Room types: grand atrium, archive (filled with empty lecterns + torn books), ritual chamber (broken circles), inner sanctum (boss arena), deep vault
  - Architecture: grander, more intact than other Loomholds, but with visible Fray corruption (desaturated textures, Frayed blocks)
- [ ] Features:
  - Maximum Weave density (1.0) + heavy Regional Fray (60+ at generation)
  - Multiple Unwoven enemies + the Warden of the Last Circle boss (Stage 6)
  - Final Sable Pages
  - Loomkeeper's Regalia relic pieces
  - Duskbrand Staff, Hollow Crown
- [ ] Loot tables: `codexarcanum:the_last_circle_chest`, `codexarcanum:the_last_circle_vault`
  - Vault: guaranteed relic drops, Sable Pages, endgame equipment

---

## 5A.2 Custom Blocks for Structures

- [ ] `loomhold_stone` — base building block for Loomholds (custom texture: carved stone with faint Weave patterns)
- [ ] `loomhold_stone_bricks` — brick variant
- [ ] `cracked_loomhold_bricks` — damaged variant
- [ ] `frayed_loomhold_stone` — corrupted variant (desaturated, dark veins, found in high-Fray areas of structures)
- [ ] `loomhold_pillar` — decorative column
- [ ] `sealed_door` — locked door block that requires `wyrdstone_key` item to open (right-click interaction)
- [ ] `wyrdstone_key` — found in Loomhold chests, consumed on use to open a sealed door
- [ ] `loomkeeper_lectern` — decorative block, sometimes contains a book with lore text when right-clicked
- [ ] `loomhold_light` — glowing block (light level 12), faint blue-white glow, used throughout structures

All blocks: standard drops (silk touch = self, else nothing for decorative), mineable with pickaxe.

---

## 5A.3 Unwoven Mobs

### Unwoven Researcher
- [ ] Register entity: `unwoven_researcher`
- [ ] Appearance: humanoid, wearing tattered Loomkeeper robes, semi-transparent with a glitchy/flickering effect
- [ ] Behavior:
  - Wanders slowly, muttering (ambient sound: whispering/mumbling)
  - Passive until provoked or player enters within 4 blocks
  - When hostile: casts a slow-moving dark bolt (similar to Spark but deals magic damage + adds 2 Personal Fray on hit)
  - HP: 30, Armor: 4, Speed: slow
- [ ] Drops: Wyrd Dust (1–3), occasionally a Sable Page fragment or relic
- [ ] Spawn: only in Loomhold structures via spawners or natural structure spawning

### Unwoven Sentinel
- [ ] Register entity: `unwoven_sentinel`
- [ ] Appearance: large humanoid in broken armor, Frayed visual distortion
- [ ] Behavior:
  - Guards specific rooms (vault doors, boss arenas)
  - Melee fighter: heavy hits (8 damage), slow but tanky
  - On hit: knockback + 1 Personal Fray to the player
  - HP: 60, Armor: 10
- [ ] Drops: relic_sigil_plate (25%), Wyrdstone (2–4)

---

## PART B — FORBIDDEN KNOWLEDGE (Section 7)

---

## 5B.1 Sable Pages (Lore & Unlock Items)

- [ ] Register item: `sable_page`
- [ ] Properties:
  - Found as loot in Loomhold chests (see loot tables above)
  - On right-click (while holding Codex in inventory): unlocks the Forbidden Knowledge section in the Codex
  - First page unlocks the section + `a_warning_ignored` research node
  - Subsequent pages unlock deeper research nodes (tracked by count: page 2 → `dark_essence`, page 3 → `wraithbind`, etc.)
- [ ] Player data: `int sablePagesFound` — tracked in `PlayerResearchData`
- [ ] Codex UI change: when Section 7 is unlocked, it appears with a distinct visual style:
  - Torn page edges (custom GUI texture)
  - Different ink color (dark red/brown)
  - Thale's margin warnings rendered in a lighter, handwritten-style font

---

## 5B.2 Blightened Rig

- [ ] Register block: `blightened_rig`
- [ ] Created by: right-clicking a normal `distillation_rig` with `gravemarrow_powder` — irreversible, one-way conversion
  - Show a confirmation dialog/warning before converting ("This cannot be undone. The Rig will only produce Dark Essence from now on.")
- [ ] Block entity: `BlightenedRigBlockEntity` (extends or replaces `DistillationRigBlockEntity`)
  - Input: same as normal rig — alchemical dust
  - But ONLY processes death/decay-aligned materials and ONLY produces Dark Essence
  - Valid inputs: bone, rotten_flesh, gunpowder, spider_eye, fermented_spider_eye, wither_rose, soul_sand, ghast_tear, phantom_membrane
- [ ] Visual: same as Distillation Rig but with dark particles, desaturated texture, faint shadow aura

### Dark Essence
- [ ] Register essence type: `dark_essence` (maps to TENEBRAE Facet)
- [ ] Stored the same way as regular Essence (in Reservoirs, Vials, Conduits)
- [ ] Vial color: deep purple/black
- [ ] Used as input for all Forbidden recipes

---

## 5B.3 Forbidden Items

### Bonecarved Wand Core
- [ ] Register item: `bonecarved_core`
- [ ] Stats: capacity 120, efficiency 0.7× (cheap to cast)
- [ ] Passive: +0.1 Personal Fray per minute while wand is equipped
- [ ] Recipe: 4 bone + 2 dark_essence_vial + 1 wyrd_dust (at Wandwright's Bench)

### Sable Cap
- [ ] Register item: `sable_cap`
- [ ] Effect: +30% damage on Umbra/Mortis-Faceted spells
- [ ] Passive: +0.1 Personal Fray per minute while wand is equipped
- [ ] Recipe: 2 obsidian + 1 dark_essence_vial + 1 wyrd_dust (at Wandwright's Bench)

### Bloodwood Core
- [ ] Register item: `bloodwood_core`
- [ ] Stats: capacity 500 (very high), efficiency 0.85× (good)
- [ ] Passive: +0.15 Personal Fray per minute
- [ ] Scales with Umbra/Mortis spells: +10% damage per spell's Umbra/Mortis Facet count
- [ ] Recipe: 2 crimson_stem + 2 dark_essence_vial + 1 nether_star + 2 wyrd_dust

### Gravemarrow Powder
- [ ] Register item: `gravemarrow_powder`
- [ ] Crafted at Mortar & Pestle from: 4 bone + 2 rotten_flesh → 4 gravemarrow_powder
- [ ] Used as catalyst for tainting rigs + Forbidden ritual catalysts

### Wither-thread Catalyst
- [ ] Register item: `wither_thread_catalyst`
- [ ] Recipe: 2 dark_essence_vial + 1 nether_star + 2 gravemarrow_powder (at Cauldron of Making)
- [ ] Use: required catalyst for the most powerful Forbidden rituals
- [ ] On use: adds 15 Regional Fray to the chunk (large one-time cost)

---

## 5B.4 Forbidden Spells

### Wraithbind
- [ ] Register spell: `wraithbind`
- [ ] Category: CURSE
- [ ] Facets: UMBRA, MORTIS
- [ ] Effect: drains 4 HP/second from target, heals caster for 50% of damage dealt
- [ ] Duration: 5 seconds (total 20 damage dealt, 10 healed)
- [ ] Wyrd cost: 25, Cooldown: 100 ticks
- [ ] Fray cost: +3 Personal Fray per cast
- [ ] Visual: dark tether beam connecting caster to target, target has draining particle effect, caster glows faintly with stolen life
- [ ] Range: 12 blocks, requires line of sight maintained for full duration (breaks if target moves behind cover)

### The Unmaker's Whisper (Capstone)
- [ ] Register spell: `unmakers_whisper`
- [ ] Category: ENCHANT (self-buff)
- [ ] Facets: TENEBRAE (UMBRA + MORTIS compound)
- [ ] Effect: 30-second buff granting:
  - +50% all spell damage
  - +25% Wyrd efficiency (costs 25% less)
  - Immunity to knockback
  - Faint shadow clone visual following the player
- [ ] Wyrd cost: 100, Cooldown: 6000 ticks (5 minutes)
- [ ] Fray cost: +15 Personal Fray (large spike)
- [ ] Requires: specific lore item found in The Last Circle to unlock the Focus recipe

---

## 5B.5 Forbidden Equipment

### Duskbound Set (complete implementation — registered in Stage 4)
- [ ] Recipes (all at Greater Circle ritual):
  - `duskbound_helmet`: 1 netherite_helmet + 4 dark_essence_vial + 2 wither_thread_catalyst + relic_sigil_plate
  - `duskbound_chestplate`: 1 netherite_chestplate + 6 dark_essence_vial + 2 wither_thread_catalyst + relic_thread_spool
  - `duskbound_leggings`: 1 netherite_leggings + 5 dark_essence_vial + 2 wither_thread_catalyst + relic_sigil_plate
  - `duskbound_boots`: 1 netherite_boots + 4 dark_essence_vial + 1 wither_thread_catalyst + relic_sigil_plate
- [ ] Stats per piece: netherite-level defense + additional -10% spell Wyrd cost + +15% spell damage
- [ ] Set bonus (3+ pieces): +0.5 Personal Fray per minute while worn (continuous drain — the tradeoff)
- [ ] Visual corruption: at Personal Fray > 50, the armor slowly shifts texture (darker, more distorted) — purely cosmetic

### Hollow Crown (Endgame Relic)
- [ ] Register item: `hollow_crown`
- [ ] Slot: head (replaces helmet)
- [ ] Stats: +40% spell damage, +200 max Wyrd to held wand, +20% Wyrd efficiency
- [ ] Cost: continuous +1.0 Personal Fray per minute while worn (the heaviest Fray item in the mod)
- [ ] Found: The Last Circle vault only (not craftable)
- [ ] Visual: a crown that appears to be made of solidified shadow, faintly flickering

---

## 5B.6 Forbidden Rituals

| Ritual | Circle | Catalysts | Wyrd | Result | Fray |
|--------|--------|-----------|------|--------|------|
| Rite of Dark Binding | medium | 4 gravemarrow_powder + 2 dark_essence_vial + 1 soul_sand + 1 wither_rose | 400 | Enchant: weapon drains 1 HP per hit to caster | +5 Regional |
| Rite of Shadow Forging | large | 4 dark_essence_vial + 2 wither_thread_catalyst + relic items | 800 | Craft Duskbound equipment | +10 Regional |
| Rite of Unmaking | large | 4 wither_thread_catalyst + 4 dark_essence_vial + 1 nether_star | 1200 | Destroy any single block instantly (including bedrock-hardness) | +20 Regional, +5 Personal |

---

## 5B.7 Research Nodes for Section 7

| Node ID | Name | Prerequisites | Unlocks | Difficulty | Trigger |
|---------|------|---------------|---------|------------|---------|
| `a_warning_ignored` | A Warning Ignored | 1st Sable Page | Section 7 visible in Codex | 1 | Reading first Sable Page |
| `tainting_the_rig` | Tainting the Rig | a_warning_ignored | Blightened Rig conversion | 3 | Auto after node |
| `dark_essence` | Dark Essence | tainting_the_rig, 2nd Sable Page | Dark Essence extraction recipes | 3 | 2nd page + research |
| `wraithbind` | Wraithbind | dark_essence | Wraithbind Focus recipe | 4 | Research at Loom |
| `the_sable_pages` | The Sable Pages | wraithbind, 3rd+ Sable Pages | Bloodwood Core, Sable Cap, Duskbound Craft | 5 | Finding pages 3–5 |
| `unmakers_whisper` | The Unmaker's Whisper | the_sable_pages + Last Circle lore item | Capstone spell Focus | 5 | End of Section 7 |

---

## 5B.8 Data Generation

- [ ] Structure templates (SNBT): Outpost rooms (~15 variants), Tower rooms (~20 variants), Last Circle rooms (~25 variants)
- [ ] Structure sets and jigsaw config JSONs
- [ ] Loot tables: per-tier chests and vaults
- [ ] Custom block models/textures: all loomhold blocks
- [ ] Entity models/textures/AI: Unwoven Researcher, Unwoven Sentinel
- [ ] Item models: Sable Page, Gravemarrow Powder, Wither-thread Catalyst, Bonecarved Core, Bloodwood Core, Sable Cap, Hollow Crown
- [ ] Duskbound armor textures (dark, shifting)
- [ ] Spell definitions: Wraithbind, Unmaker's Whisper
- [ ] Ritual definitions: all Forbidden rituals
- [ ] Sound events: Unwoven ambient muttering, Wraithbind drain, Unmaker's Whisper activation, structure ambient sounds
- [ ] Particle types: Unwoven flicker, dark bolt, Wraithbind tether, shadow clone
- [ ] Language file entries
- [ ] Research node JSONs with lore text (torn-page voice for the Forbidden author, Thale's margin warnings)

---

## 5B.9 Testing Checklist

### Structures
- [ ] Loomhold Outposts generate in valid biomes at correct spacing
- [ ] Loomhold Towers generate with multi-floor layout
- [ ] The Last Circle generates uniquely, once per world
- [ ] All structure blocks place correctly and look right
- [ ] Loot tables produce correct items
- [ ] Sealed doors open with Wyrdstone Keys
- [ ] Unwoven mobs spawn and fight correctly
- [ ] Structure chunks have boosted Weave density and Fray

### Forbidden Knowledge
- [ ] Sable Page unlocks Section 7 in the Codex
- [ ] Subsequent pages unlock deeper research nodes
- [ ] Blightened Rig conversion is one-way and produces Dark Essence
- [ ] Dark Essence only accepts valid death/decay materials
- [ ] Bonecarved Core, Sable Cap, Bloodwood Core work in wands with correct stats
- [ ] Passive Fray from Forbidden equipment accumulates correctly
- [ ] Wraithbind spell drains and heals correctly
- [ ] Unmaker's Whisper grants the power buff with heavy Fray cost
- [ ] Duskbound set crafts at Greater Circle and applies set bonus
- [ ] Hollow Crown is found in Last Circle vault with correct stats
- [ ] Forbidden rituals execute and apply Regional Fray costs
- [ ] Codex Section 7 pages have distinct torn/dark visual style
