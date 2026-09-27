---
name: placeholder-texture
description: >
  Load this skill when you need to create placeholder textures for new
  blocks, items, particles, entities, or GUI elements. Uses PowerShell +
  System.Drawing to generate programmer-art PNGs since no image generation
  tool is available. Load when: adding a new block, item, particle, entity,
  or GUI texture; when you see "texture", "sprite", "icon", "placeholder",
  "missing texture", "pink and black", or need to create any PNG asset.
---

# Placeholder Texture Generation

Uses PowerShell + `System.Drawing` to create programmer-art PNGs. These are placeholders for
development — expect them to be replaced with real art eventually. Focus on distinct
colors/shapes that make different content visually distinguishable during testing.

## Basic Pattern

```powershell
Add-Type -AssemblyName System.Drawing

$bmp = New-Object System.Drawing.Bitmap(16, 16)

# Fill with base color
for ($x = 0; $x -lt 16; $x++) {
    for ($y = 0; $y -lt 16; $y++) {
        $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(255, 120, 80, 160))
    }
}

# Add detail pixels for visual interest
$bmp.SetPixel(4, 4, [System.Drawing.Color]::FromArgb(255, 200, 160, 255))
$bmp.SetPixel(11, 7, [System.Drawing.Color]::FromArgb(255, 200, 160, 255))

$bmp.Save("D:\Projects\MDK\src\main\resources\assets\arcanemod\textures\block\my_block.png")
$bmp.Dispose()
```

## Texture Locations

| Type | Path | Typical Size |
|------|------|-------------|
| Block | `assets/arcanemod/textures/block/<name>.png` | 16x16 |
| Item | `assets/arcanemod/textures/item/<name>.png` | 16x16 |
| Particle | `assets/arcanemod/textures/particle/<name>.png` | 8x8 |
| Entity | `assets/arcanemod/textures/entity/<name>.png` | Varies (64x32 humanoid base) |
| GUI background | `assets/arcanemod/textures/gui/<name>.png` | Varies (256x256 for book-like screens) |
| GUI overlay | `assets/arcanemod/textures/gui/<name>.png` | Match screen dimensions |

For entity textures, match the dimensions of the vanilla counterpart the entity is based on.

## Color Conventions (from existing textures)

- **Magical/arcane elements**: purples and deep blues (`#7850A0`, `#5030B0`)
- **Ores**: stone-gray base with colored speckles (`#808080` base + `#A060D0` speckles)
- **Wyrdstone/Wyrd**: deep purple hues (`#6040A0`)
- **Wellspring blocks**: brighter, glowing blues/purples

Use 2-3 colors max per texture, with slight shade variation to avoid flat-looking blocks. A 1-2
pixel border in a slightly darker shade helps blocks look defined in-world.

## Alpha-Gradient Textures (for overlays/vignettes)

`fillGradient` only works vertically — for radial effects (vignette, glow), blit a pre-baked
alpha-gradient texture.

```powershell
Add-Type -AssemblyName System.Drawing

$size = 256
$bmp = New-Object System.Drawing.Bitmap($size, $size)
$cx = $size / 2; $cy = $size / 2
$maxR = [Math]::Sqrt($cx * $cx + $cy * $cy)

for ($x = 0; $x -lt $size; $x++) {
    for ($y = 0; $y -lt $size; $y++) {
        $dist = [Math]::Sqrt(($x - $cx) * ($x - $cx) + ($y - $cy) * ($y - $cy))
        $alpha = [Math]::Min(255, [int](255 * $dist / $maxR))
        $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($alpha, 255, 255, 255))
    }
}

$bmp.Save("D:\Projects\MDK\src\main\resources\assets\arcanemod\textures\gui\vignette.png")
$bmp.Dispose()
```

The texture's alpha channel carries the shape; the blit's tint color carries color/intensity:
`gg.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, srcW, srcH, texW, texH, tintARGB)`

## Particle Textures

When creating a new particle texture, you also need:

1. **Particle definition JSON**: `assets/arcanemod/particles/<name>.json`
   ```json
   {"textures": ["arcanemod:<name>"]}
   ```

2. **Register `SimpleParticleType`** in `ModParticles` (common, both dists)

3. **Register sprite provider** in `ParticleEvents` (client only, `Dist.CLIENT` bus subscriber):
   `RegisterParticleProvidersEvent.registerSpriteSet(type, provider)`

## Tips

- Always use `FromArgb(255, r, g, b)` — the alpha byte matters. Full transparency is `FromArgb(0, ...)`.
- Call `$bmp.Dispose()` when done to release the GDI handle.
- In-game GUI colors are ARGB hex (e.g., `0xFF7850A0`). Texture PNGs are standard RGBA.
- For multi-face blocks (top/side/bottom), create separate textures or use a single texture with
  the model's UV mapping pointing at different regions.
