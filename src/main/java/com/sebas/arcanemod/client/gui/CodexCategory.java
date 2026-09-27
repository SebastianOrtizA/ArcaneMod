package com.sebas.arcanemod.client.gui;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * One Codex tab. {@code section} is the design doc's Section number (1-7) — its pages are no
 * longer baked in here; {@link CodexResearchPages#forSection} computes them on demand from
 * whatever research nodes exist for that section, so a category with no content yet (Sections
 * 2-7, until later stages add their nodes) still shows a tab, just with a placeholder page.
 */
public record CodexCategory(String name, String tabLabel, Supplier<ItemStack> icon,
                             @Nullable Identifier customIconTexture, int section) {

    public CodexCategory(String name, String tabLabel, Supplier<ItemStack> icon, int section) {
        this(name, tabLabel, icon, null, section);
    }
}
