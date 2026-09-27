package com.sebas.arcanemod.item;

import com.sebas.arcanemod.client.ClientCodexOpener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class CodexArcanumItem extends Item {
    public CodexArcanumItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // Screen-construction lives in ClientCodexOpener, not here — see its class javadoc
        // for why that split (rather than just this isClientSide() guard) is what actually
        // keeps CodexScreen/Screen off a dedicated server's classloader.
        if (level.isClientSide()) {
            ClientCodexOpener.open();
        }
        return InteractionResult.SUCCESS;
    }
}