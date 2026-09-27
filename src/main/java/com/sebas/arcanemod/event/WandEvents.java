package com.sebas.arcanemod.event;

import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.item.ModularWandItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ArcaneMod.MODID)
public class WandEvents {

    @SubscribeEvent
    public static boolean onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() == InteractionHand.OFF_HAND) {
            ItemStack mainHand = event.getEntity().getMainHandItem();
            if (mainHand.getItem() instanceof ModularWandItem wand && wand.isStaff()) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static boolean onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() == InteractionHand.OFF_HAND) {
            ItemStack mainHand = event.getEntity().getMainHandItem();
            if (mainHand.getItem() instanceof ModularWandItem wand && wand.isStaff()) {
                return true;
            }
        }
        return false;
    }
}
