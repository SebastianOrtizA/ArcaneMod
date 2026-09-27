package com.sebas.arcanemod.client;

import com.sebas.arcanemod.ArcaneMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Checkpoint 4 of the Fray plan section: client-side feedback for both Fray meters. Regional Fray
 * (the chunk you're standing in, and its neighbors — see {@code FrayEvents}) tints and pulls in
 * the fog; personal Fray (your own corruption) darkens the screen edges with a round vignette and
 * occasionally plays a whisper. Both read from the plain static holders ({@link ClientChunkFray},
 * {@link ClientPersonalFray}) that {@code FrayEvents} keeps synced from the server.
 */
@Mod.EventBusSubscriber(modid = ArcaneMod.MODID, value = Dist.CLIENT)
public class FrayRenderEvents {
    private static final Identifier VIGNETTE_LAYER_ID = Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "fray_vignette");
    private static final Identifier VIGNETTE_TEXTURE = Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "textures/gui/fray_vignette.png");
    private static final int VIGNETTE_TEXTURE_SIZE = 256;

    private static final int REGIONAL_FRAY_THRESHOLD = 20;
    /** Dark, near-black violet — this needs to read as "wrong," not just "a bit foggy." */
    private static final float FOG_TINT_R = 0.06F;
    private static final float FOG_TINT_G = 0.03F;
    private static final float FOG_TINT_B = 0.09F;
    private static final float MAX_FOG_COLOR_BLEND = 0.9F;
    /** At full severity the fog boundary pulls in to this fraction of its normal distance. */
    private static final float MIN_FOG_DISTANCE_FACTOR = 0.25F;

    private static final int VIGNETTE_LOW_THRESHOLD = 20;
    private static final int VIGNETTE_HIGH_THRESHOLD = 50;
    private static final int VIGNETTE_BAND_WIDTH = 40;

    /** Roughly once a minute when eligible, scaling up as personal Fray climbs toward 100. */
    private static final float WHISPER_BASE_CHANCE_PER_TICK = 1f / 1200f;

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        float severity = regionalSeverity();
        if (severity <= 0F) return;

        float blend = severity * MAX_FOG_COLOR_BLEND;
        event.setRed(lerp(event.getRed(), FOG_TINT_R, blend));
        event.setGreen(lerp(event.getGreen(), FOG_TINT_G, blend));
        event.setBlue(lerp(event.getBlue(), FOG_TINT_B, blend));
    }

    /**
     * Conditionally-cancelling listeners on a {@code Cancellable} event return {@code boolean}
     * instead of {@code void} — returning {@code true} is what actually applies the plane-distance
     * changes (per the event's own doc: "the event must be cancelled for changes to take effect").
     */
    @SubscribeEvent
    public static boolean onRenderFog(ViewportEvent.RenderFog event) {
        float severity = regionalSeverity();
        if (severity <= 0F) return false;

        float factor = 1F - severity * (1F - MIN_FOG_DISTANCE_FACTOR);
        event.scaleNearPlaneDistance(factor);
        event.scaleFarPlaneDistance(factor);
        return true;
    }

    /** 0 at/below the threshold, ramping to 1 by regional Fray 100. */
    private static float regionalSeverity() {
        int regionalFray = ClientChunkFray.get();
        if (regionalFray <= REGIONAL_FRAY_THRESHOLD) return 0F;
        return Math.min(1F, (regionalFray - REGIONAL_FRAY_THRESHOLD) / (100F - REGIONAL_FRAY_THRESHOLD));
    }

    @SubscribeEvent
    public static void onAddLayers(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().add(VIGNETTE_LAYER_ID, FrayRenderEvents::renderVignette);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        int personalFray = ClientPersonalFray.get();
        if (personalFray <= VIGNETTE_LOW_THRESHOLD) return;

        float chance = WHISPER_BASE_CHANCE_PER_TICK * (1F + personalFray / 50F);
        if (mc.level.getRandom().nextFloat() >= chance) return;

        mc.level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.6F, 0.7F, false);
    }

    /**
     * A real round vignette needs a radial alpha falloff, which {@code fillGradient} can't do —
     * it only interpolates along one fixed axis, which is why the original edge-band version of
     * this looked like two vertical bars rather than a vignette. Blitting a pre-baked radial
     * texture (transparent center, opaque corners) stretched to the screen and tinted with the
     * desired color/alpha gets the actual round look for free.
     */
    private static void renderVignette(GuiGraphicsExtractor gg, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gui.screen() != null) return;

        int personalFray = ClientPersonalFray.get();
        if (personalFray <= VIGNETTE_LOW_THRESHOLD) return;

        float severity = Math.min(1F, (personalFray - VIGNETTE_LOW_THRESHOLD) / (float) VIGNETTE_BAND_WIDTH);
        float pulse = 0.85F + 0.15F * (float) Math.sin(mc.level.getGameTime() * 0.05);
        float maxAlpha = (personalFray >= VIGNETTE_HIGH_THRESHOLD ? 0.7F : 0.4F) * severity * pulse;

        int width = gg.guiWidth();
        int height = gg.guiHeight();
        int tint = withAlpha(0x1A0022, maxAlpha);

        gg.blit(RenderPipelines.GUI_TEXTURED, VIGNETTE_TEXTURE, 0, 0, 0F, 0F, width, height,
                VIGNETTE_TEXTURE_SIZE, VIGNETTE_TEXTURE_SIZE, VIGNETTE_TEXTURE_SIZE, VIGNETTE_TEXTURE_SIZE, tint);
    }

    private static int withAlpha(int rgb, float alpha) {
        int a = Math.round(Math.max(0F, Math.min(1F, alpha)) * 255F);
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    private static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }
}
