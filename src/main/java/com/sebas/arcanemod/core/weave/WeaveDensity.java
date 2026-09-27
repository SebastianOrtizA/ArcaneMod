package com.sebas.arcanemod.core.weave;

import com.sebas.arcanemod.ArcaneMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Maps a biome to its base Weave density. Config-driven via biome tags rather than hardcoded
 * biome IDs, so datapacks/other mods can extend the buckets without touching Java, and modded
 * biomes fall into sensible defaults through vanilla's own biome-tag conventions.
 * <p>
 * Vanilla has no "is_mushroom" or "is_desert"/"is_plains" tags, so those two buckets are our
 * own tags ({@code data/arcanemod/tags/worldgen/biome/}) — {@code arid_biomes} pulls in
 * {@code #minecraft:is_badlands} alongside desert/plains so that bucket doesn't need its own
 * Java-side badlands check.
 */
public final class WeaveDensity {
    private WeaveDensity() {}

    public static final TagKey<Biome> DENSE_WEAVE_BIOMES =
            TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "dense_weave_biomes"));
    public static final TagKey<Biome> ARID_BIOMES =
            TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "arid_biomes"));

    public static final float DEFAULT_DENSITY = 0.5f;

    public static float baseDensityFor(Holder<Biome> biome) {
        if (biome.is(DENSE_WEAVE_BIOMES)) {
            return 0.9f; // mushroom fields
        }
        if (biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_JUNGLE)) {
            return 0.75f;
        }
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN)) {
            return 0.2f;
        }
        if (biome.is(ARID_BIOMES)) {
            return 0.35f; // plains/desert/badlands
        }
        return DEFAULT_DENSITY;
    }
}
