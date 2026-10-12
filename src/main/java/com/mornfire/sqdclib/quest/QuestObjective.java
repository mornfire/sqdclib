package com.mornfire.sqdclib.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mornfire.sqdclib.storystate.condition.StoryCondition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record QuestObjective(String id, Component title, StoryCondition done) {
    public static final Codec<QuestObjective> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(QuestObjective::id),
            ComponentSerialization.CODEC.fieldOf("title").forGetter(QuestObjective::title),
            StoryCondition.CODEC.fieldOf("done").forGetter(QuestObjective::done))
            .apply(instance, QuestObjective::new));
}
