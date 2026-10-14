package com.mornfire.sqdclib.dialogue;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum DialoguePlacement implements StringRepresentable {
    BOTTOM,
    CENTER,
    TOP;

    public static final Codec<DialoguePlacement> CODEC = StringRepresentable.fromEnum(DialoguePlacement::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
