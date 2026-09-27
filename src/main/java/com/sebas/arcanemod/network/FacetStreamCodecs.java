package com.sebas.arcanemod.network;

import com.sebas.arcanemod.core.facet.CompoundFacet;
import com.sebas.arcanemod.core.facet.Facet;
import com.sebas.arcanemod.core.facet.FacetSignature;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Facet/CompoundFacet/FacetSignature don't need registry access to encode, so these are plain
 * {@code ByteBuf} codecs — Java generics let a {@code StreamCodec<ByteBuf, X>} stand in anywhere
 * a {@code StreamCodec<? super SomeRegistryBuf, X>} is expected (a {@code RegistryFriendlyByteBuf}
 * IS-A {@code ByteBuf}), so both {@link SyncFacetKnowledgePacket} (play-protocol, needs
 * {@code RegistryFriendlyByteBuf}) and {@link ScanResultPacket} can share these.
 */
final class FacetStreamCodecs {
    static final StreamCodec<ByteBuf, Facet> FACET =
            ByteBufCodecs.idMapper(id -> Facet.values()[id], Facet::ordinal);
    static final StreamCodec<ByteBuf, CompoundFacet> COMPOUND_FACET =
            ByteBufCodecs.idMapper(id -> CompoundFacet.values()[id], CompoundFacet::ordinal);

    static final StreamCodec<ByteBuf, Set<Facet>> FACET_SET =
            ByteBufCodecs.collection(size -> EnumSet.noneOf(Facet.class), FACET);
    static final StreamCodec<ByteBuf, Set<CompoundFacet>> COMPOUND_FACET_SET =
            ByteBufCodecs.collection(size -> EnumSet.noneOf(CompoundFacet.class), COMPOUND_FACET);

    private static final StreamCodec<ByteBuf, Map<Facet, Integer>> FACET_AMOUNTS =
            ByteBufCodecs.map(size -> new EnumMap<>(Facet.class), FACET, ByteBufCodecs.VAR_INT);

    static final StreamCodec<ByteBuf, FacetSignature> SIGNATURE = FACET_AMOUNTS.map(FacetSignature::of, FacetSignature::asMap);

    private FacetStreamCodecs() {}
}
