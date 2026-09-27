package com.sebas.arcanemod.blockentity;

import com.sebas.arcanemod.ArcaneMod;
import com.sebas.arcanemod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

/**
 * Registry holder for all block entity types this mod adds.
 * <p>
 * Note: in this MC/Forge version {@link BlockEntityType} has no builder helper —
 * it's constructed directly as {@code new BlockEntityType<>(factory, Set.of(validBlocks...))}.
 * <p>
 */
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ArcaneMod.MODID);

    public static final RegistryObject<BlockEntityType<WeaveWellspringBlockEntity>> WEAVE_WELLSPRING =
            BLOCK_ENTITIES.register("weave_wellspring",
                    () -> new BlockEntityType<>(WeaveWellspringBlockEntity::new, Set.of(ModBlocks.WEAVE_WELLSPRING.get())));
}
