package com.sebas.arcanemod.data;

import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.VanillaItemTagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ArcaneItemTagsProvider extends VanillaItemTagsProvider {
    public static final TagKey<Item> WAND_CORES = ItemTags.create(Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "wand_cores"));
    public static final TagKey<Item> WAND_CAPS = ItemTags.create(Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "wand_caps"));
    public static final TagKey<Item> WAND_BINDINGS = ItemTags.create(Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "wand_bindings"));
    public static final TagKey<Item> WAND_INLAYS = ItemTags.create(Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "wand_inlays"));

    public ArcaneItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                  ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ArcaneMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(WAND_CORES)
                .add(ModItems.WOOD_CORE.getKey(), ModItems.WYRDSTONE_CORE.getKey(),
                        ModItems.QUICKSILVER_CORE.getKey(), ModItems.VOIDGLASS_CORE.getKey());

        tag(WAND_CAPS)
                .add(ModItems.COPPER_CAP.getKey(), ModItems.IRON_CAP.getKey(),
                        ModItems.PRISM_CAP.getKey(), ModItems.QUARTZ_CAP.getKey());

        tag(WAND_BINDINGS)
                .add(ModItems.LEATHER_BINDING.getKey(), ModItems.SILK_BINDING.getKey(),
                        ModItems.WYRDTHREAD_BINDING.getKey(), ModItems.VOIDWEAVE_BINDING.getKey());

        tag(WAND_INLAYS)
                .add(ModItems.RUBY_INLAY.getKey(), ModItems.SAPPHIRE_INLAY.getKey(),
                        ModItems.EMERALD_INLAY.getKey(), ModItems.DIAMOND_INLAY.getKey());
    }

    @Override
    public String getName() {
        return "Arcane Mod Item Tags";
    }
}
