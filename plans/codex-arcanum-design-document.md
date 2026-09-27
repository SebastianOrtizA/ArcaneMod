# CODEX ARCANUM
### A Game & Story Design Document
*A magic mod for Minecraft Java, in the spirit of Thaumcraft 4.2*

---

## 0. How to Use This Document

This is the master design bible for the mod. It defines the world, the core magic
system, and full designs for all seven sections of the in-game guidebook, the
**Codex Arcanum**. Everything here — names, mechanics, items, spells, rituals,
recipes, and research nodes — is meant to be handed off as source material for
per-stage implementation plans (blocks/items to register, GUIs to build, data to
author, models/textures to commission, etc.).

Section 12 ("From Page to Plan") explicitly maps this document onto a sequence of
development stages, so it can be used directly for that purpose.

A note on scope: mechanic names below (e.g. "the Weave," "Wyrd," "Fray") are
original terminology invented for this mod. They are structurally similar to
concepts from Thaumcraft (aura, vis, warp) because that is the stated inspiration,
but nothing here reuses Thaumcraft's specific names, art, or code — treat this as
a spiritual successor with its own identity.

---

## 1. The World & the Central Story

### 1.1 Premise

Long before the player's world was mapped, a civilization known as the
**Loomkeepers** learned to perceive and shape **the Weave** — the unseen fabric
that threads through every living and dead thing, every block and biome. They
raised towers called **Loomholds** to study it, wove its raw energy (**Wyrd**)
into wands, rituals, and machines, and reached a height of magical mastery the
world has not seen since.

They also went too far.

In their final age, a faction of Loomkeepers began experimenting with the
Weave's darker threads — the ones tied to death, decay, and unmaking — hoping to
weave *permanence* itself: deathless bodies, unbreakable constructs, undoing of
consequence. What they wove instead was **the Unmaker**: not a creature so much
as an idea that entered the Weave and began quietly unpicking it, thread by
thread, everywhere at once.

The Loomholds fell. Some Loomkeepers were unmade outright. Others were changed —
hollowed out, still moving, still muttering research notes to nobody — into what
the Codex later calls **the Unwoven**. The rest fled or hid their knowledge,
scattering it across the world in warded chests, sealed libraries, and — most
importantly for the player — in personal journals.

### 1.2 The Codex Arcanum

The player begins by finding one such journal: a battered, half-finished book
belonging to a Loomkeeper researcher named **Maren Thale**. Thale was not among
the faction that summoned the Unmaker; she was one of the last to keep studying
the "clean" arts after the fall, trying to understand what had happened and
whether it could be undone or contained. Her journal is written as a teaching
text — as if she always intended for someone to pick up where she left off.

That journal is the **Codex Arcanum**: the player's guidebook, research log, and
the spine of the whole mod. As the player researches new knowledge, blank pages
in the Codex fill themselves in with Thale's (procedurally-assembled) notes,
sketches, and warnings — giving every unlock a small narrative beat instead of
being a bare unlock screen.

The Codex's seven sections map onto Thale's own journey, in order: she started
by relearning the basics (**Introduction**), rebuilt the practitioner's basic
toolkit (**Wands**), relearned how to actively cast (**Sorcery**), recovered
group/ritual magic from Loomhold ruins (**Rituals**), rebuilt the material
science of magic (**Alchemy**), reconstructed lost wearable artifacts
(**Magical Equipment**) — and only at the very end, reluctantly, documented what
she learned about the path that doomed her civilization (**Forbidden
Knowledge**), as a warning rather than an invitation.

### 1.3 The throughline

Across all seven sections, two threads recur and pay off at the end:

- **The Weave has a health.** Draw too much, too carelessly, or from the wrong
  places, and an area's Weave frays. Frayed land breeds Frayed mobs and stops
  regenerating Wyrd. This is a standing environmental-design device the player
  feels from hour one, long before they know why it exists.
- **Fray is personal, too.** Practicing forbidden arts frays *the caster*, not
  just the land — mechanically identical system, different meter, different
  stakes (see §1.5 and Section 11 for corruption mechanics in full).

By the time the player reaches Section 7, they should already understand Fray
intuitively from the environment; Forbidden Knowledge just turns the dial from
"ambient hazard" to "the exact thing that ended a civilization," using
mechanics the player already knows.

### 1.4 The endgame hook

The Codex's final chapters point the player toward the nearest major
**Loomhold ruin**, guarded by a boss-tier Unwoven called **the Warden of the
Last Circle** — once the head of Thale's research circle, now hollowed by the
Unmaker's influence but still, faintly, protecting the ruin's inner sanctum out
of some leftover instinct.

The confrontation is designed to branch by playstyle rather than by dialogue
choice:

- A player who avoided Forbidden Knowledge entirely can defeat the Warden with
  a strong "clean" loadout (Sections 2–6), and the fight ends with the Warden
  breaking free of the Unmaker's hold for one line of dialogue before dying —
  the "redemption" read.
- A player who leaned into Forbidden Knowledge can instead *out-corrupt* the
  Warden or bind it with Unmaker-tainted tools, which defeats it in a darker,
  more dominating way — the "conquest" read.

Both are valid, legitimate endings; neither is flagged as the "true" one. This
keeps Forbidden Knowledge from being a trap section that punishes curiosity —
it's a real alternate path with its own payoff, exactly like Thaumcraft's warp
mechanic was a real tradeoff rather than a fail state.

### 1.5 Naming reference (world & narrative)

| Term | What it is |
|---|---|
| The Weave | The ambient magical field running through the world |
| Wyrd | Raw magical energy drawn from the Weave; the mod's "mana" |
| Weave Wellspring | Loomkeeper-built pedestal that concentrates ambient Weave into harvestable Wyrd; the primary renewable energy source |
| Wyrdstone | Crystalline ore; the crafting material for magical infrastructure (not the primary energy source) |
| Loomkeepers | The ancient civilization that mastered the Weave |
| Loomhold(s) | Loomkeeper ruins/dungeons found in the world |
| The Unmaker | The corrupting force born from forbidden Loomkeeper research |
| The Unwoven | People (and creatures) hollowed out by the Unmaker |
| Maren Thale | Author of the Codex Arcanum; the player's absent mentor |
| Fray | The corruption stat, tracked both per-region and per-player |
| Warden of the Last Circle | Endgame boss, guardian of the nearest Loomhold |

---

## 2. The Core Magic System (shared by every section)

These four systems underpin all seven Codex chapters and should be built before
any chapter-specific content.

### 2.1 Wyrd — the energy resource

#### Weave density & Wellsprings — the primary energy source

- **Weave density**: every chunk has an invisible Weave-density value, visible
  only through Codex tools (§2.3). Density varies by biome and proximity to
  Loomhold ruins; it determines how fast Wyrd can be drawn and how effective
  rituals/alchemy are in that area.

- **Weave Wellsprings**: the primary source of renewable Wyrd. Wellsprings are
  ancient Loomkeeper-built stone pedestals found throughout the world — most
  densely near Loomhold ruins, but scattered in smaller numbers across forests,
  mountains, and anywhere the Loomkeepers once maintained outposts. Each
  Wellspring passively draws ambient Weave energy from its chunk and
  concentrates it into harvestable Wyrd, visible as a slow spiral of luminous
  particles building above the pedestal.

  **Drawing Wyrd**: the player points their wand at a Wellspring and channels
  (hold right-click, ~3 seconds). Wyrd flows from the Wellspring into the
  wand's internal storage. The Wellspring's visible particle charge dims and
  rebuilds over time (minutes, not seconds — fast enough to feel renewable,
  slow enough to require planning).

  **Output depends on Weave density**: a Wellspring in a high-density biome
  (deep forest, mushroom fields, near a Loomhold) produces noticeably more
  Wyrd per cycle than one sitting in open plains. A Wellspring in Frayed land
  (§2.4) produces almost nothing — creating an elegant feedback loop: overdraw
  from a Wellspring, Fray the surrounding land, and the Wellspring itself
  weakens. Greed is punished by the exact system that was already there.

  **Proximity interference**: multiple Wellsprings within the same chunk or
  adjacent chunks compete for the same ambient Weave, slightly reducing each
  other's output. This discourages hoarding and encourages the player to scout
  widely rather than clustering around one spot.

  **Progression**:
  - *Early game (Section 1)*: the player finds wild Wellsprings and draws from
    them as-is. Choosing where to build a base relative to good Wellsprings is
    the first meaningful strategic decision.
  - *Mid game (Section 4 — Rituals)*: the player learns to construct their own
    Wellsprings via the **Rite of Wellspring Raising** — but player-built ones
    are weaker than ancient Loomkeeper originals (the Loomkeepers knew
    attunement techniques the player is still rediscovering). This keeps
    found-in-world Wellsprings valuable throughout the entire game.
  - *Late game (Section 6 — Equipment)*: the player learns to **attune** and
    upgrade existing Wellsprings (both found and self-built), increasing their
    output cap and regeneration speed.

#### Wyrdstone — the crafting material

- **Wyrdstone**: a glowing crystalline ore found underground, denser near
  Loomhold ruins. Wyrdstone is the *material* the Loomkeepers used to build
  Wellsprings (and wand cores, batteries, and other magical infrastructure).
  The player mines and crushes it into **Wyrd Dust**, the base crafting reagent
  used in nearly every recipe in the mod. Wyrdstone is not the player's primary
  energy source — Wellsprings are — but it's the essential material for
  building and crafting magical objects. Think of Wyrdstone as copper wiring
  and Wellsprings as the power plant.

#### Storage

- Wyrd drawn from Wellsprings is stored in wands/staves (small, personal), in
  **Wyrdstone Batteries** (portable, equipment-slot), and in **Anchor
  Braziers** (large, placed blocks that feed rituals and can themselves be
  recharged from nearby Wellsprings — see Section 6).

### 2.2 Facets — the building blocks of knowledge

Every block, item, and mob has a **Facet signature**: a small set of tags
describing its magical "flavor," directly inspired by Thaumcraft's aspects but
independently named. Twelve primal Facets, combined pairwise, produce all
compound Facets used by recipes and research:

`Ignis (fire) · Aqua (water) · Terra (earth) · Aer (air) · Lux (light) ·
Umbra (shadow) · Vita (life) · Mortis (death) · Ordo (order) · Perdo
(entropy/decay) · Motus (motion) · Cognitio (thought/knowledge)`

Compound examples: Ignis+Terra = *Metallum* (metal/smithing); Vita+Aqua =
*Victus* (food/growth); Ordo+Cognitio = *Historia* (Loomkeeper lore, found on
ruin blocks); Umbra+Mortis = *Tenebrae* (the core Facet of Forbidden
Knowledge).

### 2.3 Discovery — how the player learns

1. **Resonometer** (Section 1's signature tool): point-and-hold on a block,
   item, or mob to reveal its Facet signature and log it to the Codex.
2. **The Loom of Understanding**: the mod's research table. Discovered Facets
   appear as nodes the player physically drags threads between (a puzzle
   board, structurally equivalent to Thaumcraft's aspect-research minigame,
   re-skinned as literal weaving). Successfully connecting the right Facets in
   the right pattern unlocks a Codex page/recipe.
3. **Warp-free early game, opt-in puzzle depth later**: Section 1–2 research
   nodes are guided (little to no puzzle ambiguity) so new players aren't
   blocked; puzzle difficulty ramps up from Section 3 onward and peaks in
   Section 7, where some patterns are deliberately only found by decoding
   torn Sable Pages (§8's lore items) rather than pure trial and error.

### 2.4 Fray — the corruption system

Two independently-tracked meters, one shared mechanic:

- **Regional Fray**: attached to chunks. Rises when Wyrd is drawn faster than
  a chunk regenerates it, and sharply when Forbidden-tier rituals/alchemy are
  performed there. Visibly discolors terrain (desaturated, "unwoven" texture
  overlay), stops Wyrd regeneration, and spawns **Frayed mobs** (weak,
  common corrupted variants of vanilla mobs) once past a threshold.
- **Personal Fray**: attached to the player. Rises from casting Forbidden
  spells, wearing Duskbound equipment, or lingering in heavily Frayed land.
  At low levels it's flavor (screen-edge static, faint whispers). At high
  levels it causes real drawbacks: periodic **Unwoven** ambushes tied to the
  player rather than the location, gradual corruption of worn Magical
  Equipment, and (very high, sustained) a chance for Codex pages to
  "mis-write" themselves with Unmaker interference text — cosmetic, but
  unsettling, never destructive to actual unlocked recipes.
- **Cleansing**: Personal Fray decays slowly on its own away from dark
  practice; can be actively reduced with the Section 5 **Draught of
  Unbinding** or the Section 4 **Rite of Cleansing**. Regional Fray only
  clears through the Rite of Cleansing performed on-site — deliberately
  making land-cleansing a visible, occasional group/ritual activity rather
  than a background non-event.
- **Design intent**: Fray is a tradeoff dial, not a fail state. Forbidden
  Knowledge should always be strictly more powerful, per action, than clean
  equivalents — the cost is Fray, not weakness.

---

## 3. Section 1 — Introduction

**Facet focus:** none required yet (this section teaches Facets themselves)
**Unlocks:** the entire rest of the Codex

### Story beat
The player finds Thale's journal (the Codex) in a small hidden satchel —
placed as a semi-guided find near world spawn, or obtainable via a simple
early recipe if the mod prefers a craft-based start. The first pages are
Thale re-deriving the basics for whoever finds this next, written in a warm,
slightly wry teaching voice.

### Mechanics & items
| Item / Block | Function |
|---|---|
| **Codex Arcanum** | The guidebook; also the research/recipe browser UI |
| **Wyrdstone** (ore) | Base magical resource, mined underground |
| **Wyrd Dust** | Crushed Wyrdstone; universal crafting reagent |
| **Resonometer** | Scans blocks/mobs/items for Facet signatures |
| **Loom of Understanding** (block) | Research table; combine known Facets |
| **Weave Wellspring** (found, not crafted) | Ancient Loomkeeper pedestal; the player's first renewable Wyrd source |
| **Cobblestone Wand** | The player's first wand (2 cobblestone + 1 stick, regular crafting table); crude, low capacity, casts only *Spark*, used to interact with magical objects |

### Research nodes (in order)
1. *Awakening* — reading the Codex for the first time; no cost, always unlocked.
2. *Sensing the Weave* — craft the Resonometer.
3. *Wyrdstone Refining* — smelt/crush Wyrdstone into Wyrd Dust.
4. *First Threads* — craft the Loom of Understanding; unlocks the research UI proper.
5. *The First Wellspring* — locate a nearby Weave Wellspring and draw Wyrd from it for the first time; introduces the core energy loop.
6. *A Crude Focus* — craft the Cobblestone Wand and learn the *Spark* spell; ends the section and bridges into Section 2 (proper wandcraft).

---

## 4. Section 2 — Wands

**Facet focus:** *Metallum* (Ignis+Terra) for cores/caps, *Motus* for casting speed
**Depends on:** Section 1 complete

### Story beat
Thale's notes here read like a workshop manual with margin doodles — this is
where her voice becomes most technical and most personal (she clearly loved
wandcraft). Marginalia references specific named wands she owned, which the
player can later find as actual loot in Loomholds.

### The Cobblestone Wand — the crude beginning

Before the player discovers the Wandwright's Bench, they can craft a
**Cobblestone Wand** on a regular crafting table: 2 cobblestone + 1 stick
arranged vertically. This is the simplest possible wand — a rough stone
focus jammed onto a stick. It has no core/cap/binding system, no Focus
slots, and minimal Wyrd capacity. It can cast only *Spark* (a weak bolt)
and its primary purpose is to *interact with the world's magical objects*:
right-clicking a Lectern with a book to obtain the Codex Arcanum, drawing
small amounts of Wyrd from Wellsprings, and scanning with the Resonometer.

The Cobblestone Wand is deliberately limited so that the player feels the
upgrade when they build their first real modular wand at the Wandwright's
Bench. It's a bridge tool — it gets you into the magic system, and then
you replace it.

### Wand anatomy (modular wands)
Every proper wand/staff is assembled from three required parts and one
optional part at the **Wandwright's Bench**:

- **Core** — determines Wyrd capacity and cast efficiency (Wyrd cost per cast).
- **Cap** — determines elemental affinity/modifiers available to spells cast
  through it, and durability.
- **Binding** (grip) — determines caster-side bonuses: cooldown reduction,
  Fray resistance, or capacity bonus, depending on material.
- **Inlay** (optional) — a socketed gem for a passive perk (e.g. +Wyrd
  regen, chance to not consume a charge).

### Cores
| Core | Capacity | Efficiency | Notes |
|---|---|---|---|
| Wood Core | Low | Average | First modular core; cheap, reliable |
| Wyrdstone Core | Medium | Good | All-purpose default upgrade |
| Quicksilver Core | Low | Excellent | Fast, cheap-to-cast, low ceiling |
| Voidglass Core | High | Poor | Big battery, leaks small passive Fray |
| Bloodwood Core *(Forbidden-adjacent)* | Very High | Good | Requires Section 7 unlock; scales with Umbra/Mortis spells |

### Caps
| Cap | Effect |
|---|---|
| Copper Cap | +Wyrd draw speed (faster Wellspring channeling); slight cast-speed bonus — the "clean, reliable starter" |
| Iron Cap | +Durability, no elemental bias |
| Prism Cap | Splits single-target spells to hit up to 3 targets at reduced power |
| Quartz Cap | +Range |
| Sable Cap *(Forbidden)* | Boosts Umbra/Mortis spell power; adds passive Fray while equipped |

### Staves
Two-handed upgrades of wands (occupy the off-hand slot too). Higher base
capacity, and unlock **channeled spells** — spells that charge for a moment
before releasing a stronger effect (beams, larger novas, sustained walls).
Crafted at an upgraded Wandwright's Bench tier.

### Foci
Small socketed stones, each holding one known spell, slotted into a wand to
make that spell castable. Swapping foci is the primary "spell loadout"
mechanic — a hotbar-adjacent inventory of 2–4 foci slots per wand depending
on tier.

### Notable named wands (as world loot, not player-craftable directly)
- **Loomkeeper's Wand** — Wyrdstone core, Prism cap; common Loomhold find.
- **Duskbrand Staff** — Bloodwood core, Sable cap; rare, Forbidden-tier drop
  from deep Loomhold vaults.

### Research nodes
1. *The Wandwright's Bench*
2. *Core Attunement* (wood → Wyrdstone → Quicksilver → Voidglass)
3. *Cap Fitting* (copper → iron/quartz → prism)
4. *Focus Crafting* (unlocks slotting known spells into foci)
5. *The Long Reach* (staves + channeled spells)
6. *Inlay Work* (gem sockets)

---

## 5. Section 3 — Sorcery

**Facet focus:** all twelve primal Facets, recombined per spell
**Depends on:** Sections 1–2 (needs a wand and at least one Focus slot)

### Story beat
This chapter is written least like a manual and most like a spellbook —
Thale includes short anecdotes about *why* she designed each spell (usually
"I needed to get out of a Loomhold collapse fast" energy), which is a good
place to seed jokes and personality.

### Spell categories
| Category | Purpose | Example |
|---|---|---|
| Bolt | Single-target direct damage | *Spark*, *Emberlance* |
| Nova | AoE burst centered on caster or target | *Emberburst* |
| Wall | Temporary defensive barrier | *Aegis of Threads* |
| Utility | Non-combat effect | *Excavate*, *Clarity* |
| Movement | Repositioning | *Stepthread* |
| Enchant | Self/ally buff | *Warding Word* |
| Curse | Enemy debuff | *Binding Word* (clean); Forbidden curses live in Section 7 |

### Notable spells
| Spell | Category | Effect | Facets |
|---|---|---|---|
| Spark | Bolt | Small direct-damage bolt | Ignis |
| Emberlance | Bolt | Stronger bolt, short burn | Ignis, Motus |
| Emberburst | Nova | Fire AoE around impact point | Ignis x2 |
| Aegis of Threads | Wall | Temporary damage-absorbing barrier | Ordo, Terra |
| Excavate | Utility | Instantly mines a small area of soft blocks | Terra, Motus |
| Stepthread | Movement | Short-range blink | Motus, Aer |
| Clarity | Utility | Cures negative potion effects | Vita, Lux |
| Warding Word | Enchant | Brief damage-reduction buff on self/ally | Ordo, Vita |
| Binding Word | Curse | Roots target briefly | Terra, Perdo |

### Spell chaining
Staves with multiple foci slots can be configured to fire foci in sequence
on a single cast, producing named combo effects when specific pairs are
chained (e.g. Spark → Emberburst = *Cascading Flame*, a bonus-damage combo
callout in the Codex, mechanically just "spell A then spell B within a
short window").

### Research nodes
Gated per-spell by (a) knowing the required Facets via Resonometer scans and
(b) solving that spell's Loom pattern. Suggested order: *Spark → Excavate →
Stepthread → Emberlance → Clarity → Aegis of Threads → Warding Word →
Emberburst → Binding Word → Spell Chaining.*

---

## 6. Section 4 — Rituals

**Facet focus:** *Ordo* (structure/circle-shape) plus whatever Facet matches
the desired effect
**Depends on:** Sections 1, 2 (item to enchant/transmute usually comes from
Wands or vanilla tools)

### Story beat
Ritual pages are the first to explicitly mention *other* Loomkeepers by
name — rituals were originally group magic, and Thale notes (with some grief)
which circle-mates taught her which rite.

### Mechanics
- **Anchor Brazier**: placed block that stores Wyrd and feeds nearby pedestals.
  Can be manually charged from a wand or, when placed near a Weave Wellspring,
  slowly recharges itself from the Wellspring's output — making Wellspring
  proximity the key siting decision for ritual circles.
- **Ritual Pedestal**: placed block that holds one item. A ring of pedestals
  around a central pedestal forms a **Ritual Circle**; the circle's *shape*
  (number/arrangement of pedestals, marked with an Etching Tool on the
  ground) determines which ritual can run there.
- **Process**: place the target item (tool/weapon/armor piece/wand) on the
  central pedestal, place Facet-matched catalyst items on the surrounding
  pedestals, and the ritual draws Wyrd from Anchor Braziers over a set
  duration (minutes, not instant) to permanently bond an effect into the
  target item. Ritual effects stack with vanilla enchantments.
- **Failure states**: insufficient sustained Wyrd stalls the ritual (no
  progress loss, resumes when power returns); wrong catalysts cause a
  **backlash** — a small explosion centered on the circle and a chunk of
  Regional Fray.

### Notable rituals
| Ritual | Effect | Catalysts |
|---|---|---|
| Rite of the Ember Edge | Weapon gains fire damage | Ignis-Facet items (coal, blaze powder) |
| Rite of Bedrock's Grasp | Pickaxe gains ore-sense (highlights nearby ore briefly on use) | Terra-Facet items |
| Rite of the Feather Step | Boots: reduced fall damage + minor speed | Aer-Facet items |
| Rite of Wyrdweaving | Wand/staff gains +Wyrd capacity | Wyrd Dust, Wyrdstone |
| Rite of Transmutation | Bulk-converts a stack of one material into a related one (e.g. cobblestone → stone variants, iron ore → gold ore) | Matched-Facet ore/material |
| Rite of Wellspring Raising | Constructs a new player-built Weave Wellspring (weaker than ancient Loomkeeper originals) | Wyrdstone blocks, Wyrd Dust, Ordo + Terra Facet items |
| Rite of Cleansing | Removes Regional Fray from the circle's chunk | Lux + Vita Facet items |

### Research nodes
1. *The First Circle* (single-pedestal ritual, e.g. Ember Edge)
2. *Pedestal Attunement* (multi-pedestal circles)
3. *Catalyst Reagents* (Facet-matching for catalysts)
4. *Rite of Transmutation*
5. *Rite of Wellspring Raising* (build your own Wellsprings)
6. *Rite of Cleansing*
7. *Greater Circles* (larger, multi-ring rituals for endgame-tier effects)

---

## 7. Section 5 — Alchemy

**Facet focus:** *Victus* (Vita+Aqua) and material-specific Facets
**Depends on:** Section 1 (Wyrd Dust), light overlap with Section 4
(catalyst items are often alchemy outputs)

### Story beat
Alchemy pages are the most "recipe book" in tone — short, dense, practical —
which is a deliberate contrast to Sorcery's storytelling voice, reinforcing
that this is the "material science" chapter.

### Core concept: Essence
Distinct from Wyrd. **Essence** is the concentrated *material* property of a
thing (its Facet signature made tangible and storable), not raw energy.
Essence is the mod's answer to Thaumcraft's Essentia.

### Tools & blocks
| Tool/Block | Function |
|---|---|
| Mortar & Pestle | Grinds raw reagents into alchemical dust |
| Distillation Rig | Extracts Essence from a reagent into a Vial |
| Essence Reservoir | Stores extracted Essence in bulk (jar-like block) |
| Essence Conduit | Pipes Essence between Rigs/Reservoirs/Cauldrons automatically |
| Cauldron of Making | Combines Essence + dust + (optionally) water into potions, salves, or solid "Concentrate" blocks |

### Materialization
The signature late-alchemy mechanic: sufficiently concentrated Essence can be
converted back into physical matter in the Cauldron of Making — e.g. pure
*Essence of Terra* + water yields clay or stone blocks, letting players
"grow" bulk basic resources at a Wyrd cost. This exists as a resource-sink
and quality-of-life reward, not a way to bypass mining entirely (recipes are
tuned to be more expensive than just mining the thing, except for
genuinely rare/renewable-poor materials).

### Notable recipes
| Item | Effect | Key inputs |
|---|---|---|
| Essence of Vita | Base reagent for healing/growth | Saplings, wheat |
| Essence of Ignis | Base reagent for fire effects | Coal, blaze powder |
| Draught of Clear Sight | Temporarily see Weave density & Facets without a Resonometer | Essence of Lux + Cognitio |
| Salve of Mending | Heals player and repairs held item's durability | Essence of Vita, Wyrd Dust |
| Philosopher's Clay | Bulk-transmutes a common ore into a related valuable ore | Concentrated Essence of Terra + Metallum, high Wyrd cost |
| Draught of Unbinding | Reduces the drinker's Personal Fray | Essence of Lux + Ordo |

### Research nodes
1. *Grinding & Distilling* (Mortar, basic Rig)
2. *The Reservoir*
3. *Essence Conduits* (automation)
4. *Concentration* (upgrading dilute Essence to pure)
5. *Materialization*
6. *The Philosopher's Clay*
7. *Draught of Unbinding*

---

## 8. Section 6 — Magical Equipment

**Facet focus:** whatever matches the gear's function; this chapter is a
capstone that draws on Sections 2–5 rather than introducing a new Facet
family
**Depends on:** Sections 2–5 (equipment recipes intentionally require
components from wands, rituals, and alchemy to reinforce that this is an
"advanced synthesis" chapter)

### Story beat
This is where the Codex starts referencing Loomhold *ruins* directly as a
source, not just Thale's own bench — several top-tier pieces are "reconstruct
this from a broken relic" recipes rather than build-from-scratch, nudging the
player toward exploration.

### Notable equipment
| Item | Slot | Effect |
|---|---|---|
| Attunement Amulet | Necklace/trinket | +Max Wyrd capacity (stacks of the amulet's tier) |
| Weavecloth Robes | Chest (alt. armor set) | Reduced spell Wyrd cost; reduced Fray gain |
| Loomkeeper's Goggles | Head | Passive Facet/Weave-density sight (upgrade of Resonometer) |
| Capacitor Belt | Belt/trinket | Portable extra Wyrd storage, rechargeable at a Brazier |
| Thread-sprite Charm | Trinket | Summons a small floating construct that collects nearby drops |
| Warding Sigil Shield | Off-hand | Passive reduction of magic-sourced damage |
| Portable Loom | Trinket | Miniature Loom of Understanding, usable away from base |
| Wellspring Attunement Stone | Placed on a Wellspring | Upgrades a Wellspring's output cap and regen speed; crafted from Loomhold relics |

### Equipment sets
- **Novice's Set** — early, single-piece unlocks, no set bonus.
- **Loomkeeper's Regalia** — full endgame "clean" set, reconstructed from
  Loomhold relics; set bonus reduces all Wyrd costs and grants slow passive
  Fray decay.
- **Duskbound Set** *(Forbidden-tier, Section 7 crossover)* — strongest raw
  stats in the mod; set bonus adds a passive personal Fray drain the whole
  time it's worn.

### Research nodes
1. *Wearable Attunement* (Amulet)
2. *Weavecloth* (Robes)
3. *The Capacitor Belt*
4. *Binding a Familiar* (Thread-sprite Charm)
5. *Wellspring Mastery* (Attunement Stone; upgrading Wellsprings)
6. *Loomkeeper's Regalia* (relic-reconstruction recipes)
7. *Duskbound Craft* (unlocked from/with Section 7)

---

## 9. Section 7 — Forbidden Knowledge

**Facet focus:** *Tenebrae* (Umbra+Mortis)
**Depends on:** finding a torn **Sable Page** in a Loomhold ruin (the section
is hidden/locked in the Codex until then, not available from a normal-
progression unlock)

### Story beat
These pages are visibly different in the Codex UI — torn edges, different
ink, written in a second hand (one of the Loomkeepers who *did* go too far,
not Thale). Thale's own handwriting reappears in the margins as warnings she
added after finding these pages herself: "I kept this. I shouldn't have. Read
it once, then decide." This is the section that directly explains the
Unmaker and the fall of the Loomholds — most of the section 1 worldbuilding
pays off here.

### Mechanics
- **Dark Essence**: extracted from death/decay-adjacent materials (bone,
  rotten flesh, gunpowder, spider eyes, wither-adjacent drops) using a
  **Blightened Rig** — a Distillation Rig that must first be deliberately
  "tainted" (a one-way crafting choice per-rig: once tainted, that specific
  Rig only produces Dark Essence, encouraging players to keep a separate
  dark setup rather than casually mixing it into their main alchemy line).
- **Fray cost**: every Forbidden recipe/spell/ritual raises Personal Fray
  (§2.4); higher-tier ones raise Regional Fray too. This section is the
  primary place the Fray system is spent, not introduced.
- **Power/cost tradeoff**: Forbidden items are strictly stronger per-action
  than their clean equivalents, by design — the Codex is explicit about this
  tradeoff rather than hiding it.

### Notable items & spells
| Item/Spell | Type | Effect | Cost |
|---|---|---|---|
| Bonecarved Wand Core | Wand core | Cheap, surprisingly high efficiency | Passive small Fray while equipped |
| Sable Cap | Wand cap | Boosts Umbra/Mortis spell power | Passive small Fray while equipped |
| Wraithbind | Curse spell | Drains target health to caster | High per-cast Fray |
| Gravemarrow Powder | Reagent | Required catalyst for necrotic rituals | — |
| Wither-thread Catalyst | Reagent | Unlocks the most powerful (and riskiest) rites | Large one-time Regional Fray on use |
| Duskbrand Staff | Staff (world loot, see §4) | Very high capacity, Bloodwood/Sable | Passive Fray while equipped |
| The Unmaker's Whisper | Lore item / capstone spell | Grants a large temporary power spike | Very large Personal Fray spike |
| Hollow Crown | Equipment (endgame relic) | Highest raw power in the mod | Continuous passive Fray drain while worn |

### Research nodes
1. *A Warning Ignored* — reading the first Sable Page; unlocks the section.
2. *Tainting the Rig* — commit a Distillation Rig to Dark Essence production.
3. *Dark Essence* — basic extraction recipes.
4. *Wraithbind* — first Forbidden spell.
5. *The Sable Pages* — recovering the rest of the torn journal from deeper
   Loomhold vaults; each recovered page unlocks further nodes.
6. *The Unmaker's Whisper* — capstone node, tied narratively to the ending
   (see §1.4).

---

## 10. World Content: Loomholds

Loomhold ruins are the mod's dungeon content and the primary delivery
mechanism for lore, relic equipment, and Sable Pages. Suggested tiers:

- **Loomhold Outposts** (small, surface/shallow-underground): a handful of
  rooms, light puzzle/trap content, common Facet-rich blocks and a chance at
  a low-tier named wand.
- **Loomhold Towers** (mid-size, vertical, partially collapsed): denser
  Weave, Frayed-mob encounters, mid-tier relic equipment, first Sable Page
  drops.
- **The Last Circle** (unique, endgame): home of the Warden boss (§1.4), the
  densest Weave and heaviest Regional Fray in the world, final Sable Pages,
  and the Loomkeeper's Regalia / Duskbrand Staff / Hollow Crown relic pool.

---

## 11. Fray Reference Table (cross-section summary)

| Fray level (Personal) | Effect |
|---|---|
| 0–20 | No mechanical effect; Codex may add a small flavor note |
| 21–50 | Faint screen-edge static/whispers; cosmetic only |
| 51–80 | Occasional Unwoven ambushes near the player; slow visual corruption of worn Duskbound-tier gear |
| 81–100 | Frequent Unwoven ambushes; chance of Codex pages "mis-writing" (cosmetic); strong candidate state for the "conquest" ending path |

| Fray level (Regional) | Effect |
|---|---|
| 0–20 | No visible change |
| 21–50 | Terrain desaturation, Wyrd regeneration slowed |
| 51–80 | Wyrd regeneration halted; common Frayed mob spawns |
| 81–100 | Frayed mob spawns frequent/stronger; only clears via Rite of Cleansing |

---

## 12. From Page to Plan

Suggested implementation stage grouping for turning this document into actual
build plans (each becomes its own detailed plan later):

1. **Stage 0 — Core Systems**: Wyrd, Weave density, Facets, Fray (both
   meters), the Resonometer, and the Loom of Understanding UI. Nothing else
   can be built before this.
2. **Stage 1 — Introduction & Wands** (Sections 1–2): first resources, first
   wand, Wandwright's Bench.
3. **Stage 2 — Sorcery** (Section 3): spell system, foci, the initial spell
   roster.
4. **Stage 3 — Rituals & Alchemy** (Sections 4–5): pedestals/circles,
   Distillation Rig chain, Materialization.
5. **Stage 4 — Magical Equipment** (Section 6): synthesis-tier gear, relic
   reconstruction recipes.
6. **Stage 5 — Forbidden Knowledge & Loomholds** (Section 7 + §10): dungeon
   generation, Sable Page item/lore delivery, Blightened Rig, endgame boss.
7. **Stage 6 — Endgame & Narrative Payoff** (§1.4): Warden of the Last
   Circle encounter and its two resolution paths.

Each stage plan should pull its item/block/recipe list directly from the
tables above, and its narrative beats directly from the "Story beat"
subsections, so nothing has to be re-invented at planning time.
