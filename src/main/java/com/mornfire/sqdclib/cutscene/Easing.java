package com.mornfire.sqdclib.cutscene;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum Easing implements StringRepresentable {
    LINEAR,
    EASE_IN,
    EASE_OUT,
    EASE_IN_OUT;

    public static final Codec<Easing> CODEC = StringRepresentable.fromEnum(Easing::values);

    public float apply(float t) {
        float clamped = Math.max(0.0F, Math.min(1.0F, t));
        return switch (this) {
            case LINEAR -> clamped;
            case EASE_IN -> clamped * clamped;
            case EASE_OUT -> 1.0F - (1.0F - clamped) * (1.0F - clamped);
            case EASE_IN_OUT -> clamped < 0.5F
                    ? 2.0F * clamped * clamped
                    : 1.0F - 2.0F * (1.0F - clamped) * (1.0F - clamped);
        };
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
