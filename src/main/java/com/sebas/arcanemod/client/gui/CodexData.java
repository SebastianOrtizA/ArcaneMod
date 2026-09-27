package com.sebas.arcanemod.client.gui;

import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.item.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * The Codex's 7 tabs (design doc §12: "7 sections, matching the design document"), one per
 * Section number. Only the chrome — name/icon — lives here now; each tab's actual page content is
 * computed from the research tree at render time by {@link CodexResearchPages}, so a category
 * shows real content the moment matching research nodes exist, with no code change needed here.
 */
public class CodexData {

    private static Identifier tabIcon(String fileName) {
        return Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "textures/gui/" + fileName);
    }

    public static final List<CodexCategory> CATEGORIES = List.of(
            new CodexCategory("Introduction", "Introduction", () -> new ItemStack(Items.BOOK),
                    tabIcon("tab_introduction.png"), 1),
            new CodexCategory("Wands", "Wands", () -> new ItemStack(ModItems.WAND.get()),
                    tabIcon("tab_wands.png"), 2),
            new CodexCategory("Sorcery", "Sorcery", () -> new ItemStack(Items.AMETHYST_SHARD),
                    tabIcon("tab_sorcery.png"), 3),
            new CodexCategory("Rituals", "Rituals", () -> new ItemStack(Items.LECTERN),
                    tabIcon("tab_rituals.png"), 4),
            new CodexCategory("Alchemy", "Alchemy", () -> new ItemStack(Items.BREWING_STAND),
                    tabIcon("tab_alchemy.png"), 5),
            new CodexCategory("Magical Equipment", "Equipment", () -> new ItemStack(Items.SHIELD),
                    tabIcon("tab_magical_equipment.png"), 6),
            new CodexCategory("Forbidden Knowledge", "Forbidden", () -> new ItemStack(Items.NETHER_STAR),
                    tabIcon("tab_forbidden_knowledge.png"), 7)
    );
}
