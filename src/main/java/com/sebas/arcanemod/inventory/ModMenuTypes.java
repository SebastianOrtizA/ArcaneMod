package com.sebas.arcanemod.inventory;

import com.sebas.arcanemod.ArcaneMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, ArcaneMod.MODID);

    public static final RegistryObject<MenuType<WandwrightsBenchMenu>> WANDWRIGHTS_BENCH =
            MENU_TYPES.register("wandwrights_bench",
                    () -> new MenuType<>(WandwrightsBenchMenu::clientFactory, FeatureFlags.VANILLA_SET));
}
