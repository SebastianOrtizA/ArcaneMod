package com.sebas.arcanemod.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.item.ModularWandItem;
import com.sebas.arcanemod.network.CycleFocusPacket;
import com.sebas.arcanemod.network.ModNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {
    private static final KeyMapping.Category ARCANE_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "arcane"));

    public static final KeyMapping CYCLE_FOCUS = new KeyMapping(
            "key.arcanemod.cycle_focus",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            ARCANE_CATEGORY
    );

    public static void init() {
        RegisterKeyMappingsEvent.BUS.addListener(ModKeybinds::onRegisterKeys);
        TickEvent.ClientTickEvent.Post.BUS.addListener(ModKeybinds::onClientTick);
    }

    private static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(CYCLE_FOCUS);
    }

    private static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gui.screen() != null) return;

        while (CYCLE_FOCUS.consumeClick()) {
            if (mc.player.getMainHandItem().getItem() instanceof ModularWandItem
                    || mc.player.getOffhandItem().getItem() instanceof ModularWandItem) {
                ModNetwork.sendToServer(new CycleFocusPacket(1));
            }
        }
    }
}
