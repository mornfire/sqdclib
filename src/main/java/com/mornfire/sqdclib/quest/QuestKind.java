package com.mornfire.sqdclib.quest;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum QuestKind implements StringRepresentable {
    MAIN,
    SIDE;

    public static final Codec<QuestKind> CODEC = StringRepresentable.fromEnum(QuestKind::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
