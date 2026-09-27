# Jar Extraction Patterns (PowerShell)

All extraction uses `System.IO.Compression.ZipFile` — no `unzip` binary is available in the bash
environment.

## Setup

```powershell
Add-Type -AssemblyName System.IO.Compression.FileSystem
```

## Open a Jar

```powershell
# Forge sources (patches + Forge classes)
$forgeJar = [System.IO.Compression.ZipFile]::OpenRead("C:\Users\sebas\.gradle\caches\minecraftforge\forgegradle\mavenizer\caches\maven\forge\net\minecraftforge\forge\26.2-65.1.3\forge-26.2-65.1.3-sources.jar")

# Decompiled vanilla
$vanillaJar = [System.IO.Compression.ZipFile]::OpenRead("C:\Users\sebas\.gradle\caches\minecraftforge\forgegradle\mavenizer\caches\forge\.global\mcp\26.2-20260616.103818\forge\8a125c613a05de4682e8a8633c5f7123d78845fa\decompile\decompile.jar")

# Client jar (vanilla data)
$clientJar = [System.IO.Compression.ZipFile]::OpenRead("C:\Users\sebas\.gradle\caches\minecraftforge\forgegradle\mavenizer\caches\minecraft_tasks\26.2\client.jar")
```

## Search Entry Names

```powershell
# Find classes by partial name
$vanillaJar.Entries | Where-Object { $_.FullName -match "BlockEntity" } | Select-Object FullName

# Find Forge patches for a specific class
$forgeJar.Entries | Where-Object { $_.FullName -match "\.java\.patch$" -and $_.FullName -match "BlockLootSubProvider" } | Select-Object FullName

# Find vanilla data files (recipes, loot tables, tags, models)
$clientJar.Entries | Where-Object { $_.FullName -match "data/minecraft/recipe/" } | Select-Object FullName
$clientJar.Entries | Where-Object { $_.FullName -match "data/minecraft/loot_table/" } | Select-Object FullName
$clientJar.Entries | Where-Object { $_.FullName -match "data/minecraft/tags/" } | Select-Object FullName
```

## Read a Specific Entry

```powershell
# Read a Java source file
$entry = $vanillaJar.Entries | Where-Object { $_.FullName -eq "net/minecraft/world/level/block/entity/BlockEntity.java" }
$reader = [System.IO.StreamReader]::new($entry.Open())
$content = $reader.ReadToEnd()
$reader.Close()
$content  # or pipe to Out-File to save

# Read a Forge patch file
$patch = $forgeJar.Entries | Where-Object { $_.FullName -match "patches/.*LevelChunk\.java\.patch$" }
$reader = [System.IO.StreamReader]::new($patch.Open())
$reader.ReadToEnd()
$reader.Close()
```

## Extract to a Local File

```powershell
# Extract a single file to scratchpad
$outPath = "$env:TEMP\claude\extracted_class.java"
$entry = $vanillaJar.Entries | Where-Object { $_.FullName -eq "net/minecraft/world/item/Item.java" }
[System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $outPath, $true)

# Extract a vanilla JSON file (e.g., a recipe to check schema)
$recipeEntry = $clientJar.Entries | Where-Object { $_.FullName -match "data/minecraft/recipe/stick\.json$" }
$reader = [System.IO.StreamReader]::new($recipeEntry.Open())
$reader.ReadToEnd()
$reader.Close()
```

## Always Close When Done

```powershell
$forgeJar.Dispose()
$vanillaJar.Dispose()
$clientJar.Dispose()
```

## Tips

- **Search before assuming**: class names and package paths may have changed in this version.
  Always search entry names first.
- **Check patches alongside classes**: a Forge-patched method won't appear in the vanilla
  decompile jar. Open the Forge sources jar and look for the `.java.patch` file.
- **Save extracted files to scratchpad** for reference within the session — they don't persist
  across sessions but save re-extraction within one.
- **The client jar is ~37MB** — don't try to extract everything. Search for specific entries.
