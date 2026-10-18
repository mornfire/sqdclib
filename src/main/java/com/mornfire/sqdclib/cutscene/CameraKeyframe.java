package com.mornfire.sqdclib.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.phys.Vec3;

public record CameraKeyframe(
        int time,
        Vec3 position,
        float yaw,
        float pitch,
        float roll,
        float fov,
        Easing easing,
        Optional<Vec3> lookAt) {

    public static final Codec<CameraKeyframe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("time").forGetter(CameraKeyframe::time),
            Vec3.CODEC.fieldOf("position").forGetter(CameraKeyframe::position),
            Codec.FLOAT.optionalFieldOf("yaw", 0.0F).forGetter(CameraKeyframe::yaw),
            Codec.FLOAT.optionalFieldOf("pitch", 0.0F).forGetter(CameraKeyframe::pitch),
            Codec.FLOAT.optionalFieldOf("roll", 0.0F).forGetter(CameraKeyframe::roll),
            Codec.FLOAT.optionalFieldOf("fov", 70.0F).forGetter(CameraKeyframe::fov),
            Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(CameraKeyframe::easing),
            Vec3.CODEC.optionalFieldOf("look_at").forGetter(CameraKeyframe::lookAt))
            .apply(instance, CameraKeyframe::new));

    public CameraKeyframe(int time, Vec3 position, float yaw, float pitch, float roll, float fov, Easing easing) {
        this(time, position, yaw, pitch, roll, fov, easing, Optional.empty());
    }

    public float resolvedYaw() {
        if (lookAt.isEmpty()) {
            return yaw;
        }
        Vec3 direction = lookAt.get().subtract(position);
        if (direction.lengthSqr() < 1.0E-6) {
            return yaw;
        }
        return (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
    }

    public float resolvedPitch() {
        if (lookAt.isEmpty()) {
            return pitch;
        }
        Vec3 direction = lookAt.get().subtract(position);
        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        if (horizontal < 1.0E-6 && Math.abs(direction.y) < 1.0E-6) {
            return pitch;
        }
        return (float) Math.toDegrees(-Math.atan2(direction.y, horizontal));
    }
}
