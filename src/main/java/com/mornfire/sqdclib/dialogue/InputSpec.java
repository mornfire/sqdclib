package com.mornfire.sqdclib.dialogue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record InputSpec(String key, int maxLength, Optional<Component> placeholder) {
    public static final Codec<InputSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("key").forGetter(InputSpec::key),
            Codec.INT.optionalFieldOf("max_length", 32).forGetter(InputSpec::maxLength),
            ComponentSerialization.CODEC.optionalFieldOf("placeholder").forGetter(InputSpec::placeholder))
            .apply(instance, InputSpec::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InputSpec> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, InputSpec::key,
            ByteBufCodecs.VAR_INT, InputSpec::maxLength,
            ComponentSerialization.OPTIONAL_STREAM_CODEC, InputSpec::placeholder,
            InputSpec::new);
}
