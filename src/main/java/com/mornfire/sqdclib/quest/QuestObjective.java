package com.mornfire.sqdclib.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mornfire.sqdclib.storystate.condition.StoryCondition;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record QuestObjective(
        String id,
        Component title,
        StoryCondition done,
        Optional<Progress> progress) {

    public static final Codec<QuestObjective> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(QuestObjective::id),
            ComponentSerialization.CODEC.fieldOf("title").forGetter(QuestObjective::title),
            StoryCondition.CODEC.fieldOf("done").forGetter(QuestObjective::done),
            Progress.CODEC.optionalFieldOf("progress").forGetter(QuestObjective::progress))
            .apply(instance, QuestObjective::new));

    public record Progress(String counter, int max) {
        public static final Codec<Progress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("counter").forGetter(Progress::counter),
                Codec.INT.fieldOf("max").forGetter(Progress::max))
                .apply(instance, Progress::new));

        public int current(int raw) {
            return Math.max(0, Math.min(raw, max));
        }
    }
}
