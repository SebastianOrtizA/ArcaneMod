package com.sebas.arcanemod.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.resources.Identifier;
import com.sebas.arcanemod.ArcaneMod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CursedLecternData extends SavedData {
    private final Set<BlockPos> cursedPositions;

    private static final Codec<CursedLecternData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().fieldOf("positions").forGetter(
                    (CursedLecternData data) -> List.copyOf(data.cursedPositions)
            )
    ).apply(instance, (List<BlockPos> list) -> new CursedLecternData(new HashSet<>(list))));

    public static final SavedDataType<CursedLecternData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(ArcaneMod.MODID, "cursed_lecterns"),
            () -> new CursedLecternData(new HashSet<>()),
            CODEC,
            null
    );

    private CursedLecternData(Set<BlockPos> initial) {
        this.cursedPositions = initial;
    }

    public static CursedLecternData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isCursed(BlockPos pos) {
        return cursedPositions.contains(pos.immutable());
    }

    public void markCursed(BlockPos pos) {
        cursedPositions.add(pos.immutable());
        setDirty();
    }

    public void clearCursed(BlockPos pos) {
        cursedPositions.remove(pos.immutable());
        setDirty();
    }
}