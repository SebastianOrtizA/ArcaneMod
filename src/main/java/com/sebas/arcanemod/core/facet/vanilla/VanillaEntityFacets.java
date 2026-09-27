package com.sebas.arcanemod.core.facet.vanilla;

import com.sebas.arcanemod.core.facet.Facet;
import com.sebas.arcanemod.core.facet.FacetSignature;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.sebas.arcanemod.core.facet.Facet.*;

/**
 * Facet signatures for mobs. Unlike blocks/items, vanilla's EntityType roster is small enough
 * (~100 entries) to mostly hand-curate directly rather than lean on tags — only a handful of
 * broad groupings ({@code UNDEAD}, {@code RAIDERS}/{@code ILLAGER}, {@code ARTHROPOD},
 * {@code AQUATIC}) exist as vanilla tags at all, and the individually-named entries below take
 * priority over even those.
 */
final class VanillaEntityFacets {
    private VanillaEntityFacets() {}

    /** Not creatures in any sense a Resonometer scan should treat as "a mob". */
    private static final Set<String> EXCLUDED = Set.of(
            "player", "item", "experience_orb", "arrow", "spectral_arrow", "trident", "snowball",
            "egg", "ender_pearl", "fireball", "small_fireball", "dragon_fireball", "wither_skull",
            "shulker_bullet", "wind_charge", "breeze_wind_charge", "fishing_bobber", "boat",
            "chest_boat", "minecart", "chest_minecart", "furnace_minecart", "hopper_minecart",
            "spawner_minecart", "tnt_minecart", "command_block_minecart", "falling_block",
            "area_effect_cloud", "armor_stand", "item_frame", "glow_item_frame", "painting",
            "leash_knot", "marker", "interaction", "text_display", "block_display", "item_display",
            "evoker_fangs", "lightning_bolt", "tnt", "lingering_potion", "eye_of_ender"
    );

    private static final Map<Identifier, FacetSignature> OVERRIDES = new HashMap<>();

    private static void mob(String path, Facet a, int aAmt) {
        OVERRIDES.put(Identifier.withDefaultNamespace(path), of(a, aAmt));
    }

    private static void mob(String path, Facet a, int aAmt, Facet b, int bAmt) {
        OVERRIDES.put(Identifier.withDefaultNamespace(path), of(a, aAmt, b, bAmt));
    }

    private static void mob(String path, Facet a, int aAmt, Facet b, int bAmt, Facet c, int cAmt) {
        OVERRIDES.put(Identifier.withDefaultNamespace(path), of(a, aAmt, b, bAmt, c, cAmt));
    }

    private static FacetSignature of(Facet a, int aAmt) {
        return FacetSignature.of(Map.of(a, aAmt));
    }

    private static FacetSignature of(Facet a, int aAmt, Facet b, int bAmt) {
        return FacetSignature.of(Map.of(a, aAmt, b, bAmt));
    }

    private static FacetSignature of(Facet a, int aAmt, Facet b, int bAmt, Facet c, int cAmt) {
        return FacetSignature.of(Map.of(a, aAmt, b, bAmt, c, cAmt));
    }

    private static final Map<TagKey<EntityType<?>>, FacetSignature> TAG_RULES = new LinkedHashMap<>();

    private static void tagRule(TagKey<EntityType<?>> tag, Facet a, int aAmt, Facet b, int bAmt) {
        TAG_RULES.put(tag, of(a, aAmt, b, bAmt));
    }

    static {
        // --- The End ---
        mob("enderman", MOTUS, 2, UMBRA, 2);
        mob("endermite", UMBRA, 1, MOTUS, 2);
        mob("shulker", ORDO, 2, UMBRA, 1);
        mob("ender_dragon", COGNITIO, 2, ORDO, 2, UMBRA, 1);

        // --- The Nether ---
        mob("blaze", IGNIS, 3);
        mob("ghast", AER, 2, IGNIS, 1, MORTIS, 1);
        mob("magma_cube", IGNIS, 2, MOTUS, 1);
        mob("wither", MORTIS, 3, UMBRA, 2);
        mob("wither_skeleton", MORTIS, 2, UMBRA, 2);
        mob("piglin", IGNIS, 1, PERDO, 1);
        mob("piglin_brute", IGNIS, 1, PERDO, 2);
        mob("hoglin", IGNIS, 1, VITA, 1);
        mob("zoglin", IGNIS, 1, MORTIS, 2);
        mob("strider", IGNIS, 2, MOTUS, 1);

        // --- The deep dark ---
        mob("warden", UMBRA, 3, MOTUS, 2, MORTIS, 2);
        mob("sniffer", COGNITIO, 1, VITA, 2);

        // --- Overworld hostiles ---
        mob("creeper", IGNIS, 1, PERDO, 3);
        mob("silverfish", PERDO, 2, TERRA, 1);
        mob("witch", UMBRA, 2, COGNITIO, 2);
        mob("vex", UMBRA, 2, MOTUS, 2);
        mob("evoker", UMBRA, 2, COGNITIO, 2);
        mob("vindicator", PERDO, 2, MOTUS, 1);
        mob("pillager", PERDO, 1, MOTUS, 1);
        mob("ravager", PERDO, 2, TERRA, 1);
        mob("illusioner", UMBRA, 2, COGNITIO, 2);

        // --- Constructs ---
        mob("iron_golem", ORDO, 2, TERRA, 1);
        mob("snow_golem", ORDO, 1, AQUA, 1);

        // --- Villagers & allies ---
        mob("villager", COGNITIO, 1, ORDO, 1);
        mob("wandering_trader", COGNITIO, 1, MOTUS, 1);
        mob("allay", AER, 1, LUX, 1);

        // --- Water life ---
        mob("dolphin", VITA, 1, AQUA, 2, COGNITIO, 1);
        mob("turtle", VITA, 1, AQUA, 2);
        mob("axolotl", VITA, 2, AQUA, 2);
        mob("guardian", AQUA, 2, ORDO, 1);
        mob("elder_guardian", AQUA, 2, ORDO, 1, MORTIS, 1);
        mob("squid", AQUA, 2, VITA, 1);
        mob("glow_squid", AQUA, 2, LUX, 1);
        mob("frog", VITA, 1, AQUA, 1);
        mob("tadpole", VITA, 1, AQUA, 2);

        // --- Air / flight ---
        mob("bee", AER, 1, VITA, 1);
        mob("parrot", AER, 1, VITA, 1);
        mob("bat", AER, 2, UMBRA, 1);
        mob("phantom", AER, 2, UMBRA, 1, MORTIS, 1);
        mob("happy_ghast", AER, 2, VITA, 1);

        // --- Everyday animals ---
        mob("wolf", VITA, 1, MOTUS, 1);
        mob("cat", VITA, 1, MOTUS, 1);
        mob("rabbit", VITA, 1, MOTUS, 1);
        mob("fox", VITA, 1, MOTUS, 1);
        mob("panda", VITA, 2);
        mob("polar_bear", VITA, 1, MOTUS, 1);
        mob("llama", VITA, 1, MOTUS, 1);
        mob("trader_llama", VITA, 1, MOTUS, 1);
        mob("camel", VITA, 1, MOTUS, 1);
        mob("goat", VITA, 1, MOTUS, 2);
        mob("mooshroom", VITA, 2, UMBRA, 1);
        mob("armadillo", VITA, 1, ORDO, 1);
        mob("horse", VITA, 1, MOTUS, 2);
        mob("donkey", VITA, 1, MOTUS, 2);
        mob("mule", VITA, 1, MOTUS, 2);
        mob("skeleton_horse", MORTIS, 1, MOTUS, 2);
        mob("zombie_horse", MORTIS, 1, MOTUS, 2);
        mob("chicken", VITA, 1);
        mob("cow", VITA, 1);
        mob("pig", VITA, 1);
        mob("sheep", VITA, 1);
    }

    static {
        tagRule(EntityTypeTags.UNDEAD, MORTIS, 2, UMBRA, 1);
        tagRule(EntityTypeTags.RAIDERS, PERDO, 1, UMBRA, 1);
        tagRule(EntityTypeTags.ILLAGER, PERDO, 1, UMBRA, 1);
        tagRule(EntityTypeTags.ARTHROPOD, UMBRA, 1, MOTUS, 1);
        tagRule(EntityTypeTags.AQUATIC, VITA, 1, AQUA, 2);
    }

    /** @return null if this entity type should get no entry at all (not a "creature"). */
    static FacetSignature resolve(EntityType<?> type, Identifier id) {
        if (id == null || EXCLUDED.contains(id.getPath())) return null;

        FacetSignature override = OVERRIDES.get(id);
        if (override != null) return override;

        for (Map.Entry<TagKey<EntityType<?>>, FacetSignature> entry : TAG_RULES.entrySet()) {
            if (type.builtInRegistryHolder().is(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Default: anything alive not otherwise categorized still carries a trace of Vita.
        return FacetSignature.of(Map.of(VITA, 1));
    }
}
