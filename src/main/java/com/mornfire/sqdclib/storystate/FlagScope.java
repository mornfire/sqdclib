package com.mornfire.sqdclib.storystate;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum FlagScope implements StringRepresentable {
    PLAYER,
    WORLD,
    ANY;

    public static final Codec<FlagScope> CODEC = StringRepresentable.fromEnum(FlagScope::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
