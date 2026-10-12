package com.mornfire.sqdclib.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record CutsceneDefinition(
        int duration,
        boolean lockMovement,
        boolean letterbox,
        List<CameraKeyframe> camera,
        List<CutsceneEvent> events) {

    public static final Codec<CutsceneDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("duration").forGetter(CutsceneDefinition::duration),
            Codec.BOOL.optionalFieldOf("lock_movement", true).forGetter(CutsceneDefinition::lockMovement),
            Codec.BOOL.optionalFieldOf("letterbox", true).forGetter(CutsceneDefinition::letterbox),
            CameraKeyframe.CODEC.listOf().optionalFieldOf("camera", List.of())
                    .forGetter(CutsceneDefinition::camera),
            CutsceneEvent.CODEC.listOf().optionalFieldOf("events", List.of())
                    .forGetter(CutsceneDefinition::events))
            .apply(instance, CutsceneDefinition::new));
}
