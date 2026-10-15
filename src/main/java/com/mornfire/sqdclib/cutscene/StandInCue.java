package com.mornfire.sqdclib.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;

public record StandInCue(boolean hide, List<StandInKey> keys) {
    public static final Codec<StandInCue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("hide", true).forGetter(StandInCue::hide),
            StandInKey.CODEC.listOf().optionalFieldOf("keys", List.of()).forGetter(StandInCue::keys))
            .apply(instance, StandInCue::new));

    public record StandInKey(int time, Vec3 offset, float yawOffset, Motion motion) {
        public static final Codec<StandInKey> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("time").forGetter(StandInKey::time),
                Vec3.CODEC.optionalFieldOf("offset", Vec3.ZERO).forGetter(StandInKey::offset),
                Codec.FLOAT.optionalFieldOf("yaw_offset", 0.0F).forGetter(StandInKey::yawOffset),
                Motion.CODEC.optionalFieldOf("motion", Motion.IDLE).forGetter(StandInKey::motion))
                .apply(instance, StandInKey::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, Vec3> VEC_CODEC = StreamCodec.composite(
                ByteBufCodecs.DOUBLE, Vec3::x,
                ByteBufCodecs.DOUBLE, Vec3::y,
                ByteBufCodecs.DOUBLE, Vec3::z,
                Vec3::new);

        public static final StreamCodec<RegistryFriendlyByteBuf, StandInKey> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, StandInKey::time,
                        VEC_CODEC, StandInKey::offset,
                        ByteBufCodecs.FLOAT, StandInKey::yawOffset,
                        ByteBufCodecs.VAR_INT.map(value -> Motion.values()[value], Motion::ordinal),
                        StandInKey::motion,
                        StandInKey::new);
    }

    public enum Motion implements StringRepresentable {
        IDLE,
        WALK,
        WAVE,
        POINT,
        LOOK_UP,
        BOW;

        public static final Codec<Motion> CODEC = StringRepresentable.fromEnum(Motion::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
