package com.mornfire.sqdclib.dialogue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mornfire.sqdclib.storystate.action.StoryAction;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record DialogueNode(
        Optional<Component> speaker,
        Component text,
        Optional<String> next,
        List<DialogueOption> options,
        List<StoryAction> actions) {

    public static final Codec<DialogueNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.optionalFieldOf("speaker").forGetter(DialogueNode::speaker),
            ComponentSerialization.CODEC.fieldOf("text").forGetter(DialogueNode::text),
            Codec.STRING.optionalFieldOf("next").forGetter(DialogueNode::next),
            DialogueOption.CODEC.listOf().optionalFieldOf("options", List.of()).forGetter(DialogueNode::options),
            StoryAction.CODEC.listOf().optionalFieldOf("actions", List.of()).forGetter(DialogueNode::actions))
            .apply(instance, DialogueNode::new));
}
