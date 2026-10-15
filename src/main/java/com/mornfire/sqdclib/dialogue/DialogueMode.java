package com.mornfire.sqdclib.dialogue;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum DialogueMode implements StringRepresentable {
    AMBIENT,
    CINEMATIC;

    public static final Codec<DialogueMode> CODEC = StringRepresentable.fromEnum(DialogueMode::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
