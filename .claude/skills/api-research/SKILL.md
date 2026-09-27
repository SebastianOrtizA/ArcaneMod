---
name: api-research
description: >
  ALWAYS load this skill BEFORE using any Minecraft, Forge, or modding API
  you have not verified in THIS session. Load when: implementing any new
  feature, fixing a compile error, writing datagen, checking event
  signatures, looking up registry constants, or ANY time you are about to
  write code that calls into net.minecraft.* or net.minecraftforge.*.
  This project uses MC 26.2 / Forge 65.1.3 / Java 25 — a version where
  web search and training data are WRONG. Read the actual jars. Also load
  when you see "API", "how does X work", "what class", "what method",
  "does X exist", "check the source", or any uncertainty about types,
  method signatures, or class locations.
---

# API Research for MC 26.2 / Forge 65.1.3

## The Iron Rule

**Never trust memory, web search, or training data for any MC/Forge API shape in this project.**

This is MC 26.2 / Forge 65.1.3 / Java 25 — a combination where published docs, tutorials, and
model training data are almost all stale or wrong. Every non-trivial API question must be answered
by reading the actual decompiled source from the jars below.

When in doubt: extract the class and read it. When not in doubt: you should probably still check.

## Jar Locations

### Forge Sources (patched vanilla + Forge additions)
```
C:\Users\sebas\.gradle\caches\minecraftforge\forgegradle\mavenizer\caches\maven\forge\net\minecraftforge\forge\26.2-65.1.3\forge-26.2-65.1.3-sources.jar
```
Contains Forge's own classes AND a `patches/` folder with `.java.patch` diffs showing exactly what
Forge changes on top of vanilla. **The patch files are often more informative than the patched class
itself** — many Forge-added methods, fields, and override points (like `getKnownBlocks()` for
datagen scoping) are ONLY visible in the patch, not in the plain decompiled vanilla class.

### Decompiled Vanilla Sources
```
C:\Users\sebas\.gradle\caches\minecraftforge\forgegradle\mavenizer\caches\forge\.global\mcp\26.2-20260616.103818\forge\8a125c613a05de4682e8a8633c5f7123d78845fa\decompile\decompile.jar
```
The `<hash>` directory may change if Forge re-decompiles. If the path 404s, list the parent
directory to find the current hash.

### Client Jar (vanilla data files)
```
C:\Users\sebas\.gradle\caches\minecraftforge\forgegradle\mavenizer\caches\minecraft_tasks\26.2\client.jar
```
Contains real vanilla data: loot tables, recipes, worldgen JSON, tags, block/item models. This is
the big ~37MB client jar — vanilla data is NOT in the small `server.jar`. When unsure of a JSON
schema, extract vanilla's own file and copy its shape.

## How to Extract and Search

Use PowerShell + `System.IO.Compression.ZipFile`. See `references/jar-extraction-patterns.md` for
copy-pasteable snippets covering:
- Opening a jar and searching entry names
- Reading a specific class file
- Checking Forge `.java.patch` files
- Extracting vanilla data JSON files

**Always search entry names first** before assuming a class/file exists — names may have changed
in this version.

## When to Research (Checklist)

Before writing code, check whether any of these apply:
- [ ] About to use an API method/class/constant you haven't verified this session
- [ ] Something doesn't compile and the error involves MC/Forge types
- [ ] Implementing a new feature that touches a new part of the MC/Forge API
- [ ] A type or method name doesn't match what you'd expect from older MC versions
- [ ] Adding datagen providers (model/loot/recipe/tag — check Forge patches for scoping hooks)
- [ ] Working with events (check the actual event class for cancellation pattern, fields, etc.)
- [ ] Unsure about a JSON schema (recipe, model, worldgen, tag file)

If any box would be checked: **extract and read the source first, then write code.**

## Known API Quirks

Before extracting a class, check `references/api-quirks.md` — it contains ~30 verified quirks
for this exact build, organized by category. These were all discovered by reading the jars and
confirmed to be correct. They cover:

- Renamed/restructured types (Identifier, EntityTypes split, ChunkPos record)
- Registration & construction patterns
- Capabilities vs data components
- BlockEntity save/load (ValueOutput/ValueInput)
- Recipe & data JSON schema quirks
- GUI & rendering gotchas
- Event system patterns (Cancellable boolean-return)
- Tag/component binding timing
- Client/server dist separation
- Item use & interaction patterns
- Worldgen placement & heightmaps
- Network packet registration
- Particle type registration

Read the relevant section before implementing — it will save you from re-discovering a quirk
the hard way.
