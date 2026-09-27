package com.sebas.arcanemod.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

/**
 * Registers this mod's datagen providers. Wired manually in {@code ArcaneMod}'s constructor
 * (`GatherDataEvent.getBus(modBusGroup).addListener(...)`), the same pattern already used for
 * {@code FMLCommonSetupEvent} — {@code GatherDataEvent} is a mod-bus event, not a Forge-bus one,
 * so it doesn't go through {@code @Mod.EventBusSubscriber} like the rest of this mod's events.
 * <p>
 * Only runs via the {@code data} Gradle run (`./gradlew runData`), writing into
 * {@code src/generated/resources} (see {@code build.gradle}'s `--output`/`--existing` args) —
 * never at normal game runtime.
 */
public final class ArcaneDataGenerators {
    private ArcaneDataGenerators() {}

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.getVanillaPack(event.includeClient())
                .addProvider(ArcaneLanguageProvider::new);

        generator.getVanillaPack(event.includeClient())
                .addProvider(ArcaneModelProvider::new);

        generator.getVanillaPack(event.includeServer())
                .addProvider(output -> new ArcaneRecipeProvider.Runner(output, event.getLookupProvider()));

        generator.getVanillaPack(event.includeServer())
                .addProvider(output -> new ArcaneBlockTagsProvider(output, event.getLookupProvider(), existingFileHelper));

        generator.getVanillaPack(event.includeServer())
                .addProvider(output -> new ArcaneItemTagsProvider(output, event.getLookupProvider(), existingFileHelper));

        generator.getVanillaPack(event.includeServer())
                .addProvider(output -> new LootTableProvider(
                        output,
                        Set.of(),
                        List.of(
                                new LootTableProvider.SubProviderEntry(ArcaneBlockLootSubProvider::new, LootContextParamSets.BLOCK),
                                new LootTableProvider.SubProviderEntry(ArcaneEntityLootSubProvider::new, LootContextParamSets.ENTITY)
                        ),
                        event.getLookupProvider()
                ));
    }
}
