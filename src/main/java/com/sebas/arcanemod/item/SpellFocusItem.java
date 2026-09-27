package com.sebas.arcanemod.item;

import com.sebas.arcanemod.core.ModDataComponents;
import com.sebas.arcanemod.core.wand.FocusData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class SpellFocusItem extends Item {
    public SpellFocusItem(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        FocusData data = stack.get(ModDataComponents.FOCUS_DATA.get());
        if (data != null) {
            tooltip.accept(Component.translatable("tooltip.arcanemod.focus_spell",
                    Component.translatable("spell.arcanemod." + data.spell().getPath())));
            tooltip.accept(Component.translatable("tooltip.arcanemod.focus_charges",
                    data.charges(), data.maxCharges()));
            if (data.isInert()) {
                tooltip.accept(Component.translatable("tooltip.arcanemod.focus_inert"));
            }
        }
    }
}
