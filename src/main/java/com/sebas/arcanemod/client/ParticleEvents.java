package com.sebas.arcanemod.client;

import com.sebas.arcanemod.ArcaneMod;
import net.minecraft.client.particle.SpellParticle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers how {@link ModParticles#WYRD_SPARK} actually renders. Kept in its own
 * {@code value = Dist.CLIENT} class — same reasoning as {@link ClientCodexOpener}: this touches
 * client-only classes ({@code SpellParticle}), and {@code @Mod.EventBusSubscriber(dist=CLIENT)}
 * classes are skipped by Forge's annotation scan rather than loaded, so this never gets near a
 * dedicated server's classloader.
 * <p>
 * Reuses vanilla's {@code SpellParticle.Provider} outright — it's the exact swirling-glow
 * particle behavior used by things like {@code minecraft:enchant}, just pointed at our own
 * sprite instead of vanilla's.
 */
@Mod.EventBusSubscriber(modid = ArcaneMod.MODID, value = Dist.CLIENT)
public class ParticleEvents {
    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.WYRD_SPARK.get(), SpellParticle.Provider::new);
    }
}
