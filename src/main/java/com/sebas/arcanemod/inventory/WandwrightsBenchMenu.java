package com.sebas.arcanemod.inventory;

import com.sebas.arcanemod.block.ModBlocks;
import com.sebas.arcanemod.core.ModDataComponents;
import com.sebas.arcanemod.core.wand.WandAssembly;
import com.sebas.arcanemod.item.ModItems;
import com.sebas.arcanemod.item.WandBindingItem;
import com.sebas.arcanemod.item.WandCapItem;
import com.sebas.arcanemod.item.WandCoreItem;
import com.sebas.arcanemod.item.WandInlayItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class WandwrightsBenchMenu extends AbstractContainerMenu {
    public static final int CORE_SLOT = 0;
    public static final int CAP_SLOT = 1;
    public static final int BINDING_SLOT = 2;
    public static final int INLAY_SLOT = 3;
    public static final int RESULT_SLOT = 4;

    private final ContainerLevelAccess access;
    private final boolean advanced;
    private final Container inputSlots;
    private final ResultContainer resultSlots = new ResultContainer() {
        @Override
        public void setChanged() {
            WandwrightsBenchMenu.this.slotsChanged(this);
        }
    };

    public static WandwrightsBenchMenu clientFactory(int containerId, Inventory inventory) {
        return new WandwrightsBenchMenu(containerId, inventory, ContainerLevelAccess.NULL, false);
    }

    public WandwrightsBenchMenu(int containerId, Inventory inventory,
                                ContainerLevelAccess access, boolean advanced) {
        super(ModMenuTypes.WANDWRIGHTS_BENCH.get(), containerId);
        this.access = access;
        this.advanced = advanced;
        this.inputSlots = new SimpleContainer(4) {
            @Override
            public void setChanged() {
                super.setChanged();
                WandwrightsBenchMenu.this.slotsChanged(this);
            }
        };

        this.addSlot(new Slot(inputSlots, CORE_SLOT, 27, 25) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof WandCoreItem;
            }
        });
        this.addSlot(new Slot(inputSlots, CAP_SLOT, 76, 25) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof WandCapItem;
            }
        });
        this.addSlot(new Slot(inputSlots, BINDING_SLOT, 27, 50) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof WandBindingItem;
            }
        });
        this.addSlot(new Slot(inputSlots, INLAY_SLOT, 76, 50) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof WandInlayItem;
            }
        });

        this.addSlot(new Slot(resultSlots, 0, 134, 38) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                inputSlots.getItem(CORE_SLOT).shrink(1);
                inputSlots.getItem(CAP_SLOT).shrink(1);
                inputSlots.getItem(BINDING_SLOT).shrink(1);
                inputSlots.getItem(INLAY_SLOT).shrink(1);
                WandwrightsBenchMenu.this.createResult();
            }
        });

        addStandardInventorySlots(inventory, 8, 84);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == inputSlots) {
            createResult();
        }
    }

    private void createResult() {
        ItemStack core = inputSlots.getItem(CORE_SLOT);
        ItemStack cap = inputSlots.getItem(CAP_SLOT);
        ItemStack binding = inputSlots.getItem(BINDING_SLOT);
        ItemStack inlay = inputSlots.getItem(INLAY_SLOT);

        if (core.isEmpty() || cap.isEmpty() || binding.isEmpty()) {
            resultSlots.setItem(0, ItemStack.EMPTY);
            return;
        }

        Identifier coreId = ForgeRegistries.ITEMS.getKey(core.getItem());
        Identifier capId = ForgeRegistries.ITEMS.getKey(cap.getItem());
        Identifier bindingId = ForgeRegistries.ITEMS.getKey(binding.getItem());
        Optional<Identifier> inlayId = inlay.isEmpty()
                ? Optional.empty()
                : Optional.ofNullable(ForgeRegistries.ITEMS.getKey(inlay.getItem()));

        ItemStack result = new ItemStack(advanced ? ModItems.MODULAR_STAFF.get() : ModItems.MODULAR_WAND.get());
        result.set(ModDataComponents.WAND_ASSEMBLY.get(),
                new WandAssembly(coreId, capId, bindingId, inlayId));
        result.set(ModDataComponents.STORED_WYRD.get(), 0);

        resultSlots.setItem(0, result);
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate(
                (level, pos) -> {
                    var state = level.getBlockState(pos);
                    return (state.is(ModBlocks.WANDWRIGHTS_BENCH.get()) ||
                            state.is(ModBlocks.ADVANCED_WANDWRIGHTS_BENCH.get())) &&
                            player.isWithinBlockInteractionRange(pos, 4.0);
                }, true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();

            if (slotIndex == RESULT_SLOT) {
                if (!this.moveItemStackTo(stack, 5, 41, true)) return ItemStack.EMPTY;
                slot.onQuickCraft(stack, copy);
            } else if (slotIndex >= 5) {
                if (stack.getItem() instanceof WandCoreItem) {
                    if (!this.moveItemStackTo(stack, CORE_SLOT, CORE_SLOT + 1, false)) return ItemStack.EMPTY;
                } else if (stack.getItem() instanceof WandCapItem) {
                    if (!this.moveItemStackTo(stack, CAP_SLOT, CAP_SLOT + 1, false)) return ItemStack.EMPTY;
                } else if (stack.getItem() instanceof WandBindingItem) {
                    if (!this.moveItemStackTo(stack, BINDING_SLOT, BINDING_SLOT + 1, false)) return ItemStack.EMPTY;
                } else if (stack.getItem() instanceof WandInlayItem) {
                    if (!this.moveItemStackTo(stack, INLAY_SLOT, INLAY_SLOT + 1, false)) return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stack, 5, 41, false)) return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();

            if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.inputSlots));
    }
}
