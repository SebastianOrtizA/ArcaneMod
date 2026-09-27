package com.sebas.arcanemod.core.wand;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record FocusData(
        Identifier spell,
        int charges,
        int maxCharges
) {
    public static final Codec<FocusData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("spell").forGetter(FocusData::spell),
            Codec.INT.fieldOf("charges").forGetter(FocusData::charges),
            Codec.INT.fieldOf("max_charges").forGetter(FocusData::maxCharges)
    ).apply(instance, FocusData::new));

    public static final StreamCodec<ByteBuf, FocusData> STREAM_CODEC =
            StreamCodec.composite(
                    Identifier.STREAM_CODEC, FocusData::spell,
                    ByteBufCodecs.VAR_INT, FocusData::charges,
                    ByteBufCodecs.VAR_INT, FocusData::maxCharges,
                    FocusData::new
            );

    public boolean isInert() {
        return charges <= 0;
    }

    public FocusData withCharges(int newCharges) {
        return new FocusData(spell, Math.max(0, Math.min(newCharges, maxCharges)), maxCharges);
    }
}
