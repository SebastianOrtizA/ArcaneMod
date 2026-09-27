package com.sebas.arcanemod.core.facet;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.sebas.arcanemod.core.facet.vanilla.VanillaFacetData;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads {@code data/<namespace>/facets/*.json} into {@link FacetRegistry} on every resource
 * reload. Registered via {@code AddReloadListenerEvent} — see {@code event.FacetEvents}.
 * <p>
 * Format: {@code {"id": "minecraft:coal", "facets": {"ignis": 2, "terra": 1}}}. Note this uses
 * "id" rather than the design doc's "item" — deliberate, since these files also describe blocks
 * and mobs, not just items; "item" read as misleading for those.
 * <p>
 * Each definition file's own filename/location is irrelevant — the target is the "id" field
 * inside it, not the file-derived identifier {@link SimpleJsonResourceReloadListener} normally
 * keys by. That's why this extends the more generic {@code SimplePreparableReloadListener}
 * directly and calls {@code SimpleJsonResourceReloadListener.scanDirectory} itself, rather than
 * extending {@code SimpleJsonResourceReloadListener<FacetDefinition>} — that base class would
 * key its result map by the file's own id, which we'd have to immediately discard anyway.
 * <p>
 * {@link #apply} is also where {@link VanillaFacetData#compute()} runs, computing the bulk
 * rule-based defaults for every vanilla block/item/mob. That needs tag membership
 * ({@code Holder#is(TagKey)}) to actually work, which requires tags to already be bound —
 * {@code apply()} is guaranteed to run after every earlier-registered reload listener (including
 * Minecraft's own tag-binding one) has fully completed, since {@code SimpleReloadInstance} chains
 * each listener's completion into the next one's barrier. This turned out to be the only reliable
 * way to get that guarantee in this Forge build: a standalone datagen tool never binds tags at
 * all, and this build's own {@code TagsUpdatedEvent} firing is dead code (commented out in
 * {@code RecipeManager.java.patch}) — see CLAUDE.md.
 */
public class FacetDataLoader extends SimplePreparableReloadListener<Map<Identifier, FacetSignature>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter LISTER = FileToIdConverter.json("facets");

    @Override
    protected Map<Identifier, FacetSignature> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, FacetDefinition> parsed = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(manager, LISTER, JsonOps.INSTANCE, FacetDefinition.CODEC, parsed);

        Map<Identifier, FacetSignature> result = new HashMap<>();
        parsed.values().forEach(definition -> result.put(definition.id(), definition.signature()));
        return result;
    }

    @Override
    protected void apply(Map<Identifier, FacetSignature> data, ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, FacetSignature> vanillaDefaults = VanillaFacetData.compute();
        FacetRegistry.setVanillaDefaults(vanillaDefaults);
        FacetRegistry.setJsonOverrides(Map.copyOf(data));
        LOGGER.info("Facet data ready: {} vanilla defaults, {} JSON overrides", vanillaDefaults.size(), data.size());
    }
}
