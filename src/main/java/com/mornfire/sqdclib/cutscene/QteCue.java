package com.mornfire.sqdclib.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mornfire.sqdclib.storystate.action.StoryAction;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.StringRepresentable;

public record QteCue(
        Kind kind,
        String key,
        int duration,
        boolean optional,
        Optional<Component> prompt,
        List<StoryAction> onSuccess,
        List<StoryAction> onFail) {

    public static final Codec<QteCue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Kind.CODEC.optionalFieldOf("kind", Kind.KEY).forGetter(QteCue::kind),
            Codec.STRING.optionalFieldOf("key", "key.jump").forGetter(QteCue::key),
            Codec.INT.optionalFieldOf("duration", 40).forGetter(QteCue::duration),
            Codec.BOOL.optionalFieldOf("optional", true).forGetter(QteCue::optional),
            ComponentSerialization.CODEC.optionalFieldOf("prompt").forGetter(QteCue::prompt),
            StoryAction.CODEC.listOf().optionalFieldOf("on_success", List.of()).forGetter(QteCue::onSuccess),
            StoryAction.CODEC.listOf().optionalFieldOf("on_fail", List.of()).forGetter(QteCue::onFail))
            .apply(instance, QteCue::new));

    public enum Kind implements StringRepresentable {
        KEY,
        MASH;

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
