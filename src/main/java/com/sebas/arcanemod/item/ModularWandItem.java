package com.sebas.arcanemod.item;

import com.sebas.arcanemod.block.WandwrightsBenchBlock;
import com.sebas.arcanemod.blockentity.WeaveWellspringBlockEntity;
import com.sebas.arcanemod.core.ModDataComponents;
import com.sebas.arcanemod.core.wand.FocusData;
import com.sebas.arcanemod.core.wand.WandAssembly;
import com.sebas.arcanemod.core.wyrd.WandWyrdStorage;
import com.sebas.arcanemod.entity.SparkBoltEntity;
import com.sebas.arcanemod.client.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ModularWandItem extends Item {
    private static final int CHANNEL_DURATION_TICKS = 72000;
    private static final int SPARK_WYRD_COST = 5;
    private static final Identifier SPARK_SPELL = Identifier.fromNamespaceAndPath("arcanemod", "spark");
    private final boolean isStaff;

    public ModularWandItem(Properties properties, boolean isStaff) {
        super(properties);
        this.isStaff = isStaff;
    }

    public int getMaxWyrd(ItemStack stack) {
        WandAssembly assembly = stack.get(ModDataComponents.WAND_ASSEMBLY.get());
        if (assembly == null) return 0;

        Item coreItem = ForgeRegistries.ITEMS.getValue(assembly.core());
        Item bindingItem = ForgeRegistries.ITEMS.getValue(assembly.binding());

        int baseCapacity = coreItem instanceof WandCoreItem core ? core.getBaseCapacity() : 100;
        double bindingMult = bindingItem instanceof WandBindingItem binding ? binding.getCapacityMultiplier() : 1.0;
        double staffMult = isStaff ? 1.5 : 1.0;

        int extraSlot = 0;
        if (assembly.inlay().isPresent()) {
            Item inlayItem = ForgeRegistries.ITEMS.getValue(assembly.inlay().get());
            if (inlayItem instanceof WandInlayItem inlay && "emerald".equals(inlay.getEffectKey())) {
                extraSlot = 1;
            }
        }

        return (int) (baseCapacity * bindingMult * staffMult);
    }

    public int getFociSlotCount(ItemStack stack) {
        int base = isStaff ? 4 : 2;
        WandAssembly assembly = stack.get(ModDataComponents.WAND_ASSEMBLY.get());
        if (assembly != null && assembly.inlay().isPresent()) {
            Item inlayItem = ForgeRegistries.ITEMS.getValue(assembly.inlay().get());
            if (inlayItem instanceof WandInlayItem inlay && "emerald".equals(inlay.getEffectKey())) {
                base += 1;
            }
        }
        return base;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            InteractionHand otherHand = (hand == InteractionHand.MAIN_HAND)
                    ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack otherStack = player.getItemInHand(otherHand);
            if (otherStack.getItem() instanceof SpellFocusItem) {
                if (!level.isClientSide()) {
                    if (trySlotFocus(stack, otherStack, player)) {
                        return InteractionResult.SUCCESS;
                    }
                }
                return InteractionResult.CONSUME;
            }
        }

        if (findDrainableWellspring(level, player, stack) != null) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            if (tryCastFromFocus(stack, serverLevel, player, hand)) {
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    private boolean trySlotFocus(ItemStack wandStack, ItemStack focusStack, Player player) {
        FocusData focusData = focusStack.get(ModDataComponents.FOCUS_DATA.get());
        if (focusData == null) return false;

        List<FocusData> foci = wandStack.getOrDefault(ModDataComponents.FOCUS_SLOTS.get(), List.of());
        int maxSlots = getFociSlotCount(wandStack);
        if (foci.size() >= maxSlots) {
            player.sendOverlayMessage(
                    Component.translatable("message.arcanemod.focus_slots_full"));
            return false;
        }

        java.util.ArrayList<FocusData> newFoci = new java.util.ArrayList<>(foci);
        newFoci.add(focusData);
        wandStack.set(ModDataComponents.FOCUS_SLOTS.get(), List.copyOf(newFoci));
        focusStack.shrink(1);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.6F, 1.2F);
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) return InteractionResult.PASS;

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();

        if (state.getBlock() instanceof WandwrightsBenchBlock) {
            WandAssembly assembly = stack.get(ModDataComponents.WAND_ASSEMBLY.get());
            if (assembly != null && !level.isClientSide()) {
                Item coreItem = ForgeRegistries.ITEMS.getValue(assembly.core());
                Item capItem = ForgeRegistries.ITEMS.getValue(assembly.cap());
                Item bindingItem = ForgeRegistries.ITEMS.getValue(assembly.binding());

                if (coreItem != null) player.getInventory().placeItemBackInInventory(new ItemStack(coreItem));
                if (capItem != null) player.getInventory().placeItemBackInInventory(new ItemStack(capItem));
                if (bindingItem != null) player.getInventory().placeItemBackInInventory(new ItemStack(bindingItem));
                assembly.inlay().ifPresent(inlayId -> {
                    Item inlayItem = ForgeRegistries.ITEMS.getValue(inlayId);
                    if (inlayItem != null) player.getInventory().placeItemBackInInventory(new ItemStack(inlayItem));
                });

                stack.shrink(1);
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundEvents.ITEM_BREAK.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.SUCCESS;
        }

        InteractionHand hand = context.getHand();
        InteractionHand otherHand = (hand == InteractionHand.MAIN_HAND)
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);
        if (otherStack.getItem() instanceof SpellFocusItem) {
            if (!level.isClientSide()) {
                trySlotFocus(stack, otherStack, player);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return CHANNEL_DURATION_TICKS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(level instanceof ServerLevel serverLevel) || !(user instanceof Player player)) return;

        WeaveWellspringBlockEntity wellspring = findDrainableWellspring(level, player, stack);
        if (wellspring == null) {
            player.stopUsingItem();
            return;
        }

        WandWyrdStorage wandStorage = new WandWyrdStorage(stack, getMaxWyrd(stack));
        int transferred = wellspring.channelTick(serverLevel, wandStorage);
        var pos = wellspring.getBlockPos();

        serverLevel.sendParticles(ModParticles.WYRD_SPARK.get(),
                pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                2, 0.2, 0.3, 0.2, 0.01);

        if (transferred > 0 && remainingUseTicks % 5 == 0) {
            serverLevel.playSound(null, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.4F, 1.4F);
        }
    }

    private WeaveWellspringBlockEntity findDrainableWellspring(Level level, Player player, ItemStack stack) {
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hit.getType() != HitResult.Type.BLOCK) return null;

        BlockEntity blockEntity = level.getBlockEntity(hit.getBlockPos());
        if (!(blockEntity instanceof WeaveWellspringBlockEntity wellspring)) return null;
        if (wellspring.getStoredWyrd() <= 0) return null;

        WandWyrdStorage wandStorage = new WandWyrdStorage(stack, getMaxWyrd(stack));
        if (wandStorage.getWyrd() >= wandStorage.getMaxWyrd()) return null;

        return wellspring;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        WandAssembly assembly = stack.get(ModDataComponents.WAND_ASSEMBLY.get());
        if (assembly == null) return;

        int maxWyrd = getMaxWyrd(stack);
        WandWyrdStorage storage = new WandWyrdStorage(stack, maxWyrd);
        tooltip.accept(Component.translatable("tooltip.arcanemod.wyrd_charge", storage.getWyrd(), maxWyrd));

        Item coreItem = ForgeRegistries.ITEMS.getValue(assembly.core());
        Item capItem = ForgeRegistries.ITEMS.getValue(assembly.cap());
        Item bindingItem = ForgeRegistries.ITEMS.getValue(assembly.binding());

        if (coreItem != null)
            tooltip.accept(Component.translatable("tooltip.arcanemod.wand_core",
                    Component.translatable(coreItem.getDescriptionId())));
        if (capItem != null)
            tooltip.accept(Component.translatable("tooltip.arcanemod.wand_cap",
                    Component.translatable(capItem.getDescriptionId())));
        if (bindingItem != null)
            tooltip.accept(Component.translatable("tooltip.arcanemod.wand_binding",
                    Component.translatable(bindingItem.getDescriptionId())));

        assembly.inlay().ifPresent(inlayId -> {
            Item inlayItem = ForgeRegistries.ITEMS.getValue(inlayId);
            if (inlayItem != null)
                tooltip.accept(Component.translatable("tooltip.arcanemod.wand_inlay",
                        Component.translatable(inlayItem.getDescriptionId())));
        });

        tooltip.accept(Component.translatable("tooltip.arcanemod.foci_slots", getFociSlotCount(stack)));

        List<FocusData> foci = stack.getOrDefault(ModDataComponents.FOCUS_SLOTS.get(), List.of());
        if (!foci.isEmpty()) {
            int activeIndex = stack.getOrDefault(ModDataComponents.ACTIVE_FOCUS.get(), 0);
            for (int i = 0; i < foci.size(); i++) {
                FocusData focus = foci.get(i);
                String marker = (i == activeIndex) ? "> " : "  ";
                String status = focus.isInert() ? " [Inert]" : "";
                tooltip.accept(Component.literal(marker)
                        .append(Component.translatable("spell.arcanemod." + focus.spell().getPath()))
                        .append(Component.literal(" " + focus.charges() + "/" + focus.maxCharges() + status)));
            }
        }
    }

    private boolean tryCastFromFocus(ItemStack stack, ServerLevel serverLevel, Player player, InteractionHand hand) {
        List<FocusData> foci = stack.getOrDefault(ModDataComponents.FOCUS_SLOTS.get(), List.of());
        int activeIndex = stack.getOrDefault(ModDataComponents.ACTIVE_FOCUS.get(), 0);

        FocusData active = null;
        if (!foci.isEmpty() && activeIndex >= 0 && activeIndex < foci.size()) {
            active = foci.get(activeIndex);
        }

        if (active != null && !active.isInert()) {
            return castSpell(active.spell(), stack, serverLevel, player, hand, foci, activeIndex);
        }

        WandWyrdStorage storage = new WandWyrdStorage(stack, getMaxWyrd(stack));
        if (storage.getWyrd() >= SPARK_WYRD_COST) {
            return castSpell(SPARK_SPELL, stack, serverLevel, player, hand, foci, -1);
        }

        return false;
    }

    private boolean castSpell(Identifier spell, ItemStack stack, ServerLevel serverLevel,
                              Player player, InteractionHand hand,
                              List<FocusData> foci, int focusIndex) {
        boolean fromFocus = focusIndex >= 0 && focusIndex < foci.size();

        if (!fromFocus) {
            WandWyrdStorage storage = new WandWyrdStorage(stack, getMaxWyrd(stack));
            if (storage.getWyrd() < SPARK_WYRD_COST) return false;
            storage.extractWyrd(SPARK_WYRD_COST, false);
        }

        if (SPARK_SPELL.equals(spell)) {
            Vec3 look = player.getLookAngle();
            SparkBoltEntity spark = new SparkBoltEntity(serverLevel, player, look);
            spark.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            serverLevel.addFreshEntity(spark);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.5F, 1.8F);
            stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND
                    ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                    : net.minecraft.world.entity.EquipmentSlot.OFFHAND);

            if (fromFocus) {
                FocusData updated = foci.get(focusIndex).withCharges(foci.get(focusIndex).charges() - 1);
                java.util.ArrayList<FocusData> newFoci = new java.util.ArrayList<>(foci);
                newFoci.set(focusIndex, updated);
                stack.set(ModDataComponents.FOCUS_SLOTS.get(), List.copyOf(newFoci));
            }
            return true;
        }
        return false;
    }

    public boolean isStaff() { return isStaff; }
}
