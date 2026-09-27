package com.sebas.arcanemod.event;

import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.data.CursedLecternData;
import com.sebas.arcanemod.item.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.Result;

@Mod.EventBusSubscriber(modid = ArcaneMod.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onLecternRightClick(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        var pos = event.getPos();
        var state = level.getBlockState(pos);
        var player = event.getEntity();
        var heldItem = event.getItemStack();

        if (!(state.getBlock() instanceof LecternBlock)) return;
        if (!heldItem.is(ModItems.WAND.get())) return;
        if (level.isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) level;
        CursedLecternData data = CursedLecternData.get(serverLevel);

        if (data.isCursed(pos)) {
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }

        if (!state.getValue(LecternBlock.HAS_BOOK)) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof LecternBlockEntity lectern) {
            lectern.setBook(ItemStack.EMPTY);
        }
        level.setBlock(pos, state.setValue(LecternBlock.HAS_BOOK, false), 3);

        player.getInventory().add(ModItems.CODEX_ARCANUM.get().getDefaultInstance());
        data.markCursed(pos);

        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onLecternBreak(BlockEvent.BreakEvent event) {
        var levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel serverLevel)) return;

        var pos = event.getPos();
        if (!(event.getState().getBlock() instanceof LecternBlock)) return;

        CursedLecternData data = CursedLecternData.get(serverLevel);
        if (!data.isCursed(pos)) return; // not cursed, let vanilla handle it normally

        // Take full manual control: stop vanilla's break process...
        event.setResult(Result.DENY);

        // ...then do the removal and drop ourselves
        serverLevel.removeBlock(pos, false);

        if (!event.getPlayer().isCreative()) {
            ItemStack cursedDrop = ModItems.CURSED_LECTERN.get().getDefaultInstance();
            Block.popResource(serverLevel, pos, cursedDrop);
        }

        data.clearCursed(pos);
    }
}