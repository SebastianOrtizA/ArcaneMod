package com.sebas.arcanemod.client.gui;

import com.sebas.arcanemod.ArcaneMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class CodexTabButton extends AbstractButton {

    private static final Identifier BACKGROUND_UNSELECTED =
            Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "textures/gui/tab_baseunselected.png");
    private static final Identifier BACKGROUND_SELECTED =
            Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "textures/gui/tab_baseselected.png");
    private static final int ICON_PADDING = 8;

    private final Supplier<ItemStack> icon;
    private final @Nullable Identifier customIconTexture;
    private final BooleanSupplier selected;
    private final Runnable onSelect;

    public CodexTabButton(int x, int y, int size, Component name, Supplier<ItemStack> icon,
                           @Nullable Identifier customIconTexture, BooleanSupplier selected, Runnable onSelect) {
        super(x, y, size, size, name);
        this.icon = icon;
        this.customIconTexture = customIconTexture;
        this.selected = selected;
        this.onSelect = onSelect;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        onSelect.run();
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();

        Identifier background = selected.getAsBoolean() ? BACKGROUND_SELECTED : BACKGROUND_UNSELECTED;
        graphics.blit(background, x, y, x + w, y + h, 0.0F, 1.0F, 0.0F, 1.0F);

        if (customIconTexture != null) {
            int iconSize = Math.min(w, h) - ICON_PADDING;
            int iconX = x + (w - iconSize) / 2;
            int iconY = y + (h - iconSize) / 2;
            graphics.blit(customIconTexture, iconX, iconY, iconX + iconSize, iconY + iconSize, 0.0F, 1.0F, 0.0F, 1.0F);
        } else {
            int iconX = x + (w - 16) / 2;
            int iconY = y + (h - 16) / 2;
            graphics.item(icon.get(), iconX, iconY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
