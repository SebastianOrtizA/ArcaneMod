package com.sebas.arcanemod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class WandCoreItem extends Item {
    private final int baseCapacity;
    private final double efficiency;

    public WandCoreItem(Properties properties, int baseCapacity, double efficiency) {
        super(properties);
        this.baseCapacity = baseCapacity;
        this.efficiency = efficiency;
    }

    public int getBaseCapacity() { return baseCapacity; }
    public double getEfficiency() { return efficiency; }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.arcanemod.core_capacity", baseCapacity));
        tooltip.accept(Component.translatable("tooltip.arcanemod.core_efficiency",
                String.format("%.0f%%", efficiency * 100)));
    }
}
