package com.sebas.arcanemod.data;

import com.sebas.arcanemod.entity.ModEntityTypes;
import com.sebas.arcanemod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.stream.Stream;

/**
 * Generates {@code data/arcanemod/loot_table/entities/*.json} — replaces the three hand-written
 * files of the same names (deleted once confirmed equivalent). Drop shapes deliberately mirror
 * vanilla's own zombie/skeleton/spider tables (same item, same 0-2 uniform roll, same 33% spider
 * eye chance) with one extra pool appended: 1-2 Wyrd Dust, standing in for "Facet-rich item" until
 * real alchemy content exists.
 */
public class ArcaneEntityLootSubProvider extends EntityLootSubProvider {
    protected ArcaneEntityLootSubProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.VANILLA_SET, registries);
    }

    /** See {@code ArcaneBlockLootSubProvider#getKnownBlocks} for why this scoping override is needed. */
    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return Stream.of(
                ModEntityTypes.FRAYED_ZOMBIE.get(),
                ModEntityTypes.FRAYED_SKELETON.get(),
                ModEntityTypes.FRAYED_SPIDER.get()
        );
    }

    @Override
    public void generate() {
        add(ModEntityTypes.FRAYED_ZOMBIE.get(), LootTable.lootTable()
                .withPool(uniformDropPool(Items.ROTTEN_FLESH, 0, 2))
                .withPool(wyrdDustPool()));

        add(ModEntityTypes.FRAYED_SKELETON.get(), LootTable.lootTable()
                .withPool(uniformDropPool(Items.BONE, 0, 2))
                .withPool(uniformDropPool(Items.ARROW, 0, 2))
                .withPool(wyrdDustPool()));

        add(ModEntityTypes.FRAYED_SPIDER.get(), LootTable.lootTable()
                .withPool(uniformDropPool(Items.STRING, 0, 2))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.SPIDER_EYE))
                        .when(LootItemRandomChanceCondition.randomChance(0.33F)))
                .withPool(wyrdDustPool()));
    }

    private static LootPool.Builder uniformDropPool(Item item, float min, float max) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))));
    }

    private static LootPool.Builder wyrdDustPool() {
        return uniformDropPool(ModItems.WYRD_DUST.get(), 1, 2);
    }
}
