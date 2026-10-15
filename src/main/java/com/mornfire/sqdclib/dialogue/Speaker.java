package com.mornfire.sqdclib.dialogue;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

public record Speaker(Component name, Optional<ResourceLocation> portrait) {
    private static final Codec<Speaker> FULL = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("name").forGetter(Speaker::name),
            ResourceLocation.CODEC.optionalFieldOf("portrait").forGetter(Speaker::portrait))
            .apply(instance, Speaker::new));

    public static final Codec<Speaker> CODEC = Codec.either(FULL, ComponentSerialization.CODEC)
            .xmap(either -> either.map(speaker -> speaker, Speaker::of),
                    speaker -> speaker.portrait().isPresent()
                            ? Either.left(speaker)
                            : Either.right(speaker.name()));

    public static Speaker of(Component name) {
        return new Speaker(name, Optional.empty());
    }

    public static Speaker of(String name) {
        return new Speaker(Component.literal(name), Optional.empty());
    }

    public static Speaker of(Component name, ResourceLocation portrait) {
        return new Speaker(name, Optional.of(portrait));
    }
}
