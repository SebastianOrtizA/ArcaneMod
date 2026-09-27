package com.sebas.arcanemod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class WandInlayItem extends Item {
    private final String effectKey;

    public WandInlayItem(Properties properties, String effectKey) {
        super(properties);
        this.effectKey = effectKey;
    }

    public String getEffectKey() { return effectKey; }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.arcanemod.inlay_effect." + effectKey));
    }
}
