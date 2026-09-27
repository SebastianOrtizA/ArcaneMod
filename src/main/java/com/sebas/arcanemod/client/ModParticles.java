package com.sebas.arcanemod.client;

import com.sebas.arcanemod.ArcaneMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Particle TYPE registration — this part is common (registered on both dists, like any other
 * registry object), unlike the actual rendering, which is client-only (see
 * {@link ParticleEvents}). Keeping this in the {@code client} package anyway since nothing
 * outside client-only code (Wellspring/wand effects) references it.
 */
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, ArcaneMod.MODID);

    /**
     * The mod's signature magical spark — currently used for the Wellspring's idle shimmer and
     * the wand's channeling effect. Its look is entirely controlled by:
     * - assets/arcanemod/particles/wyrd_spark.json (lists which texture(s) it cycles through)
     * - assets/arcanemod/textures/particle/wyrd_spark.png (the actual sprite)
     * Add more frames to the json's texture list for an animated swirl instead of a static spark.
     */
    public static final RegistryObject<SimpleParticleType> WYRD_SPARK =
            PARTICLE_TYPES.register("wyrd_spark", () -> new SimpleParticleType(false));
}
