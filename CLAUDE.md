# Codex Arcanum (arcanemod)

A Thaumcraft-inspired Minecraft Forge mod. Design bible and per-stage plans live in `plans/` —
read `plans/codex-arcanum-design-document.md` first for the big picture, then the relevant
`plans/stage-N-*.md` file for whatever's being worked on. `plans/stage-0-core-systems.md`'s
"Phase 0.A Status" section is the current source of truth for what's actually built vs. planned.

## Project identity

- **Minecraft version:** 26.2 · **Forge:** 65.1.3 · **Java:** 25 (toolchain already pinned in
  `build.gradle` and `gradle.properties`)
- **Mod ID:** `arcanemod` · **Base package:** `com.sebas.arcanemod` · **Project root:** this repo
- This is a very new/uncommon version combination — published docs and tutorials are almost all
  stale or wrong for it. **Do not trust web search or memory for API shapes here.** Load the
  `api-research` skill before using any MC/Forge API.

## Skills

This project has five skills in `.claude/skills/` — load them by name when their domain applies.
They contain the procedural knowledge that used to live in this file.

| Skill | When to load |
|-------|-------------|
| **`api-research`** | Before using any MC/Forge API you haven't verified this session. Contains jar paths, extraction patterns, and all verified API quirks for this exact build. |
| **`build-validate`** | When compiling, running datagen, booting the test server, or checking logs. The full compile -> datagen -> server -> logs cycle. |
| **`datagen`** | When adding/modifying data-generated content (models, loot, recipes, tags, lang). Lists all existing providers and step-by-step checklists. |
| **`placeholder-texture`** | When creating placeholder PNG textures for new blocks, items, particles, entities, or GUI elements via PowerShell + System.Drawing. |
| **`new-feature`** | When implementing a new block, item, entity, particle, capability, packet, or screen from scratch. Orchestrates the other skills into a step-by-step workflow. |

### Key references inside skills

- **All verified API quirks** (~30 entries, organized by category):
  `.claude/skills/api-research/references/api-quirks.md`
- **Jar extraction patterns** (copy-pasteable PowerShell snippets):
  `.claude/skills/api-research/references/jar-extraction-patterns.md`
- **Datagen provider templates** (annotated skeletons for block/item/entity):
  `.claude/skills/datagen/references/provider-patterns.md`

## Jar locations (quick reference)

For the full extraction workflow, load `api-research`. These are the three source jars:

- **Forge sources** (patched classes + `patches/` diffs):
  `C:\Users\sebas\.gradle\caches\minecraftforge\forgegradle\mavenizer\caches\maven\forge\net\minecraftforge\forge\26.2-65.1.3\forge-26.2-65.1.3-sources.jar`
- **Decompiled vanilla**:
  `C:\Users\sebas\.gradle\caches\minecraftforge\forgegradle\mavenizer\caches\forge\.global\mcp\26.2-20260616.103818\forge\<hash>\decompile\decompile.jar`
  (list the parent dir if `<hash>` changes)
- **Client jar** (vanilla data — loot tables, recipes, worldgen, tags, models):
  `C:\Users\sebas\.gradle\caches\minecraftforge\forgegradle\mavenizer\caches\minecraft_tasks\26.2\client.jar`

## Working style

This is a **learning project** — the user is building this mod idea by idea and wants to
understand *why*, not just get working code. When implementing:
- Complete each stage fully before moving on — don't pause for intermediate build/test cycles
  unless something is actually broken.
- When something doesn't compile or behaves oddly, **read the actual decompiled source** rather
  than guessing from general Minecraft-modding knowledge — this version diverges from every
  published tutorial in ways that are easy to get subtly wrong.
- Tuning numbers (Wyrd rates, rarity, capacities, etc.) get iterated on rapidly based on in-game
  feel — expect frequent small constant changes rather than getting them "right" up front.
