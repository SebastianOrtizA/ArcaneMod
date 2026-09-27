package com.sebas.arcanemod.core;

import com.mojang.serialization.Codec;
import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.core.wand.FocusData;
import com.sebas.arcanemod.core.wand.WandAssembly;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/**
 * Registry holder for custom {@link DataComponentType}s.
 * <p>
 * Data components are the replacement for the old NBT-tag-on-ItemStack approach,
 * and — importantly for us — they persist and sync to the client automatically.
 * That matters because, in this version, ItemStack capabilities do NOT persist or
 * sync on their own (verified by reading the ItemStack Forge patch): capabilities
 * are gathered fresh from the Item type each time, nothing is serialized into the
 * stack. So per-item state that needs to survive a save/reload (like a wand's
 * stored Wyrd) belongs in a data component, not a capability.
 * <p>
 * {@code STORED_WYRD} is a plain int — how much Wyrd an item (currently just the wand) is
 * carrying. persistent(Codec.INT) makes it survive save/reload; networkSynchronized makes the
 * client aware of it too, which is what lets the wand's tooltip show an accurate charge with
 * no custom packet.
 */
public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ArcaneMod.MODID);

    public static final RegistryObject<DataComponentType<Integer>> STORED_WYRD = DATA_COMPONENTS.register("stored_wyrd",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final RegistryObject<DataComponentType<WandAssembly>> WAND_ASSEMBLY = DATA_COMPONENTS.register("wand_assembly",
            () -> DataComponentType.<WandAssembly>builder()
                    .persistent(WandAssembly.CODEC)
                    .networkSynchronized(WandAssembly.STREAM_CODEC)
                    .build());

    public static final RegistryObject<DataComponentType<List<FocusData>>> FOCUS_SLOTS = DATA_COMPONENTS.register("focus_slots",
            () -> DataComponentType.<List<FocusData>>builder()
                    .persistent(FocusData.CODEC.listOf())
                    .networkSynchronized(FocusData.STREAM_CODEC.apply(ByteBufCodecs.list()))
                    .build());

    public static final RegistryObject<DataComponentType<Integer>> ACTIVE_FOCUS = DATA_COMPONENTS.register("active_focus",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final RegistryObject<DataComponentType<FocusData>> FOCUS_DATA = DATA_COMPONENTS.register("focus_data",
            () -> DataComponentType.<FocusData>builder()
                    .persistent(FocusData.CODEC)
                    .networkSynchronized(FocusData.STREAM_CODEC)
                    .build());
}
