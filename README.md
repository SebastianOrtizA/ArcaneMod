# Codex Arcanum

A Thaumcraft-inspired magic mod for Minecraft Java Edition, built on Forge. Explore the Weave, harness Wyrd energy, scan the world with Facets, and unravel the lost knowledge of the Loomkeepers through the Codex Arcanum — an in-game guidebook that fills itself as you research.

## Requirements

- **Minecraft** 26.2
- **Forge** 65.1.3+
- **Java** 25 (JDK 25 — [Eclipse Adoptium](https://adoptium.net/) recommended)

## Building from Source

1. **Clone the repository**
   ```
   git clone https://github.com/SebastianOrtizA/ArcaneMod.git
   cd ArcaneMod
   ```

2. **Build the mod**
   ```
   ./gradlew build
   ```
   The compiled jar will be in `build/libs/`.

3. **Run the client** (development environment)
   ```
   ./gradlew runClient
   ```

4. **Run a dedicated server** (development environment)
   ```
   ./gradlew runServer
   ```

5. **Run data generation** (regenerates models, loot tables, recipes, tags, and lang files)
   ```
   ./gradlew runData
   ```

> **Note:** On first run, Gradle will download Minecraft, Forge, and all dependencies. This can take several minutes.

## What's Built (Stage 0 — Core Systems)

Stage 0 lays the foundation every later system depends on. All four phases are complete:

### Phase 0.A — Weave & Wyrd
- **Weave density** per chunk — biome-driven base values with slow regeneration toward equilibrium
- **Weave Wellsprings** — ancient pedestals that convert ambient Weave into harvestable Wyrd, with randomized capacity, proximity interference, and density-dependent output
- **Wyrdstone ore** and **Wyrd Dust** — the core crafting material, with deepslate variant and worldgen
- **Wand channeling** — hold right-click on a Wellspring to draw Wyrd into a wand's internal storage

### Phase 0.B — Facets & Resonometer
- **Facet system** — 12 primal Facets (Ignis, Aqua, Terra, Aer, Lux, Umbra, Vita, Mortis, Ordo, Perdo, Motus, Cognitio) plus compound Facets from pairwise combinations
- **~1,780 vanilla entries** covered with computed Facet signatures (blocks, items, entities)
- **Resonometer** — point-and-hold scanning tool that reveals Facet signatures and logs discoveries
- **HUD overlay** showing scan results in real time

### Phase 0.C — Fray
- **Regional Fray** — chunk-level corruption from overdrawn Weave; halts density regen, tints fog, closes in render distance
- **Personal Fray** — player-level corruption from lingering in Frayed areas; vignette overlay with escalating severity
- **Frayed mobs** — corrupted Zombies, Skeletons, and Spiders that replace natural spawns in high-Fray chunks (+20% HP, +10% damage)
- **Fray rendering** — fog color/distance effects with smooth cross-chunk blending, full-screen vignette

### Phase 0.D — Loom of Understanding & Research
- **Research tree** — data-driven nodes loaded from JSON, with prerequisites and unlock triggers
- **Loom of Understanding** — the research block; interact to browse the tree and solve Facet-connection puzzles
- **Puzzle board** — drag threads between Facet nodes to match a target pattern and unlock new knowledge
- **Codex Arcanum** — in-game guidebook with tabbed sections; pages unlock as research progresses, ordered by prerequisite depth

### Items & Blocks
- Modular wand system (cores, caps, bindings, inlays) with Wandwright's Bench crafting
- Spell Focus items with cycle-switching on modular wands/staves
- Wyrdstone Ore, Deepslate Wyrdstone Ore, Weave Wellspring
- Wandwright's Bench, Advanced Wandwright's Bench, Loom of Understanding
- Codex Arcanum (guidebook item), Resonometer
- Full set of crafting recipes, loot tables, and item/block tags
- Placeholder textures for all content

### Debug Commands
- `/arcane facets <id>` — look up a Facet signature
- `/arcane scan <id>` — force-record a scan
- `/arcane knowledge` — dump known Facets/compounds/scans
- `/arcane fray` — report personal Fray level
- `/arcane fray add <amount>` — modify personal Fray
- `/arcane frayed <zombie|skeleton|spider>` — spawn a Frayed mob
- `/arcane loom` — place a Loom of Understanding
- `/arcane research` — list all nodes with status
- `/arcane research complete/forget <id>` — modify research progress

## Roadmap

| Stage | Name | Summary |
|-------|------|---------|
| **0** | **Core Systems** | Weave density, Wyrd energy, Facets, Fray, Research tree, Loom, Codex *(complete)* |
| **1** | **Introduction & Wands** | Codex Sections 1–2: guided early-game progression, full wand component tree, first spells |
| **2** | **Sorcery** | Codex Section 3: spell Focus system, offensive/defensive/utility spells, Wyrd cost balancing |
| **3** | **Rituals & Alchemy** | Codex Sections 4–5: multi-block ritual circles, Anchor Braziers, alchemical transmutation |
| **4** | **Magical Equipment** | Codex Section 6: wearable artifacts, Wyrdstone Batteries, Wellspring attunement |
| **5** | **Forbidden Knowledge & Loomholds** | Codex Section 7 + dungeons: Sable Pages, Duskbound gear, Loomhold structure generation |
| **6** | **Endgame & Narrative** | Warden of the Last Circle boss, branching resolution (redemption vs. conquest), narrative payoff |

## License

This mod is licensed under the [GNU Affero General Public License v3.0](LICENSE) (AGPL-3.0).
Minecraft Forge itself is licensed separately under the LGPL 2.1 — see [LICENSE.txt](LICENSE.txt).
