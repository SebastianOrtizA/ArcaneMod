package com.sebas.arcanemod.core.facet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Locale;

/**
 * The on-disk shape of one {@code data/<namespace>/facets/*.json} entry:
 * {@code {"id": "minecraft:coal", "facets": {"ignis": 2, "terra": 1}}}. Shared between
 * {@link FacetDataLoader} (reads it) and the datagen {@code FacetDataProvider} (writes it), so
 * the schema is defined exactly once.
 */
public record FacetDefinition(Identifier id, FacetSignature signature) {
    private static final Codec<Facet> FACET_CODEC = Codec.STRING.xmap(
            s -> Facet.valueOf(s.toUpperCase(Locale.ROOT)),
            f -> f.name().toLowerCase(Locale.ROOT));

    private static final Codec<FacetSignature> SIGNATURE_CODEC = Codec.unboundedMap(FACET_CODEC, Codec.INT)
            .xmap(FacetSignature::of, FacetSignature::asMap);

    public static final Codec<FacetDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(FacetDefinition::id),
            SIGNATURE_CODEC.fieldOf("facets").forGetter(FacetDefinition::signature)
    ).apply(instance, FacetDefinition::new));
}
