# Stage 4 — Magical Equipment
## Codex Arcanum · Developer Implementation Plan

**Depends on:** Stages 1–3 (equipment recipes intentionally require components from wands, rituals, and alchemy)
**Covers:** Codex Section 6 (Magical Equipment)

---

## 4.1 Equipment Slot System

### Trinket / Accessory Slots
Minecraft doesn't natively support trinket slots (necklace, belt, charm). Two options:

- [ ] **Option A — Curios API integration**: depend on the Curios mod to provide extra equipment slots (necklace, belt, charm/trinket). This is the standard approach for Forge/NeoForge mods. Implement `ICurioItem` on each trinket.
- [ ] **Option B — Custom inventory slots**: add a custom "Magical Equipment" inventory panel accessible via keybind, with 3 extra slots (necklace, belt, charm). More self-contained but more work.

**Recommendation:** Use Curios API — it's well-established, and players expect it. Register Curios slot types: `necklace`, `belt`, `charm` (if not already registered by Curios defaults).

---

## 4.2 Equipment Items

### Attunement Amulet
- [ ] Register item: `attunement_amulet`
- [ ] Slot: necklace (Curios)
- [ ] Effect: +50 max Wyrd capacity to any held wand/staff while worn
  - Implementation: in the Wyrd capacity calculation for wands, check if the player has an Attunement Amulet equipped → add bonus
- [ ] Tiers: `attunement_amulet_lesser` (+50), `attunement_amulet` (+100), `attunement_amulet_greater` (+200)
- [ ] Recipes:
  - Lesser: 1 gold_ingot + 1 wyrdstone + 1 string (shaped as necklace)
  - Standard: 1 lesser_amulet + 2 wyrdstone + 1 wyrd_dust (ritual: Rite of Wyrdweaving circle)
  - Greater: 1 standard_amulet + 4 wyrdstone + 2 wyrd_dust + 1 diamond (ritual: Greater Circle)
- [ ] Model: 2D sprite item, rendered on player model via Curios renderer (chain around neck with glowing gem)

### Weavecloth Robes
- [ ] Register armor set items: `weavecloth_helmet`, `weavecloth_chestplate`, `weavecloth_leggings`, `weavecloth_boots`
- [ ] Armor material: custom `WEAVECLOTH` — low physical defense (between leather and chainmail), but grants:
  - Per piece: -5% spell Wyrd cost, -0.05 Personal Fray gain rate
  - Full set: -25% total Wyrd cost, -0.25 Fray gain rate (additive from 4 pieces + set bonus of -5% extra)
- [ ] Recipe per piece: Weavecloth fabric + standard armor pattern
- [ ] Weavecloth fabric: `weavecloth` — crafted at Cauldron of Making:
  - 50 Essence of Ordo + 30 Essence of Vita + 2 wool + 2 wyrd_dust → 4 weavecloth
- [ ] Model: custom armor model — robes rather than plates. Texture: deep blue with faint glowing thread lines
- [ ] Enchantable with vanilla and ritual enchantments

### Loomkeeper's Goggles
- [ ] Register item: `loomkeepers_goggles`
- [ ] Slot: head (replaces helmet)
- [ ] Effect: passive Facet sight + Weave density overlay (same effect as Draught of Clear Sight, but permanent while worn)
  - Client-side rendering: tint blocks with faint Facet-colored overlay; show Weave density as a subtle fog gradient
  - Show Facet tags on entities as floating text (like name tags but smaller)
- [ ] Recipe: 2 glass + 1 gold_ingot + 2 wyrd_dust + 1 loomkeeper_relic (loot item from Loomholds)
- [ ] Model: custom head model — brass goggles with glowing lenses

### Capacitor Belt
- [ ] Register item: `capacitor_belt`
- [ ] Slot: belt (Curios)
- [ ] Effect: portable Wyrd storage (+300 Wyrd capacity, separate from wand)
  - When casting and wand Wyrd is depleted, automatically draws from belt
  - Rechargeable: right-click on an Anchor Brazier to transfer Wyrd from Brazier → Belt
- [ ] Recipe: 2 leather + 2 wyrdstone + 1 wyrd_dust + 1 redstone (shaped)
- [ ] Visual: belt item with glowing Wyrdstone segments

### Thread-sprite Charm
- [ ] Register item: `thread_sprite_charm`
- [ ] Slot: charm (Curios)
- [ ] Effect: summons a small floating entity (`thread_sprite`) that follows the player
  - Thread-sprite behavior: moves toward nearby dropped items (within 8 blocks), picks them up, and deposits them in the player's inventory
  - Spawns when charm is equipped, despawns when removed
  - Visual: tiny (0.3 blocks) glowing construct, bobbing gently, trails thread particles
  - Passive — no combat ability, 1 HP, respawns after 30 seconds if killed
- [ ] Register entity: `thread_sprite` — extends `PathfinderMob` (or `FlyingMob`)
  - AI goals: `FollowOwnerGoal`, `PickUpItemGoal` (custom), `FloatGoal`
  - No attack goals
- [ ] Recipe (ritual): Rite of Familiar Binding — medium circle, catalysts: 1 string + 1 amethyst_shard + 2 wyrd_dust + 1 ender_pearl; central item: 1 gold_nugget → becomes thread_sprite_charm

### Warding Sigil Shield
- [ ] Register item: `warding_sigil_shield`
- [ ] Slot: off-hand (vanilla shield slot)
- [ ] Effect: passive 20% reduction of magic-sourced damage (from spells, potions, guardian beams, etc.)
  - Implementation: on `LivingHurtEvent`, if damage source is magic type and player holds this shield, reduce by 20%
  - Does NOT need to be actively blocking (passive effect)
- [ ] Also functions as a regular shield (can block physical attacks)
- [ ] Recipe: 1 shield + 2 wyrdstone + 2 wyrd_dust + 1 diamond (shaped overlay)
- [ ] Model: shield with glowing sigil pattern

### Portable Loom
- [ ] Register item: `portable_loom`
- [ ] Slot: charm (Curios)
- [ ] Effect: while equipped, pressing a keybind (suggest `L`) opens the Loom of Understanding GUI from anywhere — no need to be at the physical block
  - Uses the same menu/screen as the block version
  - Player's research data is stored on the player, not the block, so this works naturally
- [ ] Recipe: 1 loom_of_understanding + 2 ender_pearl + 4 wyrd_dust (shapeless)

### Wellspring Attunement Stone
- [ ] Register item: `wellspring_attunement_stone`
- [ ] Use: right-click on a Weave Wellspring (block interaction, not equipment slot)
  - Permanently upgrades that Wellspring:
    - +50% maxWyrd capacity
    - +30% regeneration speed
    - Works on both ancient (found) and player-built Wellsprings
  - Consumed on use
  - Wellspring block entity stores `boolean isAttuned` — can only be attuned once
  - Visual change: attuned Wellspring has brighter particles and a subtle hum sound
- [ ] Recipe (ritual): Greater Circle, catalysts: 2 wyrdstone_block + 1 diamond + 1 nether_star + 4 wyrd_dust + 2 gold_block + 1 loomkeeper_relic + 1 amethyst_block; central item: 1 wyrdstone → becomes wellspring_attunement_stone

---

## 4.3 Loomkeeper Relics (Loot Items)

These are uncraftable items found only in Loomhold chests — used as crafting ingredients for top-tier equipment.

- [ ] `loomkeeper_relic` — generic relic item, several variants:
  - `relic_lens_fragment` — used in Goggles
  - `relic_thread_spool` — used in Weavecloth, Regalia
  - `relic_core_fragment` — used in Attunement Stone
  - `relic_sigil_plate` — used in Shield, Regalia
- [ ] These are just items with lore text — no special behavior, purely crafting materials
- [ ] Added to Loomhold loot tables (Stage 5)
- [ ] Each has a custom tooltip with Thale's commentary ("I found this in the Third Circle's workshop. Still warm.")

---

## 4.4 Equipment Sets

### Novice's Set (no set bonus)
Individual unlocks from early research — Attunement Amulet (lesser), Capacitor Belt. No formal set tracking needed.

### Loomkeeper's Regalia (endgame clean set)
- [ ] Pieces: `regalia_helmet` (goggles upgrade), `regalia_chestplate`, `regalia_leggings`, `regalia_boots`, `regalia_amulet`
- [ ] Per piece: same as Weavecloth but with higher defense (chainmail-level) + stronger Wyrd cost reduction (-8% per piece)
- [ ] **Set bonus (3+ pieces)**: -10% additional Wyrd cost + passive Personal Fray decay (+1 decay per minute, stacks with natural decay)
- [ ] Set bonus detection: on tick, count equipped Regalia pieces; apply bonus as mob effect or attribute modifier
- [ ] Recipes: each piece requires a Weavecloth equivalent + Loomkeeper relics + ritual at Greater Circle
- [ ] Model: ornate robes with golden thread accents, glowing Weave patterns

### Duskbound Set (Forbidden-tier, see Stage 5 for full implementation)
- [ ] Register items here as placeholders but recipes/unlock gated behind Stage 5 research
- [ ] Pieces: `duskbound_helmet`, `duskbound_chestplate`, `duskbound_leggings`, `duskbound_boots`
- [ ] Per piece: highest raw stats in the mod (diamond-level defense + strong spell bonuses)
- [ ] **Set bonus (3+ pieces)**: continuous passive Personal Fray drain (+0.5 per minute while worn)
- [ ] Visual: dark, desaturated armor with shifting shadow particles

---

## 4.5 Research Nodes for Section 6

| Node ID | Name | Prerequisites | Unlocks | Difficulty |
|---------|------|---------------|---------|------------|
| `wearable_attunement` | Wearable Attunement | pedestal_attunement | Attunement Amulet recipes | 3 |
| `weavecloth` | Weavecloth | wearable_attunement, concentration | Weavecloth fabric + Robes recipes | 3 |
| `the_capacitor_belt` | The Capacitor Belt | wearable_attunement | Capacitor Belt recipe | 3 |
| `binding_a_familiar` | Binding a Familiar | the_capacitor_belt, rite_of_transmutation | Thread-sprite Charm (ritual recipe) | 4 |
| `wellspring_mastery` | Wellspring Mastery | greater_circles | Attunement Stone (ritual recipe) | 4 |
| `loomkeepers_regalia` | Loomkeeper's Regalia | wellspring_mastery, weavecloth | Regalia reconstruction recipes | 4 |
| `duskbound_craft` | Duskbound Craft | loomkeepers_regalia + Stage 5 `dark_essence` | Duskbound set recipes | 5 |

---

## 4.6 Data Generation

- [ ] Item models: all equipment items, relic items, Weavecloth fabric, Thread-sprite entity
- [ ] Armor models/textures: Weavecloth set, Regalia set, Duskbound set (placeholder textures for now)
- [ ] Entity model: Thread-sprite (small floating construct)
- [ ] Recipes: all crafting and ritual recipes
- [ ] Curios slot registration and rendering
- [ ] Loot table entries for relics (added to Loomhold chests in Stage 5)
- [ ] Sound events: Thread-sprite ambient hum, Attunement Stone application, Regalia equip chime
- [ ] Language file entries
- [ ] Research node JSONs with lore text (Thale referencing Loomhold ruins as sources)

---

## 4.7 Testing Checklist

- [ ] Curios slots appear in player inventory (necklace, belt, charm)
- [ ] Attunement Amulet correctly adds Wyrd capacity to held wand
- [ ] Weavecloth Robes reduce spell Wyrd cost and Fray gain
- [ ] Loomkeeper's Goggles show Facet/Weave density overlay
- [ ] Capacitor Belt stores Wyrd and auto-feeds wand when empty
- [ ] Thread-sprite spawns when charm equipped and collects nearby items
- [ ] Thread-sprite despawns when charm removed and respawns if killed
- [ ] Warding Sigil Shield reduces magic damage by 20%
- [ ] Portable Loom opens research GUI from anywhere
- [ ] Wellspring Attunement Stone upgrades a Wellspring correctly (once only)
- [ ] Regalia set bonus activates with 3+ pieces
- [ ] Duskbound set applies passive Fray drain
- [ ] Equipment tooltips show all stat bonuses and set bonus info
- [ ] Relic items appear in Loomhold loot (tested with Stage 5)
