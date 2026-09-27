package com.sebas.arcanemod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class WandBindingItem extends Item {
    private final double capacityMultiplier;
    private final double cooldownReduction;
    private final double frayResistance;
    private final String effectKey;

    public WandBindingItem(Properties properties, double capacityMultiplier, double cooldownReduction,
                           double frayResistance, String effectKey) {
        super(properties);
        this.capacityMultiplier = capacityMultiplier;
        this.cooldownReduction = cooldownReduction;
        this.frayResistance = frayResistance;
        this.effectKey = effectKey;
    }

    public double getCapacityMultiplier() { return capacityMultiplier; }
    public double getCooldownReduction() { return cooldownReduction; }
    public double getFrayResistance() { return frayResistance; }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.arcanemod.binding_effect." + effectKey));
    }
}
