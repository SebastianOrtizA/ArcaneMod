package com.sebas.arcanemod.item;

import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.block.ModBlocks;
import com.sebas.arcanemod.core.ModDataComponents;
import com.sebas.arcanemod.core.wand.FocusData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ArcaneMod.MODID);

    // --- Stage 0 items ---

    public static final RegistryObject<Item> WAND = ITEMS.register("wand",
            () -> new WandItem(new Item.Properties()
                    .setId(ITEMS.key("wand"))
                    .stacksTo(1)
                    .durability(64)));

    public static final RegistryObject<Item> CODEX_ARCANUM = ITEMS.register("codex_arcanum",
            () -> new CodexArcanumItem(new Item.Properties()
                    .setId(ITEMS.key("codex_arcanum"))
                    .stacksTo(1)));

    public static final RegistryObject<Item> CURSED_LECTERN = ITEMS.register("cursed_lectern",
            () -> new CursedLecternItem(new Item.Properties().setId(ITEMS.key("cursed_lectern"))));

    public static final RegistryObject<Item> WYRDSTONE_ORE = ITEMS.register("wyrdstone_ore",
            () -> new BlockItem(ModBlocks.WYRDSTONE_ORE.get(),
                    new Item.Properties().setId(ITEMS.key("wyrdstone_ore")).useBlockDescriptionPrefix()));

    public static final RegistryObject<Item> DEEPSLATE_WYRDSTONE_ORE = ITEMS.register("deepslate_wyrdstone_ore",
            () -> new BlockItem(ModBlocks.DEEPSLATE_WYRDSTONE_ORE.get(),
                    new Item.Properties().setId(ITEMS.key("deepslate_wyrdstone_ore")).useBlockDescriptionPrefix()));

    public static final RegistryObject<Item> WYRDSTONE = ITEMS.register("wyrdstone",
            () -> new Item(new Item.Properties().setId(ITEMS.key("wyrdstone"))));

    public static final RegistryObject<Item> WYRD_DUST = ITEMS.register("wyrd_dust",
            () -> new Item(new Item.Properties().setId(ITEMS.key("wyrd_dust"))));

    public static final RegistryObject<Item> WEAVE_WELLSPRING = ITEMS.register("weave_wellspring",
            () -> new BlockItem(ModBlocks.WEAVE_WELLSPRING.get(),
                    new Item.Properties().setId(ITEMS.key("weave_wellspring")).useBlockDescriptionPrefix()));

    public static final RegistryObject<Item> RESONOMETER = ITEMS.register("resonometer",
            () -> new ResonometerItem(new Item.Properties()
                    .setId(ITEMS.key("resonometer"))
                    .stacksTo(1)));

    public static final RegistryObject<Item> LOOM_OF_UNDERSTANDING = ITEMS.register("loom_of_understanding",
            () -> new BlockItem(ModBlocks.LOOM_OF_UNDERSTANDING.get(),
                    new Item.Properties().setId(ITEMS.key("loom_of_understanding")).useBlockDescriptionPrefix()));

    // --- Stage 1: Wand Cores ---

    public static final RegistryObject<Item> WOOD_CORE = ITEMS.register("wood_core",
            () -> new WandCoreItem(new Item.Properties().setId(ITEMS.key("wood_core")), 100, 1.0));

    public static final RegistryObject<Item> WYRDSTONE_CORE = ITEMS.register("wyrdstone_core",
            () -> new WandCoreItem(new Item.Properties().setId(ITEMS.key("wyrdstone_core")), 200, 0.85));

    public static final RegistryObject<Item> QUICKSILVER_CORE = ITEMS.register("quicksilver_core",
            () -> new WandCoreItem(new Item.Properties().setId(ITEMS.key("quicksilver_core")), 80, 0.6));

    public static final RegistryObject<Item> VOIDGLASS_CORE = ITEMS.register("voidglass_core",
            () -> new WandCoreItem(new Item.Properties().setId(ITEMS.key("voidglass_core")), 400, 1.3));

    // --- Stage 1: Wand Caps ---

    public static final RegistryObject<Item> COPPER_CAP = ITEMS.register("copper_cap",
            () -> new WandCapItem(new Item.Properties().setId(ITEMS.key("copper_cap")),
                    0.10, 0.0, 0.0, 0, 0.0, "copper"));

    public static final RegistryObject<Item> IRON_CAP = ITEMS.register("iron_cap",
            () -> new WandCapItem(new Item.Properties().setId(ITEMS.key("iron_cap")),
                    0.0, 0.50, 0.0, 0, 0.0, "iron"));

    public static final RegistryObject<Item> PRISM_CAP = ITEMS.register("prism_cap",
            () -> new WandCapItem(new Item.Properties().setId(ITEMS.key("prism_cap")),
                    0.0, 0.0, 0.0, 3, 0.50, "prism"));

    public static final RegistryObject<Item> QUARTZ_CAP = ITEMS.register("quartz_cap",
            () -> new WandCapItem(new Item.Properties().setId(ITEMS.key("quartz_cap")),
                    0.0, 0.0, 0.50, 0, 0.0, "quartz"));

    // --- Stage 1: Wand Bindings ---

    public static final RegistryObject<Item> LEATHER_BINDING = ITEMS.register("leather_binding",
            () -> new WandBindingItem(new Item.Properties().setId(ITEMS.key("leather_binding")),
                    1.0, 0.0, 0.0, "leather"));

    public static final RegistryObject<Item> SILK_BINDING = ITEMS.register("silk_binding",
            () -> new WandBindingItem(new Item.Properties().setId(ITEMS.key("silk_binding")),
                    1.0, 0.15, 0.0, "silk"));

    public static final RegistryObject<Item> WYRDTHREAD_BINDING = ITEMS.register("wyrdthread_binding",
            () -> new WandBindingItem(new Item.Properties().setId(ITEMS.key("wyrdthread_binding")),
                    1.20, 0.0, 0.0, "wyrdthread"));

    public static final RegistryObject<Item> VOIDWEAVE_BINDING = ITEMS.register("voidweave_binding",
            () -> new WandBindingItem(new Item.Properties().setId(ITEMS.key("voidweave_binding")),
                    1.0, 0.0, 0.15, "voidweave"));

    // --- Stage 1: Wand Inlays ---

    public static final RegistryObject<Item> RUBY_INLAY = ITEMS.register("ruby_inlay",
            () -> new WandInlayItem(new Item.Properties().setId(ITEMS.key("ruby_inlay")), "ruby"));

    public static final RegistryObject<Item> SAPPHIRE_INLAY = ITEMS.register("sapphire_inlay",
            () -> new WandInlayItem(new Item.Properties().setId(ITEMS.key("sapphire_inlay")), "sapphire"));

    public static final RegistryObject<Item> EMERALD_INLAY = ITEMS.register("emerald_inlay",
            () -> new WandInlayItem(new Item.Properties().setId(ITEMS.key("emerald_inlay")), "emerald"));

    public static final RegistryObject<Item> DIAMOND_INLAY = ITEMS.register("diamond_inlay",
            () -> new WandInlayItem(new Item.Properties().setId(ITEMS.key("diamond_inlay")), "diamond"));

    // --- Stage 1: Modular Wand & Staff ---

    public static final RegistryObject<Item> MODULAR_WAND = ITEMS.register("modular_wand",
            () -> new ModularWandItem(new Item.Properties()
                    .setId(ITEMS.key("modular_wand"))
                    .stacksTo(1)
                    .durability(256), false));

    public static final RegistryObject<Item> MODULAR_STAFF = ITEMS.register("modular_staff",
            () -> new ModularWandItem(new Item.Properties()
                    .setId(ITEMS.key("modular_staff"))
                    .stacksTo(1)
                    .durability(384), true));

    // --- Stage 1: Spell Focus ---

    public static final RegistryObject<Item> SPELL_FOCUS = ITEMS.register("spell_focus",
            () -> new SpellFocusItem(new Item.Properties()
                    .setId(ITEMS.key("spell_focus"))
                    .stacksTo(1)
                    .component(ModDataComponents.FOCUS_DATA.get(),
                            new FocusData(Identifier.fromNamespaceAndPath("arcanemod", "spark"), 32, 32))));

    // --- Stage 1: Block Items ---

    public static final RegistryObject<Item> WANDWRIGHTS_BENCH = ITEMS.register("wandwrights_bench",
            () -> new BlockItem(ModBlocks.WANDWRIGHTS_BENCH.get(),
                    new Item.Properties().setId(ITEMS.key("wandwrights_bench")).useBlockDescriptionPrefix()));

    public static final RegistryObject<Item> ADVANCED_WANDWRIGHTS_BENCH = ITEMS.register("advanced_wandwrights_bench",
            () -> new BlockItem(ModBlocks.ADVANCED_WANDWRIGHTS_BENCH.get(),
                    new Item.Properties().setId(ITEMS.key("advanced_wandwrights_bench")).useBlockDescriptionPrefix()));
}