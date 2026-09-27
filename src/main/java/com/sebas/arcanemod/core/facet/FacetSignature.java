package com.sebas.arcanemod.core.facet;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An immutable Facet → amount map — a block/item/mob's magical "signature". Built with
 * {@link #of(Map)}; two blocks with the same signature are meant to compare equal.
 */
public final class FacetSignature {
    public static final FacetSignature EMPTY = new FacetSignature(Map.of());

    private final Map<Facet, Integer> amounts;

    private FacetSignature(Map<Facet, Integer> amounts) {
        this.amounts = amounts;
    }

    /** Amounts of 0 or less are dropped; an all-empty/non-positive input collapses to {@link #EMPTY}. */
    public static FacetSignature of(Map<Facet, Integer> amounts) {
        Map<Facet, Integer> positive = amounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, () -> new EnumMap<>(Facet.class)));
        return positive.isEmpty() ? EMPTY : new FacetSignature(Map.copyOf(positive));
    }

    public int amountOf(Facet facet) {
        return amounts.getOrDefault(facet, 0);
    }

    public boolean isEmpty() {
        return amounts.isEmpty();
    }

    /** Read-only view, iterating in {@link Facet} declaration order. */
    public Map<Facet, Integer> asMap() {
        return amounts;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FacetSignature other && amounts.equals(other.amounts);
    }

    @Override
    public int hashCode() {
        return amounts.hashCode();
    }

    @Override
    public String toString() {
        return amounts.toString();
    }
}
