package com.sebas.arcanemod.network;

import com.sebas.arcanemod.core.ModDataComponents;
import com.sebas.arcanemod.core.wand.FocusData;
import com.sebas.arcanemod.item.ModularWandItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.List;

public record CycleFocusPacket(int direction) {

    public static final StreamCodec<RegistryFriendlyByteBuf, CycleFocusPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CycleFocusPacket::direction,
            CycleFocusPacket::new
    );

    public static void handle(CycleFocusPacket packet, CustomPayloadEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player == null) return;

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof ModularWandItem wand)) continue;

            List<FocusData> foci = stack.getOrDefault(ModDataComponents.FOCUS_SLOTS.get(), List.of());
            if (foci.isEmpty()) break;

            int current = stack.getOrDefault(ModDataComponents.ACTIVE_FOCUS.get(), 0);
            int next = (current + packet.direction() % foci.size() + foci.size()) % foci.size();
            stack.set(ModDataComponents.ACTIVE_FOCUS.get(), next);
            break;
        }
    }
}
