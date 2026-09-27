package com.sebas.arcanemod.worldgen;

import com.sebas.arcanemod.ArcaneMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, ArcaneMod.MODID);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, ArcaneMod.MODID);

    public static final RegistryObject<StructureType<LoomkeepersSatchelStructure>> LOOMKEEPERS_SATCHEL_TYPE =
            STRUCTURE_TYPES.register("loomkeepers_satchel",
                    () -> () -> LoomkeepersSatchelStructure.CODEC);

    public static final RegistryObject<StructurePieceType> LOOMKEEPERS_SATCHEL_PIECE =
            STRUCTURE_PIECES.register("loomkeepers_satchel",
                    () -> (StructurePieceType.ContextlessType) LoomkeepersSatchelPiece::new);
}
