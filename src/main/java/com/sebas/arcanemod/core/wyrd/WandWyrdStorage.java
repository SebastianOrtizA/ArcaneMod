package com.sebas.arcanemod.core.wyrd;

import com.sebas.arcanemod.core.ModDataComponents;
import net.minecraft.world.item.ItemStack;

/**
 * Adapts an ItemStack's {@code STORED_WYRD} data component to the {@link IWyrdStorage}
 * interface. A thin, stateless wrapper — all state lives in the stack's component, so this
 * can be constructed fresh wherever it's needed rather than cached.
 */
public class WandWyrdStorage implements IWyrdStorage {
    /** The cobblestone wand's capacity — "crude, low capacity" per the design doc. */
    public static final int COBBLESTONE_WAND_MAX_WYRD = 100;

    private final ItemStack stack;
    private final int maxWyrd;

    public WandWyrdStorage(ItemStack stack, int maxWyrd) {
        this.stack = stack;
        this.maxWyrd = maxWyrd;
    }

    @Override
    public int getWyrd() {
        return stack.getOrDefault(ModDataComponents.STORED_WYRD.get(), 0);
    }

    @Override
    public int getMaxWyrd() {
        return maxWyrd;
    }

    @Override
    public int receiveWyrd(int amount, boolean simulate) {
        if (amount <= 0) return 0;
        int current = getWyrd();
        int accepted = Math.min(amount, maxWyrd - current);
        if (accepted <= 0) return 0;
        if (!simulate) {
            stack.set(ModDataComponents.STORED_WYRD.get(), current + accepted);
        }
        return accepted;
    }

    @Override
    public int extractWyrd(int amount, boolean simulate) {
        if (amount <= 0) return 0;
        int current = getWyrd();
        int extracted = Math.min(amount, current);
        if (extracted <= 0) return 0;
        if (!simulate) {
            stack.set(ModDataComponents.STORED_WYRD.get(), current - extracted);
        }
        return extracted;
    }
}
