package com.mornfire.sqdclib.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mornfire.sqdclib.storystate.action.StoryAction;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.sounds.SoundEvent;

public record CutsceneEvent(
        int time,
        Optional<Component> text,
        int duration,
        Optional<Fade> fade,
        Optional<SoundCue> sound,
        Optional<AnimationCue> animation,
        Optional<StandInCue> standIn,
        Optional<QteCue> qte,
        List<StoryAction> actions) {

    public static final Codec<CutsceneEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("time").forGetter(CutsceneEvent::time),
            ComponentSerialization.CODEC.optionalFieldOf("text").forGetter(CutsceneEvent::text),
            Codec.INT.optionalFieldOf("duration", 40).forGetter(CutsceneEvent::duration),
            Fade.CODEC.optionalFieldOf("fade").forGetter(CutsceneEvent::fade),
            SoundCue.CODEC.optionalFieldOf("sound").forGetter(CutsceneEvent::sound),
            AnimationCue.CODEC.optionalFieldOf("animation").forGetter(CutsceneEvent::animation),
            StandInCue.CODEC.optionalFieldOf("player_stand_in").forGetter(CutsceneEvent::standIn),
            QteCue.CODEC.optionalFieldOf("qte").forGetter(CutsceneEvent::qte),
            StoryAction.CODEC.listOf().optionalFieldOf("actions", List.of()).forGetter(CutsceneEvent::actions))
            .apply(instance, CutsceneEvent::new));

    public record Fade(float to, int ticks) {
        public static final Codec<Fade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("to").forGetter(Fade::to),
                Codec.INT.optionalFieldOf("ticks", 20).forGetter(Fade::ticks))
                .apply(instance, Fade::new));
    }

    public record SoundCue(Holder<SoundEvent> sound, float volume, float pitch) {
        public static final Codec<SoundCue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                SoundEvent.CODEC.fieldOf("sound").forGetter(SoundCue::sound),
                Codec.FLOAT.optionalFieldOf("volume", 1.0F).forGetter(SoundCue::volume),
                Codec.FLOAT.optionalFieldOf("pitch", 1.0F).forGetter(SoundCue::pitch))
                .apply(instance, SoundCue::new));
    }
}
