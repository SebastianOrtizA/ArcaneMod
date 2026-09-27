package com.sebas.arcanemod.core.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.sebas.arcanemod.core.facet.Facet;

import java.util.List;

/**
 * One required thread in a research node's puzzle pattern — an unordered pair of Facets the
 * player must connect on the Loom's board. {@link #of} always normalizes by {@code Facet}
 * ordinal, so {@code of(TERRA, IGNIS)} and {@code of(IGNIS, TERRA)} produce an equal record —
 * needed so a player's drawn connection (in whichever click order they made it) can be compared
 * against the node's target pattern with plain {@code Set#equals}.
 */
public record FacetConnection(Facet a, Facet b) {
    public static FacetConnection of(Facet x, Facet y) {
        return x.ordinal() <= y.ordinal() ? new FacetConnection(x, y) : new FacetConnection(y, x);
    }

    public static final Codec<FacetConnection> CODEC = Codec.STRING.listOf().comapFlatMap(
            list -> {
                if (list.size() != 2) {
                    return DataResult.error(() -> "A facet connection needs exactly 2 facets, got " + list.size());
                }
                try {
                    Facet x = Facet.valueOf(list.get(0).toUpperCase(java.util.Locale.ROOT));
                    Facet y = Facet.valueOf(list.get(1).toUpperCase(java.util.Locale.ROOT));
                    return DataResult.success(FacetConnection.of(x, y));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "Unknown facet in connection: " + list);
                }
            },
            connection -> List.of(
                    connection.a().name().toLowerCase(java.util.Locale.ROOT),
                    connection.b().name().toLowerCase(java.util.Locale.ROOT))
    );
}
