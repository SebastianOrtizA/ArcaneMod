package com.sebas.arcanemod.creative;

import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ArcaneMod.MODID);

    public static final RegistryObject<CreativeModeTab> ARCANE_TAB = CREATIVE_MODE_TABS.register("arcane_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> ModItems.WAND.get().getDefaultInstance())
                    .title(Component.translatable("creativetab.arcanemod.arcane_tab"))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.WAND.get());
                        output.accept(ModItems.CODEX_ARCANUM.get());
                        output.accept(ModItems.WYRDSTONE_ORE.get());
                        output.accept(ModItems.DEEPSLATE_WYRDSTONE_ORE.get());
                        output.accept(ModItems.WYRDSTONE.get());
                        output.accept(ModItems.WYRD_DUST.get());
                        output.accept(ModItems.WEAVE_WELLSPRING.get());
                        output.accept(ModItems.RESONOMETER.get());
                        output.accept(ModItems.LOOM_OF_UNDERSTANDING.get());
                        // Stage 1: Wandwright's Benches
                        output.accept(ModItems.WANDWRIGHTS_BENCH.get());
                        output.accept(ModItems.ADVANCED_WANDWRIGHTS_BENCH.get());
                        // Stage 1: Wand Components
                        output.accept(ModItems.WOOD_CORE.get());
                        output.accept(ModItems.WYRDSTONE_CORE.get());
                        output.accept(ModItems.QUICKSILVER_CORE.get());
                        output.accept(ModItems.VOIDGLASS_CORE.get());
                        output.accept(ModItems.COPPER_CAP.get());
                        output.accept(ModItems.IRON_CAP.get());
                        output.accept(ModItems.PRISM_CAP.get());
                        output.accept(ModItems.QUARTZ_CAP.get());
                        output.accept(ModItems.LEATHER_BINDING.get());
                        output.accept(ModItems.SILK_BINDING.get());
                        output.accept(ModItems.WYRDTHREAD_BINDING.get());
                        output.accept(ModItems.VOIDWEAVE_BINDING.get());
                        output.accept(ModItems.RUBY_INLAY.get());
                        output.accept(ModItems.SAPPHIRE_INLAY.get());
                        output.accept(ModItems.EMERALD_INLAY.get());
                        output.accept(ModItems.DIAMOND_INLAY.get());
                        // Stage 1: Modular Wand/Staff & Focus
                        output.accept(ModItems.MODULAR_WAND.get());
                        output.accept(ModItems.MODULAR_STAFF.get());
                        output.accept(ModItems.SPELL_FOCUS.get());
                    })
                    .build());
}