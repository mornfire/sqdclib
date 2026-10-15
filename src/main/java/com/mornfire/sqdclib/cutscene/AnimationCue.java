package com.mornfire.sqdclib.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AnimationCue(String entity, String controller, String name, boolean loop, float speed) {
    public static final String SELF = "@self";
    public static final String PLAYER = "@player";
    public static final String NEAREST = "@nearest";

    public static final Codec<AnimationCue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("entity", PLAYER).forGetter(AnimationCue::entity),
            Codec.STRING.optionalFieldOf("controller", "main").forGetter(AnimationCue::controller),
            Codec.STRING.fieldOf("name").forGetter(AnimationCue::name),
            Codec.BOOL.optionalFieldOf("loop", false).forGetter(AnimationCue::loop),
            Codec.FLOAT.optionalFieldOf("speed", 1.0F).forGetter(AnimationCue::speed))
            .apply(instance, AnimationCue::new));
}
