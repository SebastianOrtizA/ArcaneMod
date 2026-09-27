package com.sebas.arcanemod.core.wand;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record WandAssembly(
        Identifier core,
        Identifier cap,
        Identifier binding,
        Optional<Identifier> inlay
) {
    public static final Codec<WandAssembly> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("core").forGetter(WandAssembly::core),
            Identifier.CODEC.fieldOf("cap").forGetter(WandAssembly::cap),
            Identifier.CODEC.fieldOf("binding").forGetter(WandAssembly::binding),
            Identifier.CODEC.optionalFieldOf("inlay").forGetter(WandAssembly::inlay)
    ).apply(instance, WandAssembly::new));

    public static final StreamCodec<ByteBuf, WandAssembly> STREAM_CODEC =
            StreamCodec.composite(
                    Identifier.STREAM_CODEC, WandAssembly::core,
                    Identifier.STREAM_CODEC, WandAssembly::cap,
                    Identifier.STREAM_CODEC, WandAssembly::binding,
                    ByteBufCodecs.optional(Identifier.STREAM_CODEC), WandAssembly::inlay,
                    WandAssembly::new
            );
}
