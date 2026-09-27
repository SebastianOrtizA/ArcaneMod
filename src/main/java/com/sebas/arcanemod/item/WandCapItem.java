package com.sebas.arcanemod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class WandCapItem extends Item {
    private final double castSpeedBonus;
    private final double durabilityBonus;
    private final double rangeBonus;
    private final int multiTargetCount;
    private final double multiTargetPower;
    private final String effectKey;

    public WandCapItem(Properties properties, double castSpeedBonus, double durabilityBonus,
                       double rangeBonus, int multiTargetCount, double multiTargetPower,
                       String effectKey) {
        super(properties);
        this.castSpeedBonus = castSpeedBonus;
        this.durabilityBonus = durabilityBonus;
        this.rangeBonus = rangeBonus;
        this.multiTargetCount = multiTargetCount;
        this.multiTargetPower = multiTargetPower;
        this.effectKey = effectKey;
    }

    public double getCastSpeedBonus() { return castSpeedBonus; }
    public double getDurabilityBonus() { return durabilityBonus; }
    public double getRangeBonus() { return rangeBonus; }
    public int getMultiTargetCount() { return multiTargetCount; }
    public double getMultiTargetPower() { return multiTargetPower; }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.arcanemod.cap_effect." + effectKey));
    }
}
