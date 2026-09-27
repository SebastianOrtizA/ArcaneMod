package com.sebas.arcanemod;

import com.mojang.logging.LogUtils;
import com.sebas.arcanemod.blockentity.ModBlockEntities;
import com.sebas.arcanemod.core.ModDataComponents;
import com.sebas.arcanemod.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import com.sebas.arcanemod.creative.ModCreativeTabs;
import com.sebas.arcanemod.block.ModBlocks;
import com.sebas.arcanemod.client.ModParticles;
import com.sebas.arcanemod.data.ArcaneDataGenerators;
import com.sebas.arcanemod.inventory.ModMenuTypes;
import com.sebas.arcanemod.network.ModNetwork;
import com.sebas.arcanemod.entity.ModEntityTypes;
import com.sebas.arcanemod.worldgen.ModStructures;
import net.minecraftforge.data.event.GatherDataEvent;

@Mod(ArcaneMod.MODID)
public final class ArcaneMod {
    public static final String MODID = "arcanemod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ArcaneMod(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();

        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::commonSetup);
        GatherDataEvent.getBus(modBusGroup).addListener(ArcaneDataGenerators::gatherData);

        // Registries
        ModBlocks.BLOCKS.register(modBusGroup);
        ModBlockEntities.BLOCK_ENTITIES.register(modBusGroup);
        ModDataComponents.DATA_COMPONENTS.register(modBusGroup);
        ModItems.ITEMS.register(modBusGroup);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modBusGroup);
        ModParticles.PARTICLE_TYPES.register(modBusGroup);
        ModEntityTypes.ENTITY_TYPES.register(modBusGroup);
        ModMenuTypes.MENU_TYPES.register(modBusGroup);
        ModStructures.STRUCTURE_TYPES.register(modBusGroup);
        ModStructures.STRUCTURE_PIECES.register(modBusGroup);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
        ModNetwork.register();
        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);
        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
            net.minecraft.client.gui.screens.MenuScreens.register(
                    ModMenuTypes.WANDWRIGHTS_BENCH.get(),
                    com.sebas.arcanemod.client.gui.WandwrightsBenchScreen::new);
            com.sebas.arcanemod.client.ModKeybinds.init();
        }
    }
}