package com.mornfire.sqdclib.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mornfire.sqdclib.storystate.action.StoryAction;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record CutsceneEvent(
        int time,
        Optional<Component> text,
        int duration,
        Optional<Fade> fade,
        List<StoryAction> actions) {

    public static final Codec<CutsceneEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("time").forGetter(CutsceneEvent::time),
            ComponentSerialization.CODEC.optionalFieldOf("text").forGetter(CutsceneEvent::text),
            Codec.INT.optionalFieldOf("duration", 40).forGetter(CutsceneEvent::duration),
            Fade.CODEC.optionalFieldOf("fade").forGetter(CutsceneEvent::fade),
            StoryAction.CODEC.listOf().optionalFieldOf("actions", List.of()).forGetter(CutsceneEvent::actions))
            .apply(instance, CutsceneEvent::new));

    public record Fade(float to, int ticks) {
        public static final Codec<Fade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("to").forGetter(Fade::to),
                Codec.INT.optionalFieldOf("ticks", 20).forGetter(Fade::ticks))
                .apply(instance, Fade::new));
    }
}
