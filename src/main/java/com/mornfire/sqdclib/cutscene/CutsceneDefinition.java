package com.mornfire.sqdclib.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

public record CutsceneDefinition(
        int schema,
        int duration,
        boolean lockMovement,
        boolean letterbox,
        boolean skippable,
        Optional<ResourceLocation> music,
        Optional<ResourceLocation> videoId,
        Optional<ResourceLocation> audio,
        List<CameraKeyframe> camera,
        List<CutsceneEvent> events,
        Optional<Credits> credits) {

    public static final int CURRENT_SCHEMA = 3;

    public static final Codec<CutsceneDefinition> CODEC = RecordCodecBuilder.<CutsceneDefinition>create(instance -> instance.group(
            Codec.INT.optionalFieldOf("schema", CURRENT_SCHEMA).forGetter(CutsceneDefinition::schema),
            Codec.INT.optionalFieldOf("duration", 0).forGetter(CutsceneDefinition::duration),
            Codec.BOOL.optionalFieldOf("lock_movement", true).forGetter(CutsceneDefinition::lockMovement),
            Codec.BOOL.optionalFieldOf("letterbox", true).forGetter(CutsceneDefinition::letterbox),
            Codec.BOOL.optionalFieldOf("skippable", false).forGetter(CutsceneDefinition::skippable),
            ResourceLocation.CODEC.optionalFieldOf("music").forGetter(CutsceneDefinition::music),
            ResourceLocation.CODEC.optionalFieldOf("video_id").forGetter(CutsceneDefinition::videoId),
            ResourceLocation.CODEC.optionalFieldOf("audio").forGetter(CutsceneDefinition::audio),
            CameraKeyframe.CODEC.listOf().optionalFieldOf("camera", List.of())
                    .forGetter(CutsceneDefinition::camera),
            CutsceneEvent.CODEC.listOf().optionalFieldOf("events", List.of())
                    .forGetter(CutsceneDefinition::events),
            Credits.CODEC.optionalFieldOf("credits").forGetter(CutsceneDefinition::credits))
            .apply(instance, CutsceneDefinition::new))
            .validate(CutsceneDefinition::validate);

    public boolean isVideo() {
        return videoId.isPresent();
    }

    private static DataResult<CutsceneDefinition> validate(CutsceneDefinition definition) {
        if (definition.schema() > CURRENT_SCHEMA) {
            return DataResult.error(() -> "cutscene schema " + definition.schema()
                    + " is newer than this build supports (" + CURRENT_SCHEMA + ")");
        }
        if (!definition.isVideo() && definition.duration() <= 0) {
            return DataResult.error(() -> "a timed cutscene needs a positive duration");
        }
        return DataResult.success(definition);
    }

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
