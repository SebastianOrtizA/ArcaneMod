package com.sebas.arcanemod.client;

import com.sebas.arcanemod.ArcaneMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Draws the last Resonometer scan result near the crosshair for a few seconds. Registered as a
 * plain top-level layer (via {@link AddGuiOverlayLayersEvent}) rather than ordered against a
 * specific vanilla layer — it just needs to render on top of everything, and exact stacking
 * against, say, the crosshair isn't worth the extra complexity of locating a nested draw stack
 * for a HUD element that's only visible a few seconds at a time.
 */
@Mod.EventBusSubscriber(modid = ArcaneMod.MODID, value = Dist.CLIENT)
public class ResonometerHud {

    private static final Identifier LAYER_ID = Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "resonometer_scan_result");
    private static final int NAME_COLOR = 0xFFE0C080;
    private static final int LINE_COLOR = 0xFFAADDFF;
    private static final int LINE_HEIGHT = 10;

    @SubscribeEvent
    public static void onAddLayers(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().add(LAYER_ID, ResonometerHud::render);
    }

    private static void render(GuiGraphicsExtractor gg, DeltaTracker deltaTracker) {
        ClientScanResult result = ClientScanResult.current();
        Minecraft mc = Minecraft.getInstance();
        if (result == null || mc.gui.screen() != null) return;

        int centerX = gg.guiWidth() / 2;
        int y = gg.guiHeight() / 2 - 40;

        gg.centeredText(mc.font, result.displayName(), centerX, y, NAME_COLOR);
        y += LINE_HEIGHT;
        for (Component line : result.lines()) {
            gg.centeredText(mc.font, line, centerX, y, LINE_COLOR);
            y += LINE_HEIGHT;
        }
    }
}
