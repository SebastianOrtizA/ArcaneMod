package com.sebas.arcanemod.network;

import com.sebas.arcanemod.core.facet.Facet;
import com.sebas.arcanemod.core.research.FacetConnection;
import com.sebas.arcanemod.core.research.ResearchNode;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wire format for {@link ResearchNode} and the full research tree — synced whole (see
 * {@code SyncResearchTreePacket}) rather than looked up on demand, the same way vanilla syncs
 * the whole advancement tree rather than fetching one advancement at a time. The client needs
 * every node's metadata (not just which ones are completed) to render locked/available entries
 * in the Loom screen.
 */
final class ResearchStreamCodecs {
    private static final StreamCodec<ByteBuf, List<Facet>> FACET_LIST =
            ByteBufCodecs.collection(ArrayList::new, FacetStreamCodecs.FACET);
    private static final StreamCodec<ByteBuf, List<Identifier>> IDENTIFIER_LIST =
            ByteBufCodecs.collection(ArrayList::new, Identifier.STREAM_CODEC);

    private static final StreamCodec<ByteBuf, FacetConnection> CONNECTION = StreamCodec.composite(
            FacetStreamCodecs.FACET, FacetConnection::a,
            FacetStreamCodecs.FACET, FacetConnection::b,
            FacetConnection::new
    );
    private static final StreamCodec<ByteBuf, List<FacetConnection>> CONNECTION_LIST =
            ByteBufCodecs.collection(ArrayList::new, CONNECTION);

    static final StreamCodec<ByteBuf, ResearchNode> NODE = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ResearchNode::section,
            ByteBufCodecs.STRING_UTF8, ResearchNode::displayName,
            ByteBufCodecs.STRING_UTF8, ResearchNode::description,
            FACET_LIST, ResearchNode::requiredFacets,
            ByteBufCodecs.VAR_INT, ResearchNode::difficulty,
            IDENTIFIER_LIST, ResearchNode::prerequisites,
            IDENTIFIER_LIST, ResearchNode::unlocks,
            ByteBufCodecs.STRING_UTF8, ResearchNode::loreText,
            CONNECTION_LIST, ResearchNode::pattern,
            ResearchNode::new
    );

    static final StreamCodec<ByteBuf, Map<Identifier, ResearchNode>> TREE =
            ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, NODE);

    private ResearchStreamCodecs() {}
}
