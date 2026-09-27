package com.sebas.arcanemod.client.gui;

import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.inventory.WandwrightsBenchMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
public class WandwrightsBenchScreen extends AbstractContainerScreen<WandwrightsBenchMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "textures/gui/container/wandwrights_bench.png");

    public WandwrightsBenchScreen(WandwrightsBenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                this.leftPos, this.topPos,
                0.0F, 0.0F,
                this.imageWidth, this.imageHeight,
                256, 256);
    }
}
