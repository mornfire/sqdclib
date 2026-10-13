package com.mornfire.sqdclib.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record CutsceneDefinition(
        int duration,
        boolean lockMovement,
        boolean letterbox,
        List<CameraKeyframe> camera,
        List<CutsceneEvent> events,
        Optional<Credits> credits) {

    public static final Codec<CutsceneDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("duration").forGetter(CutsceneDefinition::duration),
            Codec.BOOL.optionalFieldOf("lock_movement", true).forGetter(CutsceneDefinition::lockMovement),
            Codec.BOOL.optionalFieldOf("letterbox", true).forGetter(CutsceneDefinition::letterbox),
            CameraKeyframe.CODEC.listOf().optionalFieldOf("camera", List.of())
                    .forGetter(CutsceneDefinition::camera),
            CutsceneEvent.CODEC.listOf().optionalFieldOf("events", List.of())
                    .forGetter(CutsceneDefinition::events),
            Credits.CODEC.optionalFieldOf("credits").forGetter(CutsceneDefinition::credits))
            .apply(instance, CutsceneDefinition::new));

    public record Credits(int start, int duration, boolean speedUp, List<Line> lines) {
        public static final Codec<Credits> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("start", 0).forGetter(Credits::start),
                Codec.INT.fieldOf("duration").forGetter(Credits::duration),
                Codec.BOOL.optionalFieldOf("speed_up", true).forGetter(Credits::speedUp),
                Line.CODEC.listOf().optionalFieldOf("lines", List.of()).forGetter(Credits::lines))
                .apply(instance, Credits::new));

        public record Line(Component text, float scale, int gap, Optional<Integer> color) {
            public static final Codec<Line> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ComponentSerialization.CODEC.fieldOf("text").forGetter(Line::text),
                    Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(Line::scale),
                    Codec.INT.optionalFieldOf("gap", 8).forGetter(Line::gap),
                    Codec.INT.optionalFieldOf("color").forGetter(Line::color))
                    .apply(instance, Line::new));
        }
    }
}
