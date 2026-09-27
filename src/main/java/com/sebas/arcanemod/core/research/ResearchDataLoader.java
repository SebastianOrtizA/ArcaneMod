package com.sebas.arcanemod.core.research;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Loads {@code data/<namespace>/research/*.json} into {@link ResearchTree} on every resource
 * reload. Registered via {@code AddReloadListenerEvent} — see {@code event.ResearchEvents}.
 * <p>
 * Unlike {@code FacetDataLoader}, each node's id IS the file's own derived location (there's no
 * separate vanilla-coverage computation here that would need tag binding), so this can extend the
 * simpler {@code SimpleJsonResourceReloadListener<ResearchNode>} base directly instead of driving
 * {@code scanDirectory} by hand.
 */
public class ResearchDataLoader extends SimpleJsonResourceReloadListener<ResearchNode> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ResearchDataLoader() {
        super(ResearchNode.CODEC, FileToIdConverter.json("research"));
    }

    @Override
    protected void apply(Map<Identifier, ResearchNode> data, ResourceManager manager, ProfilerFiller profiler) {
        ResearchTree.set(data);
        LOGGER.info("Research tree ready: {} nodes", data.size());
    }
}
