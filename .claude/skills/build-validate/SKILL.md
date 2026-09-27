---
name: build-validate
description: >
  Load this skill whenever you need to compile, run datagen, test with a
  server boot, or check logs. Covers the full compile -> datagen ->
  server-boot -> check-logs cycle. Load when: finishing any code change,
  debugging a crash, after datagen provider changes, before declaring a
  feature "done", or when the user says "build", "compile", "test", "run",
  "server", "check", "validate", or "verify". Also load when you need to
  kill orphaned JVM processes or diagnose server boot failures.
---

# Build & Validate Workflow

## JAVA_HOME

Every Gradle command in this project requires the JAVA_HOME prefix (it isn't inherited from the
calling shell):

```
JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot"
```

## 1. Compile

Always the first step after any Java source change.

```bash
JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot" ./gradlew compileJava --console=plain
```

If it fails, fix compile errors before proceeding. For API-related errors, load the `api-research`
skill and verify the types/methods against the actual jars.

## 2. Datagen

Run after changing any `data/Arcane*Provider` class. Datagen does NOT run automatically as part
of `compileJava`/`runServer`/`runClient`.

```bash
JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot" ./gradlew runData --console=plain
```

- Output: `src/generated/resources/` (merged with hand-written `src/main/resources/` at build time)
- `--existing src/main/resources` is already configured in `build.gradle`
- Hash cache under `src/generated/resources/.cache` makes it a no-op for unchanged providers
- Safe to re-run anytime
- Key-ordering differences and missing trailing newlines in generated JSON are cosmetic, not errors

## 3. Server Boot (validates registry/worldgen/capability/dist-safety)

This is the ONLY way to catch dist-separation bugs (client code leaking to server), registry
errors, worldgen JSON typos, and tag-resolution issues.

### Clean before test run
```powershell
Remove-Item -Recurse -Force run/saves, run/logs, run/world -ErrorAction SilentlyContinue
```

### Accept EULA (one-time dev setup, safe to auto-accept)
```powershell
if (-not (Test-Path run)) { New-Item -ItemType Directory run }
Set-Content -Path run/eula.txt -Value "eula=true" -Encoding utf8
```

### Launch server
```bash
JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot" ./gradlew runServer --console=plain
```

Run in background for automated validation. The "experimental features" world-creation warning is
**normal Forge/vanilla behavior** when any mod touches worldgen — not a bug, safe to click through.

## 4. Log Checking

After server boot completes (or crashes), check the log:

```bash
grep -E "arcanemod|ERROR|Exception" run/logs/latest.log
```

**What to look for:**
- `ClassNotFoundException` / `NoClassDefFoundError` — dist separation violation (client class
  loaded on server)
- `Missing loottable` / `Missing model` — datagen scoping issue (forgot `getKnownBlocks`
  override or didn't add to provider)
- `Tags not bound` / `Components not bound` — timing issue (tag/component access too early)
- `Registry` errors — missing or duplicate registration
- `[arcanemod/]` prefixed lines — the mod's own logger output, good for checking initialization

## 5. Killing Orphaned JVM Processes

**`TaskStop` on a backgrounded `runServer` does NOT kill the forked server JVM.** It's a separate
process from the Gradle wrapper. If not cleaned up, the next `runServer` fails with
`Address already in use`.

### Check for orphans
```powershell
Get-CimInstance Win32_Process -Filter "name='java.exe'" | Select-Object ProcessId, CommandLine
```

### Kill (skip the Gradle daemon PID if you know it)
```powershell
Stop-Process -Id <PID> -Force
```

## 6. Validation Checklist

Before declaring a change "done":

- [ ] `compileJava` succeeds with no errors
- [ ] `runData` succeeds (if datagen providers were changed)
- [ ] Generated JSON in `src/generated/resources/` looks correct
- [ ] Server boots without errors in `latest.log`
- [ ] No `ClassNotFoundException` or `NoClassDefFoundError` (dist separation)
- [ ] No `Missing loottable`/`Missing model` errors (datagen scoping)
- [ ] No `Tags not bound` or `Components not bound` errors (timing issues)
- [ ] Mod's own log lines show expected initialization

## Common Issues

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| `Address already in use` | Orphaned java.exe from previous run | Kill the process (see step 5) |
| `ClassNotFoundException` on server | Client class referenced in shared code | Move to its own class, load only on client dist |
| `Missing loottable 'minecraft:blocks/stone'` | Datagen provider not scoped to mod | Override `getKnownBlocks()` / `getKnownEntityTypes()` |
| `Tags not bound` | Tag check during datagen or too early | Move to reload listener `apply()` or game time |
| `Components not bound yet` | `Item#components()` called in reload listener | Hand-list values instead (see `VanillaFacetOverrides`) |
| Stale generated resources | Forgot to re-run datagen after provider change | Run `./gradlew runData` |
