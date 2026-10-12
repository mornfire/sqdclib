package com.mornfire.sqdclib.dialogue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mornfire.sqdclib.storystate.action.StoryAction;
import com.mornfire.sqdclib.storystate.condition.StoryCondition;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record DialogueOption(
        Component text,
        Optional<StoryCondition> condition,
        Optional<String> next,
        List<StoryAction> actions) {

    public static final Codec<DialogueOption> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("text").forGetter(DialogueOption::text),
            StoryCondition.CODEC.optionalFieldOf("condition").forGetter(DialogueOption::condition),
            Codec.STRING.optionalFieldOf("next").forGetter(DialogueOption::next),
            StoryAction.CODEC.listOf().optionalFieldOf("actions", List.of()).forGetter(DialogueOption::actions))
            .apply(instance, DialogueOption::new));
}
