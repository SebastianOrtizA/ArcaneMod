package com.sebas.arcanemod.client;

import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.entity.ModEntityTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers renderers for the Frayed mobs — each is a thin subclass of the vanilla renderer that
 * only overrides {@code getTextureLocation}, reusing vanilla's own model/animation entirely.
 * Kept {@code value = Dist.CLIENT} for the same reason as {@code ParticleEvents}: these touch
 * client-only rendering classes and must never load on a dedicated server.
 */
@Mod.EventBusSubscriber(modid = ArcaneMod.MODID, value = Dist.CLIENT)
public class FrayedMobRenderers {
    private static final Identifier ZOMBIE_TEXTURE =
            Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "textures/entity/frayed_zombie.png");
    private static final Identifier SKELETON_TEXTURE =
            Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "textures/entity/frayed_skeleton.png");
    private static final Identifier SPIDER_TEXTURE =
            Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "textures/entity/frayed_spider.png");

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.FRAYED_ZOMBIE.get(), FrayedZombieRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FRAYED_SKELETON.get(), FrayedSkeletonRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FRAYED_SPIDER.get(), FrayedSpiderRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SPARK_BOLT.get(), NoopRenderer::new);
    }

    private static class FrayedZombieRenderer extends ZombieRenderer {
        FrayedZombieRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public Identifier getTextureLocation(ZombieRenderState state) {
            return ZOMBIE_TEXTURE;
        }
    }

    private static class FrayedSkeletonRenderer extends SkeletonRenderer {
        FrayedSkeletonRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public Identifier getTextureLocation(SkeletonRenderState state) {
            return SKELETON_TEXTURE;
        }
    }

    private static class FrayedSpiderRenderer extends SpiderRenderer<com.sebas.arcanemod.entity.FrayedSpider> {
        FrayedSpiderRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return SPIDER_TEXTURE;
        }
    }
}
