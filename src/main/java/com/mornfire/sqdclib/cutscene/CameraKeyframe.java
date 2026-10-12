package com.mornfire.sqdclib.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

public record CameraKeyframe(
        int time,
        Vec3 position,
        float yaw,
        float pitch,
        float roll,
        float fov,
        Easing easing) {

    public static final Codec<CameraKeyframe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("time").forGetter(CameraKeyframe::time),
            Vec3.CODEC.fieldOf("position").forGetter(CameraKeyframe::position),
            Codec.FLOAT.optionalFieldOf("yaw", 0.0F).forGetter(CameraKeyframe::yaw),
            Codec.FLOAT.optionalFieldOf("pitch", 0.0F).forGetter(CameraKeyframe::pitch),
            Codec.FLOAT.optionalFieldOf("roll", 0.0F).forGetter(CameraKeyframe::roll),
            Codec.FLOAT.optionalFieldOf("fov", 70.0F).forGetter(CameraKeyframe::fov),
            Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(CameraKeyframe::easing))
            .apply(instance, CameraKeyframe::new));
}
